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
(ns violit.core.dropdown-menu-cards
  (:require
    [com.fulcrologic.fulcro.components :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.ui.core.dropdown-menu :as dropdown-menu]
    [violit.ui.core.styles :as styles]))

(def ITEMS
  [{:icon :add
    :text "Add"}
   {:icon :edit
    :text "Edit"}
   {:icon :delete
    :text "Delete"}])

(defsc Root [_this _props]
  {:css         [[:* {:box-sizing "border-box"}]
                 [:.root (merge styles/body {:position "relative"})]]
   :css-include [dropdown-menu/DropdownMenu]}
  (dom/div
    :.root
    (dropdown-menu/ui-dropdown-menu {:id    "dropdown-menu1"
                                     :items ITEMS
                                     :open? true})
    (inj/style-element {:component Root})))

(ws/defcard dropdown-menu
            (ct.fulcro/fulcro-card {::ct.fulcro/root       Root
                                    ::ct.fulcro/wrap-root? false}))
