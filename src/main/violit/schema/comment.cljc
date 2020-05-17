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
(ns violit.schema.comment
  (:require
    [clojure.spec.alpha :as s]
    [clojure.string :as str]
    [tick.alpha.api :as t]
    [violit.schema.account :as account]
    [violit.utils.string :as vstr]
    [violit.utils.time :as time]))

(s/def ::id uuid?)
(s/def ::text (s/and string? vstr/present?))
(s/def ::created-at time/instant?)
(s/def ::author (s/nilable string?))
(s/def ::cursor string?)
(s/def ::deleted-at time/instant?)
(s/def ::updated-at time/instant?)
(s/def ::voters (s/coll-of string? :kind set? :distinct true))
(s/def ::upvoters ::voters)
(s/def ::downvoters ::voters)
(s/def ::comment (s/keys :req [::id ::text ::created-at ::author ::cursor ::upvoters ::downvoters]
                         :opt [::deleted-at ::updated-at]))

(defn comment-path
  ([id] [::id id])
  ([id field] [::id id field]))

(defn mk-comment
  [{::keys [created-at author] :as props}]
  (let [created-at (if created-at
                     (t/instant created-at)
                     (t/now))
        cursor (str created-at author)
        comment (-> props
                    (assoc ::created-at created-at
                           ::cursor cursor
                           ::upvoters #{author}
                           ::downvoters #{})
                    (update ::text str/trimr))]
    (when-not (s/valid? ::comment comment)
      (throw
        (ex-info "Invalid comment" (s/explain-data ::comment comment))))
    comment))

(defn can-delete?
  [{::account/keys [username]} {::keys [author deleted-at]}]
  (and username author (not deleted-at) (= username author)))

(defn can-update?
  [{::account/keys [username]} {::keys [author deleted-at]}]
  (and username author (not deleted-at) (= username author)))

(defn can-vote?
  [{::account/keys [username]} {::keys [deleted-at]}]
  (and username (not deleted-at)))
