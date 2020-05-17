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
(ns violit.db.account
  (:require
    [buddy.hashers :as hashers]
    [clojure.set :as set]
    [crux.api :as crux]
    [taoensso.timbre :as log]
    [violit.schema.account :as account]
    [violit.schema.comment :as comment]
    [violit.schema.thread :as thread]
    [violit.logger :as logger]))

(defn get-account
  [conn id]
  (-> (crux/db conn)
      (crux/entity id)
      (set/rename-keys {::account/password ::account/encrypted-password})))

(defn get-account-by-username
  [conn username]
  (when-first [[id] (crux/q (crux/db conn)
                            {:find  '[id]
                             :where '[[a ::account/id id]
                                      [a ::account/username username]]
                             :args  [{'username username}]})]
    (get-account conn id)))

(defn username-id
  [username]
  (keyword "username" username))

(defn get-username
  "
  Returns the database entity corresponding to the specified `username`.

  We keep the username in a separate entity from the account so that we can preserve usernames from
  deleted accounts."
  [conn username]
  (crux/entity (crux/db conn) (username-id username)))

(defn create-account
  [conn {::account/keys [id username] :as account}]
  (logger/log-entity "Creating account" account)
  (let [new-account (-> account
                        (assoc :crux.db/id id)
                        (update ::account/password hashers/derive))]
    (crux/submit-tx conn [[:crux.tx/put new-account]
                          [:crux.tx/put {:crux.db/id (username-id username)}]])))

(defn mk-remove-prop-txs
  [conn q k]
  (mapv (fn [[id]]
          (let [entity (crux/entity (crux/db conn) id)]
            [:crux.tx/put (assoc entity k nil)]))
        q))

(defn delete-account
  [conn {::account/keys [id username] :as account}]
  (logger/log-entity "Deleting account" account)
  (let [thread-q (crux/q (crux/db conn)
                         {:find  '[id]
                          :where '[[t ::thread/id id]
                                   [t ::thread/author author]]
                          :args  [{'author username}]})
        comment-q (crux/q (crux/db conn)
                          {:find  '[id]
                           :where '[[c ::comment/id id]
                                    [c ::comment/author author]]
                           :args  [{'author username}]})
        thread-txs (mk-remove-prop-txs conn thread-q ::thread/author)
        comment-txs (mk-remove-prop-txs conn comment-q ::comment/author)
        txs (->
              (concat thread-txs comment-txs)
              (conj [:crux.tx/delete id])
              vec)]
    (crux/submit-tx conn txs)))

(defn update-password
  [conn account]
  (logger/log-entity "Updating account password" account)
  (let [updated-account (update account ::account/password hashers/derive)]
    (crux/submit-tx conn [[:crux.tx/put updated-account]])))

(defn update-pronouns
  [conn id pronouns]
  (log/info "Updating account" id "with pronouns" pronouns)
  (let [account (crux/entity (crux/db conn) id)
        updated-account (assoc account ::account/pronouns pronouns)]
    (crux/submit-tx conn [[:crux.tx/put updated-account]])))
