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
(ns violit.schema.account
  (:require
    [clojure.spec.alpha :as s]
    [clojure.string :as str]
    [tick.alpha.api :as t]
    [violit.utils.time :as time]
    [violit.utils.uuid :as uuid]))

(s/def ::id uuid?)
(s/def ::username (s/and string? #(re-matches #"^[A-Za-z][\w-]+$" %)))
(s/def ::password (s/and string? #(> (count %) 8)))
(s/def ::created-at time/instant?)
(s/def ::pronouns (s/nilable string?))
(s/def ::account (s/keys :req [::id ::username ::password ::created-at]
                         :opt [::pronouns]))

(defn account-path
  ([username] [::username username])
  ([username field] [::username username field]))

(defn mk-pronouns
  [pronouns]
  (when-not (str/blank? pronouns)
    (str/trim pronouns)))

(defn mk-account
  [{::keys [created-at] :as props}]
  (let [created-at (if created-at
                     (t/instant created-at)
                     (t/now))
        account (assoc props
                  ::id (uuid/gen-uuid)
                  ::created-at created-at)]
    (when-not (s/valid? ::account account)
      (throw
        (ex-info "Invalid account" (s/explain-data ::account account))))
    account))
