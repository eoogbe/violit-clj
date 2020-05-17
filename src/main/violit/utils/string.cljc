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
(ns violit.utils.string
  (:require
    [clojure.string :as str]))

(defn multiline [& lines]
  (str/join "\n" lines))

(defn present?
  [str]
  (and str (not (str/blank? str))))

(defn rand-str
  [len]
  (apply str (repeatedly len #(char (+ (rand 26) 65)))))
