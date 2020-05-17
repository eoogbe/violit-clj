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
(ns violit.ui.core.click-outside
  (:require
    [goog.events :as events]
    [oops.core :refer [oget]])
  (:import
    [goog.events EventType]))

(defn click-outside
  [el listener]
  (let [listenable-key (events/listen
                         js/document
                         EventType.CLICK
                         (fn [evt]
                           (let [target (oget evt :target)]
                             (when-not (.contains el target)
                               (listener)))))]
    #(events/unlistenByKey listenable-key)))
