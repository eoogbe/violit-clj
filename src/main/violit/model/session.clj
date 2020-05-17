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
(ns violit.model.session
  (:require
    [buddy.sign.jwt :as jwt]
    [buddy.core.keys :as keys]
    [buddy.hashers :as hashers]
    [clojure.java.io :as io]
    [com.fulcrologic.fulcro.server.api-middleware :as fmw]
    [com.wsscode.pathom.connect :as pc :refer [defmutation defresolver]]
    [taoensso.timbre :as log]
    [tick.alpha.api :as t]
    [violit.components.database :as db]
    [violit.db.account :as db.account]
    [violit.schema.account :as account]))

(def private-key
  (let [passphrase (System/getenv "VIOLIT_PASSPHRASE")]
    (keys/private-key (io/resource "private/privkey.pem") passphrase)))

(def public-key
  (keys/public-key (io/resource "private/pubkey.pem")))

(defn logged-in?
  [{:keys [::status]}]
  (= status :success))

(defn get-credentials
  [{::db/keys [conn] :as env}]
  (if-let [token (-> env :ring/request :session ::token)]
    (let [{username :sub} (jwt/decrypt token private-key {:alg :rsa-oaep-256})
          {::account/keys [id]} (db.account/get-account-by-username conn username)]
      {::account/id       id
       ::account/username username
       ::status           :success})
    {::status :logged-out}))

(defn ensure-logged-in
  [env]
  (let [credentials (get-credentials env)]
    (if (logged-in? credentials)
      credentials
      (throw
        (RuntimeException. "Not authenticated")))))

(defresolver current-credentials-resolver [env _input]
  {::pc/output [{::current-credentials [::account/id ::account/username ::status]}]}
  (let [{::account/keys [username] :as credentials} (get-credentials env)]
    (when (logged-in? credentials)
      (log/info username "already logged in"))
    {::current-credentials credentials}))

(defn add-credentials
  [env {::account/keys [username] :as credentials}]
  (let [existing-session (-> env :ring/request :session)
        data {:iat (t/now)
              :sub username}
        token (jwt/encrypt data public-key {:alg :rsa-oaep-256})]
    (fmw/augment-response
      credentials
      #(assoc % :session (assoc existing-session ::token token)))))

(defn remove-credentials
  [env credentials]
  (let [existing-session (-> env :ring/request :session)]
    (fmw/augment-response
      credentials
      #(assoc % :session (dissoc existing-session ::token)))))

(defmutation log-in [{::db/keys [conn] :as env} {::account/keys [username password]}]
  {::pc/params [::account/username ::account/password]
   ::pc/output [::account/username ::status]}
  (log/info "Attempt login for" username)
  (let [{::account/keys [id encrypted-password]} (db.account/get-account-by-username conn username)
        passwords-match? (and encrypted-password (hashers/check password encrypted-password))]
    (if passwords-match?
      (do
        (log/info "Login for" username)
        (add-credentials env {::account/id       id
                              ::account/username username
                              ::status           :success}))
      (do
        (log/error "Cannot login: Invalid credentials supplied for" username)
        (remove-credentials env {::status :invalid})))))

(defmutation log-out [env _params]
  {::pc/output [::account/username ::status]}
  (log/info "Logging out")
  (remove-credentials env {::status :logged-out}))

(def resolvers [current-credentials-resolver log-in log-out])
