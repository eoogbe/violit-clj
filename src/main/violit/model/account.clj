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
(ns violit.model.account
  (:require
    [buddy.hashers :as hashers]
    [com.wsscode.pathom.connect :as pc :refer [defmutation defresolver]]
    [taoensso.timbre :as log]
    [violit.components.database :as db]
    [violit.db.account :as db.account]
    [violit.model.session :as session]
    [violit.schema.account :as account]))

(defresolver account-resolver [{::db/keys [conn]} {::account/keys [username]}]
  {::pc/input  #{::account/username}
   ::pc/output [::account/username ::account/created-at ::account/pronouns]}
  (let [account (db.account/get-account-by-username conn username)]
    (dissoc account ::account/encrypted-password)))

(defresolver has-username-resolver [{::db/keys [conn] :as env} _input]
  {::pc/output [::account/has-username?]}
  (let [username (-> env :ast :params ::account/username)]
    {::account/has-username? (some? (db.account/get-username conn username))}))

(defmutation create-account [{::db/keys [conn]} {:keys [diff] :as params}]
  {::pc/params [:diff]
   ::pc/output [::account/id ::signup-error]}
  (if-let [[_ props] (first diff)]
    (let [{::account/keys [id username] :as account} (account/mk-account props)
          has-username? (some? (db.account/get-username conn username))]
      (if has-username?
        (do
          (log/error "Cannot create account: " username " is already taken")
          {::account/id   id
           ::signup-error "Username is already taken"})
        (do
          (db.account/create-account conn account)
          {::account/id   id
           ::signup-error nil})))
    (throw
      (ex-info "No account to create" {:params params}))))

(defmutation delete-account [{::db/keys [conn] :as env} _params]
  {::pc/params []}
  (let [credentials (session/ensure-logged-in env)]
    (db.account/delete-account conn credentials)))

(defmutation update-password [{::db/keys [conn] :as env} {:keys [old-password new-password]}]
  {::pc/params [:old-password :new-password]}
  (let [{::account/keys [id username]} (session/ensure-logged-in env)
        {::account/keys [encrypted-password] :as account} (db.account/get-account conn id)
        passwords-match? (and encrypted-password (hashers/check old-password encrypted-password))]
    (if passwords-match?
      (do
        (db.account/update-password conn (assoc account ::account/password new-password))
        {::account/id id})
      (do
        (log/error "Cannot change password: Invalid credentials supplied for" username)
        {::account/id            id
         ::change-password-error "Old password is invalid."}))))

(defmutation update-pronouns [{::db/keys [conn] :as env} {:keys [diff] :as params}]
  {::pc/params [:username :diff]}
  (if diff
    (let [{::account/keys [id]} (session/ensure-logged-in env)]
      (when-let [{:keys [before after]} (some-> diff first second ::account/pronouns)]
        (let [new-pronouns (account/mk-pronouns after)]
          (when-not (= new-pronouns before)
            (db.account/update-pronouns conn id new-pronouns)))))
    (throw
      (ex-info "No input for pronouns update" {:params params}))))

(def resolvers
  [account-resolver
   has-username-resolver
   create-account
   delete-account
   update-password
   update-pronouns])
