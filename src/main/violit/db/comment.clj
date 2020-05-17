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
(ns violit.db.comment
  (:require
    [crux.api :as crux]
    [taoensso.timbre :as log]
    [tick.alpha.api :as t]
    [violit.db.article :as article]
    [violit.logger :as logger]
    [violit.schema.account :as account]
    [violit.schema.comment :as comment]))

(defn get-comment
  [conn id]
  (crux/entity (crux/db conn) id))

(defn ensure-comment
  [conn id]
  (if-let [comment (get-comment conn id)]
    comment
    (throw
      (ex-info "Comment not found" {:id id}))))

(defn get-comment-id-by-cursor
  [conn cursor]
  (when-first [[id thread-slug] (crux/q (crux/db conn)
                                        {:find  '[id thread-slug]
                                         :where '[[c ::comment/id id]
                                                  [c ::comment/cursor cursor]
                                                  [c ::comment/thread-slug thread-slug]]
                                         :args  [{'cursor cursor}]})]
    {::comment/id          id
     ::comment/thread-slug thread-slug}))

(defn get-comment-revisions
  [conn id]
  (article/get-revisions conn id {:unique-key     ::comment/text
                                  :deleted-at-key ::comment/deleted-at
                                  :updated-at-key ::comment/updated-at}))

(defn create-comment
  [conn {::comment/keys [id] :as comment}]
  (logger/log-entity "Creating comments" comment)
  (crux/submit-tx conn [[:crux.tx/put
                         (assoc comment :crux.db/id id)]]))

(defn delete-comment
  [conn comment]
  (logger/log-entity "Deleting comment" comment)
  (let [updated-comment (assoc comment ::comment/deleted-at (t/now))]
    (crux/submit-tx conn [[:crux.tx/put updated-comment]])))

(defn update-comment
  [conn comment new-text]
  (log/info "Updating comment" comment "with text" new-text)
  (let [updated-comment (assoc comment ::comment/text new-text
                                       ::comment/updated-at (t/now))]
    (crux/submit-tx conn [[:crux.tx/put updated-comment]])))

(defn upvote-comment
  [conn {::comment/keys [upvoters] :as comment} {::account/keys [username]}]
  (logger/log-entity "Upvoting comment" comment)
  (let [update-comment (if (contains? upvoters username) disj conj)
        updated-comment (-> comment
                            (update ::comment/upvoters update-comment username)
                            (update ::comment/downvoters disj username))]
    (crux/submit-tx conn [[:crux.tx/put updated-comment]])))

(defn downvote-comment
  [conn {::comment/keys [downvoters] :as comment} {::account/keys [username]}]
  (logger/log-entity "Downvoting comment" comment)
  (let [update-comment (if (contains? downvoters username) disj conj)
        updated-comment (-> comment
                            (update ::comment/downvoters update-comment username)
                            (update ::comment/upvoters disj username))]
    (crux/submit-tx conn [[:crux.tx/put updated-comment]])))
