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
(ns violit.ui.core.main-container
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [violit.ui.core.theme :as theme]))

(defsc MainContainer [this {:keys [sidebar]}]
  {:css [[:.main-container {:display "flex"
                            :flex    1
                            :width   "100%"
                            :padding [[theme/spacing-lg 0]]}]
         [:.sidebar {:width            "20%"
                     :padding          theme/spacing-lg
                     :background-color theme/white}]
         [:.main {:width            "calc(60% - 2em)"
                  :margin-left      "calc(20% + 1em)"
                  :padding          theme/spacing-lg
                  :background-color theme/white}]
         [:.main-with-sidebar {:margin-left theme/spacing-lg}]]}
  (dom/div
    :.main-container
    (when sidebar
      (dom/aside :.sidebar sidebar))
    (dom/main
      :.main
      {:classes [(when sidebar :.main-with-sidebar)]}
      (comp/children this))))

(def ui-main-container (comp/factory MainContainer))
