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
(ns violit.ui.core.loading-indicator
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [garden.stylesheet :as stylesheet]
    [violit.ui.core.theme :as theme]))

(defn loading-indicator-path
  ([] [:component/id ::LoadingIndicator])
  ([field] [:component/id ::LoadingIndicator field]))

(defn set-visible*
  [state {:keys [visible?]}]
  (assoc-in state (loading-indicator-path :ui/visible?) visible?))

(defmutation set-visible [params]
  (action [{:keys [state]}]
          (swap! state set-visible* params)))

(def bar1
  (stylesheet/at-keyframes
    "bar1"

    ["0%" {:left  "-35%"
           :right "100%"}]
    ["60%" {:left  "100%"
            :right "-90%"}]
    ["100%" {:left  "100%"
             :right "-9-%"}]))

(def bar2
  (stylesheet/at-keyframes
    "bar2"

    ["0%" {:left  "-200%"
           :right "100%"}]
    ["60%" {:left  "107%"
            :right "-8%"}]
    ["100%" {:left  "107%"
             :right "-8%"}]))

(defsc LoadingIndicator [_this {:ui/keys [visible?]}]
  {:query         [:ui/visible?]
   :ident         (fn [] (loading-indicator-path))
   :initial-state {:ui/visible? :param/visible?}
   :css           [bar1
                   bar2
                   [:.container {:position         "relative"
                                 :width            "100%"
                                 :height           "4px"
                                 :overflow         "hidden"
                                 :background-color theme/primary-color-light}]
                   [:.bar {:position         "absolute"
                           :top              0
                           :bottom           0
                           :background-color theme/primary-color}]
                   [:.bar1 {:animation
                            [[bar1
                              "2.1s"
                              "cubic-bezier(0.65, 0.815, 0.735, 0.395)" "infinite"]]}]
                   [:.bar2 {:animation
                            [[bar2
                              "2.1s"
                              "cubic-bezier(0.165, 0.84, 0.44, 1)" "1.15s" "infinite"]]}]]}
  (when visible?
    (dom/div
      :.container
      {:role "progressbar"}
      (dom/div :.bar.bar1)
      (dom/div :.bar.bar2))))

(def ui-loading-indicator (comp/factory LoadingIndicator))
