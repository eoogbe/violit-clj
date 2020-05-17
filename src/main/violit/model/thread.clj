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
(ns violit.model.thread
  (:require
    [com.wsscode.pathom.connect :as pc :refer [defmutation defresolver]]
    [tick.alpha.api :as t]
    [violit.components.database :as db]
    [violit.db.account :as db.account]
    [violit.db.thread :as db.thread]
    [violit.model.session :as session]
    [violit.model.voting :as voting]
    [violit.schema.account :as account]
    [violit.schema.comment :as comment]
    [violit.schema.thread :as thread]
    [violit.utils.base64 :as base64]))

(defresolver thread-resolver [{::db/keys [conn]} {::thread/keys [slug]}]
  {::pc/input  #{::thread/slug}
   ::pc/output [::thread/slug
                ::thread/title
                ::thread/body
                ::thread/created-at
                ::thread/author
                ::thread/cursor
                ::thread/deleted-at
                ::thread/updated-at
                ::thread/upvoters
                ::thread/downvoters]}
  (let [thread (db.thread/get-thread-by-slug conn slug)]
    (update thread ::thread/cursor base64/encode)))

(defresolver thread-author-pronouns-resolver [{::db/keys [conn]} {::thread/keys [author]}]
  {::pc/input  #{::thread/author}
   ::pc/output [::thread/author-pronouns]}
  (let [{::account/keys [pronouns]} (db.account/get-account-by-username conn author)]
    {::thread/author-pronouns pronouns}))

(defresolver thread-score-resolver [_env {::thread/keys [upvoters downvoters]}]
  {::pc/input  #{::thread/upvoters ::thread/downvoters}
   ::pc/output [::thread/score]}
  {::thread/score (voting/get-score upvoters downvoters)})

(defresolver thread-vote-status-resolver [env {::thread/keys [upvoters downvoters]}]
  {::pc/input  #{::thread/upvoters ::thread/downvoters}
   ::pc/output [::thread/vote-status]}
  {::thread/vote-status (voting/get-vote-status env upvoters downvoters)})

(defresolver thread-edit-history-resolver [{::db/keys [conn]} {cursor ::thread/edit-history}]
  {::pc/input  #{::thread/edit-history}
   ::pc/output [::thread/cursor
                ::thread/slug
                ::thread/title
                {::thread/revisions [:revision/updated-at :revision/old-text :revision/new-text]}]}
  (if-let [{::thread/keys [id slug title]}
           (db.thread/get-thread-by-cursor conn (base64/decode cursor))]
    (let [revisions (db.thread/get-thread-revisions conn id)]
      (when (seq revisions)
        {::thread/cursor    cursor
         ::thread/slug      slug
         ::thread/title     title
         ::thread/revisions revisions}))
    (throw
      (ex-info "Thread not found" {:cursor cursor}))))

(defresolver thread-comment-page-resolver [{::db/keys [conn] :as env} {slug ::thread/comment-page}]
  {::pc/input  #{::thread/comment-page}
   ::pc/output [::thread/slug
                ::thread/next-comment-cursor
                {::thread/comments [::comment/id ::comment/cursor]}]}
  (let [{:keys [after]} (:query-params env)
        after (if after
                (base64/decode after)
                (str (t/now)))
        comment-page (db.thread/get-thread-comment-page-by-slug
                       conn slug {:after after})
        comment-page (mapv #(update % ::comment/cursor base64/encode) comment-page)
        has-more-comments? (= (count comment-page) db.thread/COMMENT-LIMIT)
        next-cursor (when has-more-comments?
                      (-> comment-page last ::comment/cursor base64/encode))]
    {::thread/slug                slug
     ::thread/next-comment-cursor next-cursor
     ::thread/comments            (cond-> comment-page
                                          has-more-comments? butlast)}))

(defmutation create-thread [{::db/keys [conn] :as env} {:keys [id diff] :as params}]
  {::pc/params [:id :diff]
   ::pc/output [::thread/id
                ::thread/slug
                ::thread/cursor
                ::thread/title
                ::thread/body
                ::thread/created-at
                ::thread/author
                ::thread/upvoters
                ::thread/downvoters]}
  (let [{::account/keys [username]} (session/ensure-logged-in env)]
    (if-let [[_ props] (some-> diff first)]
      (let [thread (-> props
                       (assoc
                         ::thread/id id
                         ::thread/author username)
                       thread/mk-thread)]
        (db.thread/create-thread conn thread)
        (update thread ::thread/cursor base64/encode))
      (throw
        (ex-info "No input for thread creation" {:params params})))))

(defmutation delete-thread [{::db/keys [conn] :as env} {:keys [slug]}]
  {::pc/params [:slug]}
  (let [credentials (session/ensure-logged-in env)
        thread (db.thread/ensure-thread conn slug)]
    (if (thread/can-delete? credentials thread)
      (db.thread/delete-thread conn thread)
      (throw
        (ex-info "Delete thread forbidden" {:slug slug})))))

(defmutation update-thread [{::db/keys [conn] :as env} {:keys [slug diff] :as params}]
  {::pc/params [:slug :diff]}
  (if diff
    (let [credentials (session/ensure-logged-in env)
          thread (db.thread/ensure-thread conn slug)]
      (if (thread/can-update? credentials thread)
        (when-let [{:keys [before after]} (-> diff first second ::thread/body)]
          (let [new-body (thread/mk-body after)]
            (when-not (= new-body before)
              (db.thread/update-thread conn thread new-body))))
        (throw
          (ex-info "Update thread forbidden" {:params params}))))
    (throw
      (ex-info "No input for thread update" {:params params}))))

(defmutation upvote-thread [{::db/keys [conn] :as env} {:keys [slug]}]
  {::pc/params [:slug]}
  (let [credentials (session/ensure-logged-in env)
        thread (db.thread/ensure-thread conn slug)]
    (if (comment/can-vote? credentials thread)
      (db.thread/upvote-thread conn thread credentials)
      (throw
        (ex-info "Upvote thread forbidden" {:slug slug})))))

(defmutation downvote-thread [{::db/keys [conn] :as env} {:keys [slug]}]
  {::pc/params [:slug]}
  (let [credentials (session/ensure-logged-in env)
        thread (db.thread/ensure-thread conn slug)]
    (if (comment/can-vote? credentials thread)
      (db.thread/downvote-thread conn thread credentials)
      (throw
        (ex-info "Downvote thread forbidden" {:slug slug})))))

(def resolvers
  [thread-resolver
   thread-author-pronouns-resolver
   thread-score-resolver
   thread-vote-status-resolver
   thread-edit-history-resolver
   thread-comment-page-resolver
   create-thread
   delete-thread
   update-thread
   upvote-thread
   downvote-thread])
