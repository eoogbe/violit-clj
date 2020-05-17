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
(ns violit.ui.account.account-dropdown
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom.icons :as icons]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [goog.object :as gobj]
    [violit.lib.routing :as routing]
    [violit.model.session :as session]
    [violit.schema.account :as account]
    [violit.ui.account.profile :as profile]
    [violit.ui.core.dropdown-menu :as dropdown-menu]
    [violit.ui.core.click-outside :as click-outside]
    [violit.ui.core.theme :as theme]))

(defsc AccountDropdown [this {:ui/keys [open? current-credentials]}]
  {:query                [:ui/open?
                          {[:ui/current-credentials '_] (comp/get-query session/Credentials)}]
   :ident                (fn [] [:component/id ::AccountDropdown])
   :initial-state        {}
   :initLocalState       (fn [this _props]
                           {:save-dropdown-ref #(gobj/set this "dropdown-el" %)})
   :componentDidMount    (fn [this]
                           (let [dropdown-el (gobj/get this "dropdown-el")
                                 close-menu #(m/set-value! this :ui/open? false)
                                 cleanup (click-outside/click-outside dropdown-el close-menu)]
                             (comp/update-state! this assoc :cleanup cleanup)))
   :componentWillUnmount (fn [this]
                           (let [{:keys [cleanup]} (comp/get-state this)]
                             (cleanup)))
   :css                  [[:.account-dropdown {:position "relative"}]
                          [:.toggle {:font-size       "1rem"
                                     :font-family     theme/body-font
                                     :display         "flex"
                                     :justify-content "space-between"
                                     :align-items     "center"
                                     :width           "100%"
                                     :padding         [[theme/spacing-sm theme/spacing-md]]
                                     :border          [["1px" "solid" theme/gray200]]
                                     :border-radius   theme/border-radius
                                     :background      "none"
                                     :color           theme/black
                                     :cursor          "pointer"}]]}
  (let [{:keys [save-dropdown-ref]} (comp/get-state this)
        {::account/keys [username]} current-credentials]
    (dom/nav
      :.account-dropdown
      {:ref save-dropdown-ref}
      (dom/button
        :.toggle
        {:aria-controls "dropdown-menu-account"
         :aria-expanded (str open?)
         :aria-haspopup "true"
         :onClick       #(m/toggle! this :ui/open?)
         :type          "button"}
        username
        (if open?
          (icons/ui-icon {:icon  :arrow-drop-up
                          :title "Close menu"})
          (icons/ui-icon {:icon  :arrow-drop-down
                          :title "Open menu"})))
      (dropdown-menu/ui-dropdown-menu
        {:id        "dropdown-menu-account"
         :items     [{:icon     :account-box
                      :on-click #(routing/push-route!
                                   this
                                   {:route-targets [profile/Profile username]})
                      :text     "Profile"}

                     {:icon     :exit-to-app
                      :on-click #(uism/trigger! this ::session/sessionsm :event/log-out)
                      :text     "Log out"}]
         :open?     open?
         :placement "bottom"}
        {:close #(m/set-value! this :ui/open? false)}))))

(def ui-account-dropdown (comp/factory AccountDropdown))
