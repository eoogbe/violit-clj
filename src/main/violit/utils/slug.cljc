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
(ns violit.utils.slug
  (:require
    [cuerdas.core :as str]
    [violit.utils.string :as vstr]))

(defn mk-slug
  [name]
  (let [rand-part (vstr/rand-str 8)
        name-part (-> name str/trim str/slug (str/slice 0 247))]
    (str name-part "-" rand-part)))
