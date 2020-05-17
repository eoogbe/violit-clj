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
(ns violit.app.app-bar-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.model.session :as session]
    [violit.ui.app.app-bar :as app-bar]
    [violit.ui.core.dropdown-menu :as dropdown-menu]
    [violit.ui.core.styles :as styles]))

(def USERNAME "fred-astaire")

(defsc Root [_this {:keys [app-bar]}]
  {:query         [{:app-bar (comp/get-query app-bar/AppBar)}
                   {:ui/current-credentials (comp/get-query session/Credentials)}]
   :initial-state (fn [{:keys [credentials]}]
                    {:app-bar
                     (comp/get-initial-state app-bar/AppBar {})

                     :ui/current-credentials
                     (comp/get-initial-state session/Credentials credentials)})
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root styles/body]]
   :css-include   [dropdown-menu/DropdownMenu]}
  (dom/div
    :.root
    (app-bar/ui-app-bar app-bar)
    (inj/style-element {:component Root})))

(ws/defcard logged-in-app-bar
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:credentials {:id       "app-bar-account1"
                                                        :username USERNAME
                                                        :status   :success}}}))

(ws/defcard logged-out-app-bar
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:credentials {:status :logged-out}}}))
