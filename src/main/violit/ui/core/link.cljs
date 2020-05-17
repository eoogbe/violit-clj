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
(ns violit.ui.core.link
  (:require
    [clojure.spec.alpha :as s]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [taoensso.timbre :as log]
    [violit.application :refer [app]]
    [violit.lib.routing :as routing]))

(def route-target?
  (some-fn string? map? comp/component? comp/component-class?))

(s/def ::route (s/coll-of string? :kind vector? :min-count 1))
(s/def ::route-targets (s/coll-of route-target? :kind vector?))
(s/def ::save-route? boolean?)

(s/def ::link-args (s/cat :classes (s/? keyword?)
                          :props (s/keys :req-un [(or ::route ::route-targets)]
                                         :opt-un [::save-route?])
                          :children (s/* any?)))

(defn ui-link
  [& args]
  (let [{:keys [classes props children]} (s/conform ::link-args args)
        new-props (-> props
                      (assoc :onClick #(routing/push-route! app props))
                      (dissoc :route :route-targets :save-route?))
        new-args (->
                   (if classes [classes] [])
                   (conj new-props)
                   (concat children))]
    (when-not (s/valid? ::link-args args)
      (log/error (s/explain-str ::link-args args)))
    (apply dom/a new-args)))
