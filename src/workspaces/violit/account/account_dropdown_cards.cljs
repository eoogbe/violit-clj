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
(ns violit.account.account-dropdown-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.model.session :as session]
    [violit.ui.account.account-dropdown :as account-dropdown]
    [violit.ui.core.dropdown-menu :as dropdown-menu]
    [violit.ui.core.styles :as styles]))

(defsc Root [_this {:keys [account-dropdown]}]
  {:query         [{:account-dropdown (comp/get-query account-dropdown/AccountDropdown)}
                   {:ui/current-credentials (comp/get-query session/Credentials)}]
   :initial-state {:account-dropdown       {}
                   :ui/current-credentials {:id       "account-dropdown-account1"
                                            :username "fred-astaire"
                                            :status   :success}}
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root styles/body]]
   :css-include   [dropdown-menu/DropdownMenu]}
  (dom/div
    :.root
    (account-dropdown/ui-account-dropdown account-dropdown)
    (inj/style-element {:component Root})))

(ws/defcard account-dropdown
            (ct.fulcro/fulcro-card {::ct.fulcro/root       Root
                                    ::ct.fulcro/wrap-root? false}))
