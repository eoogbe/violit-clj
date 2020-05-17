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
(ns violit.core.main-container-cards
  (:require
    [com.fulcrologic.fulcro.components :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.ui.core.main-container :as main-container]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]))

(defsc Root [_this {:keys [has-sidebar?]}]
  {:query         [:has-sidebar?]
   :initial-state {:has-sidebar? :param/has-sidebar?}
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root (merge styles/body {:width            "65vw"
                                               :border           [["1px" "solid" theme/gray200]]
                                               :background-color theme/gray200})]]
   :css-include   [main-container/MainContainer]}
  (dom/div
    :.root
    (main-container/ui-main-container
      {:sidebar (when has-sidebar? (dom/p "Sed ut perspiciatis unde omnis iste natus error"))}
      (dom/p "sit voluptatem accusantium doloremque laudantium"))
    (inj/style-element {:component Root})))

(ws/defcard main-container-with-sidebar
            (ct.fulcro/fulcro-card {::ct.fulcro/root          Root
                                    ::ct.fulcro/wrap-root?    false
                                    ::ct.fulcro/initial-state {:has-sidebar? true}}))

(ws/defcard main-container-without-sidebar
            (ct.fulcro/fulcro-card {::ct.fulcro/root          Root
                                    ::ct.fulcro/wrap-root?    false
                                    ::ct.fulcro/initial-state {:has-sidebar? false}}))
