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
(ns violit.schema.thread
  (:require
    [clojure.spec.alpha :as s]
    [clojure.string :as str]
    [tick.alpha.api :as t]
    [violit.schema.account :as account]
    [violit.utils.slug :as slug]
    [violit.utils.string :as vstr]
    [violit.utils.time :as time]))

(s/def ::id uuid?)
(s/def ::slug string?)
(s/def ::title (s/and string? vstr/present?))
(s/def ::body (s/nilable string?))
(s/def ::created-at time/instant?)
(s/def ::updated-at time/instant?)
(s/def ::author (s/nilable string?))
(s/def ::cursor string?)
(s/def ::deleted-at time/instant?)
(s/def ::voters (s/coll-of string? :kind set? :distinct true))
(s/def ::upvoters ::voters)
(s/def ::downvoters ::voters)
(s/def ::thread (s/keys
                  :req [::id ::slug ::title ::created-at ::author ::cursor ::upvoters ::downvoters]
                  :opt [::body ::deleted-at ::updated-at]))

(defn thread-path
  ([slug] [::slug slug])
  ([slug field] [::slug slug field]))

(defn mk-body
  [body]
  (when (vstr/present? body)
    (str/trimr body)))

(defn mk-thread
  [{::keys [title created-at author] :as props}]
  (let [slug (slug/mk-slug title)
        created-at (if created-at
                     (t/instant created-at)
                     (t/now))
        cursor (str created-at author)
        thread (-> props
                   (assoc ::slug slug
                          ::created-at created-at
                          ::cursor cursor
                          ::upvoters #{author}
                          ::downvoters #{})
                   (update ::title str/trim)
                   (update ::body mk-body))]
    (when-not (s/valid? ::thread thread)
      (throw
        (ex-info "Invalid thread" (s/explain-data ::thread thread))))
    thread))

(defn can-delete?
  [{::account/keys [username]} {::keys [author deleted-at]}]
  (and username author (not deleted-at) (= username author)))

(defn can-update?
  [{::account/keys [username]} {::keys [author deleted-at]}]
  (and username author (not deleted-at) (= username author)))

(defn can-vote?
  [{::account/keys [username]} {::keys [deleted-at]}]
  (and username (not deleted-at)))
