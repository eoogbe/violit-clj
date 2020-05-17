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
(ns violit.ui.app.app-bar
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [violit.model.session :as session]
    [violit.ui.account.account-dropdown :as account-dropdown]
    [violit.ui.account.signup-form :as signup-form]
    [violit.ui.auth.login-form :as login-form]
    [violit.ui.core.link :as link]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]))

(defsc AppBar [_this {:ui/keys [account-dropdown current-credentials]}]
  {:query         [{:ui/account-dropdown (comp/get-query account-dropdown/AccountDropdown)}
                   {[:ui/current-credentials '_] (comp/get-query session/Credentials)}]
   :ident         (fn [] [:component/id ::AppBar])
   :initial-state {:ui/account-dropdown {}}
   :css           [[:.app-bar {:display          "flex"
                               :align-items      "center"
                               :width            "100%"
                               :height           "3.5em"
                               :padding          [[0 theme/spacing-lg]]
                               :background-color theme/white}]
                   [:.title {:font-size      "1.5rem"
                             :font-family    "'Montserrat', sans-serif"
                             :font-weight    400
                             :flex           1
                             :margin         0
                             :letter-spacing "0.15px"}]
                   [:.title-link {:color  theme/primary-color
                                  :cursor "pointer"}]
                   [:.signup-link styles/button-primary]
                   [:.login-link (merge styles/button-secondary
                                        {:margin-left theme/spacing-md})]]}
  (dom/header
    :.app-bar
    (dom/h1
      :.title
      (link/ui-link :.title-link {:route [""]} "Violit"))
    (if (session/logged-in? current-credentials)
      (account-dropdown/ui-account-dropdown account-dropdown)
      (dom/nav
        (link/ui-link
          :.signup-link
          {:route-targets [signup-form/SignupForm]
           :save-route?   false}
          "Sign up")
        (link/ui-link
          :.login-link
          {:route-targets [login-form/LoginForm]
           :save-route?   false}
          "Log in")))))

(def ui-app-bar (comp/factory AppBar))
