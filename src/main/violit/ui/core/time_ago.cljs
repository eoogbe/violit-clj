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
(ns violit.ui.core.time-ago
  (:require
    [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
    ["react-timeago" :default BaseTimeAgo]
    [tick.alpha.api :as t]))

(def ui-base-time-ago (interop/react-factory BaseTimeAgo))

(defn ui-time-ago
  [props]
  (letfn [(formatter [_value unit suffix _epoch-millis next-formatter]
            (if (= unit "second")
              (str "less than a minute " suffix)
              (next-formatter)))]
    (ui-base-time-ago (-> props
                          (assoc :formatter formatter)
                          (update :date t/inst)))))
