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
(ns violit.ui.dialog.dialog
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.dom.icons :as icons]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [goog.object :as gobj]
    [violit.ui.core.click-outside :as click-outside]
    [violit.ui.core.theme :as theme]
    [violit.ui.dialog.modal :as modal]))

(defsc Dialog [this props]
  {:initLocalState       (fn [this _props]
                           {:save-dialog-ref #(gobj/set this "dialog-el" %)})
   :componentDidMount    (fn [this]
                           (let [dialog-el (gobj/get this "dialog-el")
                                 close (comp/get-computed this :close)
                                 cleanup (click-outside/click-outside dialog-el close)]
                             (comp/update-state! this assoc :cleanup cleanup)))
   :componentWillUnmount (fn [this]
                           (let [{:keys [cleanup]} (comp/get-state this)]
                             (cleanup)))
   :css                  [[:.backdrop {:display          "flex"
                                       :justify-content  "center"
                                       :align-items      "center"
                                       :position         "fixed"
                                       :left             0
                                       :top              0
                                       :right            0
                                       :bottom           0
                                       :background-color "rgba(0, 0, 0, 0.5)"}]
                          [:.dialog {:background-color theme/white
                                     :padding          [[theme/spacing-md theme/spacing-lg]]}]
                          [:.button-panel {:display         "flex"
                                           :justify-content "flex-end"}]
                          [:.close-button {:font-size  "1rem"
                                           :min-width  "1.25em"
                                           :min-height "1.25em"
                                           :padding    0
                                           :border     "none"
                                           :background "none"}]
                          [:.close-icon {:fill     theme/gray700
                                         :overflow "visible"}]]}
  (let [{:keys [close-icon]} (css/get-classnames Dialog)
        {:keys [save-dialog-ref]} (comp/get-state this)
        close (comp/get-computed this :close)]
    (modal/ui-modal
      {}
      (dom/div
        :.backdrop
        {:onKeyDown #(when (evt/escape? %)
                       (close))}
        (dom/div
          :.dialog
          (merge props {:ref  save-dialog-ref
                        :role "dialog"})
          (dom/div
            :.button-panel
            (dom/button
              :.close-button
              {:onClick close :type "button"}
              (icons/ui-icon {:className close-icon
                              :height    18
                              :icon      :close
                              :title     "Close"
                              :width     18})))
          (comp/children this))))))

(def ui-dialog (comp/factory Dialog))
