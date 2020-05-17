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
(ns violit.db.thread
  (:require
    [crux.api :as crux]
    [taoensso.timbre :as log]
    [tick.alpha.api :as t]
    [violit.db.article :as article]
    [violit.logger :as logger]
    [violit.schema.account :as account]
    [violit.schema.comment :as comment]
    [violit.schema.thread :as thread]
    [violit.utils.voting :as voting]))

(def COMMENT-LIMIT 21)

(defn get-thread-by-slug
  [conn slug]
  (when-first [[id] (crux/q (crux/db conn)
                            {:find  '[id]
                             :where '[[t ::thread/id id]
                                      [t ::thread/slug slug]]
                             :args  [{'slug slug}]})]
    (crux/entity (crux/db conn) id)))

(defn ensure-thread
  [conn slug]
  (if-let [thread (get-thread-by-slug conn slug)]
    thread
    (throw
      (ex-info "Thread not found" {:slug slug}))))

(defn get-thread-by-cursor
  [conn cursor]
  (when-first [[id slug title] (crux/q (crux/db conn)
                                       {:find  '[id slug title]
                                        :where '[[t ::thread/id id]
                                                 [t ::thread/slug slug]
                                                 [t ::thread/title title]
                                                 [t ::thread/cursor cursor]]
                                        :args  [{'cursor cursor}]})]
    {::thread/id    id
     ::thread/slug  slug
     ::thread/title title}))

(defn get-thread-revisions
  [conn id]
  (article/get-revisions conn id {:unique-key     ::thread/body
                                  :deleted-at-key ::thread/deleted-at
                                  :updated-at-key ::thread/updated-at}))

(defn get-thread-comment-page-by-slug
  [conn slug {:keys [after]}]
  (let [q (crux/q (crux/db conn)
                  {:find     '[id cursor]
                   :where    '[[c ::comment/id id]
                               [c ::comment/cursor cursor]
                               [c ::comment/thread-slug slug]
                               [(<= cursor after)]]
                   :order-by '[[cursor :desc]]
                   :limit    COMMENT-LIMIT
                   :args     [{'slug slug 'after after}]})]
    (->> q
         (map (fn [[id]]
                (let [{::comment/keys [upvoters downvoters] :as comment}
                      (crux/entity (crux/db conn) id)

                      ci-lower-bound (voting/ci-lower-bound upvoters downvoters)]
                  (assoc comment ::comment/ci-lower-bound ci-lower-bound))))
         (sort-by (juxt ::comment/ci-lower-bound ::comment/cursor))
         reverse
         (into [] (map #(select-keys % [::comment/id ::comment/cursor]))))))

(defn create-thread
  [conn {::thread/keys [id] :as thread}]
  (logger/log-entity "Creating thread" thread)
  (let [new-thread (assoc thread :crux.db/id id)]
    (crux/submit-tx conn [[:crux.tx/put new-thread]])))

(defn delete-thread
  [conn thread]
  (logger/log-entity "Deleting thread" thread)
  (let [updated-thread (assoc thread ::thread/deleted-at (t/now))]
    (crux/submit-tx conn [[:crux.tx/put updated-thread]])))

(defn update-thread
  [conn thread new-body]
  (log/info "Updating thread" thread "with body" new-body)
  (let [updated-thread (assoc thread ::thread/body new-body
                                     ::thread/updated-at (t/now))]
    (crux/submit-tx conn [[:crux.tx/put updated-thread]])))

(defn upvote-thread
  [conn {::thread/keys [upvoters] :as thread} {::account/keys [username]}]
  (logger/log-entity "Upvoting thread" thread)
  (let [update-thread (if (contains? upvoters username) disj conj)
        updated-thread (-> thread
                           (update ::thread/upvoters update-thread username)
                           (update ::thread/downvoters disj username))]
    (crux/submit-tx conn [[:crux.tx/put updated-thread]])))

(defn downvote-thread
  [conn {::thread/keys [downvoters] :as thread} {::account/keys [username]}]
  (logger/log-entity "Downvoting thread" thread)
  (let [update-thread (if (contains? downvoters username) disj conj)
        updated-thread (-> thread
                           (update ::thread/downvoters update-thread username)
                           (update ::thread/upvoters disj username))]
    (crux/submit-tx conn [[:crux.tx/put updated-thread]])))
