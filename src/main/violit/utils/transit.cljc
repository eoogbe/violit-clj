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
(ns violit.utils.transit
  (:require
    #?(:cljs [java.time :refer [Instant]])
    [cognitect.transit :as transit]
    [time-literals.read-write])
  #?(:clj
     (:import
       [java.time Instant])))

(def read-handlers
  (into {} (for [[sym fun] time-literals.read-write/tags]
             [(name sym) (transit/read-handler fun)])))

(def write-handlers
  {Instant (transit/write-handler (constantly "instant") str)})
