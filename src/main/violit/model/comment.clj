;;;  Copyright 2022 Google LLC
;;; Licensed under the Apache License, Version 2.0 (the "License");
;;; you may not use this file except in compliance with the License.
;;; You may obtain a copy of the License at
;;;
;;;      http://www.apache.org/licenses/LICENSE-2.0
;;;
;;; Unless required by applicable law or agreed to in writing, software
;;; distributed under the License is distributed on an "AS IS" BASIS,
;;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;;; See the License for the specific language governing permissions and
;;; limitations under the License.
(ns violit.model.comment
  (:require
    [clojure.string :as str]
    [com.wsscode.pathom.connect :as pc :refer [defmutation defresolver]]
    [violit.components.database :as db]
    [violit.db.account :as db.account]
    [violit.db.comment :as db.comment]
    [violit.db.thread :as db.thread]
    [violit.model.session :as session]
    [violit.model.voting :as voting]
    [violit.schema.account :as account]
    [violit.schema.comment :as comment]
    [violit.schema.thread :as thread]
    [violit.utils.base64 :as base64]))

(defresolver comment-resolver [{::db/keys [conn]} {::comment/keys [id]}]
  {::pc/input  #{::comment/id}
   ::pc/output [::comment/id
                ::comment/text
                ::comment/created-at
                ::comment/author
                ::comment/cursor
                ::comment/deleted-at
                ::comment/updated-at
                ::comment/upvoters
                ::comment/downvoters]}
  (let [comment (db.comment/get-comment conn id)]
    (update comment ::comment/cursor base64/encode)))

(defresolver comment-author-pronouns-resolver [{::db/keys [conn]} {::comment/keys [author]}]
  {::pc/input  #{::comment/author}
   ::pc/output [::comment/author-pronouns]}
  (let [{::account/keys [pronouns]} (db.account/get-account-by-username conn author)]
    {::comment/author-pronouns pronouns}))

(defresolver comment-score-resolver [_env {::comment/keys [upvoters downvoters]}]
  {::pc/input  #{::comment/upvoters ::comment/downvoters}
   ::pc/output [::comment/score]}
  {::comment/score (voting/get-score upvoters downvoters)})

(defresolver comment-vote-status-resolver [env {::comment/keys [upvoters downvoters]}]
  {::pc/input  #{::comment/upvoters ::comment/downvoters}
   ::pc/output [::comment/vote-status]}
  {::comment/vote-status (voting/get-vote-status env upvoters downvoters)})

(defresolver comment-edit-history-resolver [{::db/keys [conn]} {cursor ::comment/edit-history}]
  {::pc/input  #{::comment/edit-history}
   ::pc/output [::comment/cursor
                ::comment/id
                ::thread/slug
                ::thread/title
                {::comment/revisions [:revision/updated-at :revision/old-text :revision/new-text]}]}
  (if-let [{::comment/keys [id thread-slug]}
           (db.comment/get-comment-id-by-cursor conn (base64/decode cursor))]
    (let [{::thread/keys [title]} (db.thread/get-thread-by-slug conn thread-slug)
          revisions (db.comment/get-comment-revisions conn id)]
      (when (seq revisions)
        {::comment/cursor    cursor
         ::comment/id        id
         ::thread/slug       thread-slug
         ::thread/title      title
         ::comment/revisions revisions}))
    (throw
      (ex-info "Comment not found" {:cursor cursor}))))

(defmutation create-comment [{::db/keys [conn] :as env} {:keys [id diff thread-slug] :as params}]
  {::pc/params [:id :diff :thread-slug]
   ::pc/output [::comment/id
                ::comment/thread-slug
                ::comment/text
                ::comment/created-at
                ::comment/author
                ::comment/cursor
                ::comment/upvoters
                ::comment/downvoters]}
  (let [{::account/keys [username]} (session/ensure-logged-in env)
        thread (db.thread/ensure-thread conn thread-slug)]
    (if (::thread/deleted-at thread)
      (throw
        (ex-info "Create comment forbidden" {:params params}))
      (if-let [[_ props] (some-> diff first)]
        (let [comment (-> props
                          (assoc
                            ::comment/id id
                            ::comment/author username
                            ::comment/thread-slug thread-slug)
                          comment/mk-comment)]
          (db.comment/create-comment conn comment)
          (update comment ::comment/cursor base64/encode))
        (throw
          (ex-info "No input for comment creation" {:params params}))))))

(defmutation delete-comment [{::db/keys [conn] :as env} {:keys [id]}]
  {::pc/params [:id]}
  (let [credentials (session/ensure-logged-in env)
        {::comment/keys [thread-slug] :as comment} (db.comment/ensure-comment conn id)
        thread (db.thread/ensure-thread conn thread-slug)]
    (if (and (not (::thread/deleted-at thread)) (comment/can-delete? credentials comment))
      (db.comment/delete-comment conn comment)
      (throw
        (ex-info "Delete comment forbidden" {:id id})))))

(defmutation update-comment [{::db/keys [conn] :as env} {:keys [id diff] :as params}]
  {::pc/params [:id :diff]}
  (if-let [{:keys [before after]} (some-> diff first second ::comment/text)]
    (let [credentials (session/ensure-logged-in env)
          {::comment/keys [thread-slug] :as comment} (db.comment/ensure-comment conn id)
          thread (db.thread/ensure-thread conn thread-slug)
          new-text (str/trimr after)]
      (if (and (not (::thread/deleted-at thread)) (comment/can-update? credentials comment))
        (when-not (= new-text before)
          (db.comment/update-comment conn comment new-text))
        (throw
          (ex-info "Update comment forbidden" {:params params}))))
    (throw
      (ex-info "No input for comment update" {:params params}))))

(defmutation upvote-comment [{::db/keys [conn] :as env} {:keys [id]}]
  {::pc/params [:id]}
  (let [credentials (session/ensure-logged-in env)
        comment (db.comment/ensure-comment conn id)]
    (if (comment/can-vote? credentials comment)
      (db.comment/upvote-comment conn comment credentials)
      (throw
        (ex-info "Upvote comment forbidden" {:id id})))))

(defmutation downvote-comment [{::db/keys [conn] :as env} {:keys [id]}]
  {::pc/params [:id]}
  (let [credentials (session/ensure-logged-in env)
        comment (db.comment/ensure-comment conn id)]
    (if (comment/can-vote? credentials comment)
      (db.comment/downvote-comment conn comment credentials)
      (throw
        (ex-info "Downvote comment forbidden" {:id id})))))

(def resolvers
  [comment-resolver
   comment-author-pronouns-resolver
   comment-score-resolver
   comment-vote-status-resolver
   comment-edit-history-resolver
   create-comment
   delete-comment
   update-comment
   upvote-comment
   downvote-comment])
