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
(ns violit.logger
  (:require
    [clojure.walk :as walk]
    [taoensso.timbre :as log]
    [violit.schema.account :as account]))

(def sensitive-keys
  #{::account/password})

(defn- kv-pair?
  [x]
  (and (vector? x) (= 2 (count x))))

(defn remove-omissions
  [value]
  (walk/postwalk
    (fn [x]
      (if (and (kv-pair? x) (contains? sensitive-keys (first x)))
        [(first x) :omitted]
        x))
    value))

(defn log-entity
  [msg entity]
  (binding [*print-level* 4 *print-length* 4]
    (log/info
      msg
      (try
        (-> entity remove-omissions pr-str)
        (catch #?(:clj Exception :cljs :default) e
          (log/error (ex-message e))
          "<failed to serialize>")))))
