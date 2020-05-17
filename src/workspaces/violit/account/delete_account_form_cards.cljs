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
(ns violit.account.delete-account-form-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.model.session :as session]
    [violit.ui.account.delete-account-form :as delete-account-form]
    [violit.ui.core.styles :as styles]
    [violit.ui.dialog.dialog :as dialog]))

(defsc Launcher [this {:keys [delete-account-form open?]}]
  {:query         [:open?
                   {:delete-account-form
                    (comp/get-query delete-account-form/DeleteAccountForm)}]
   :ident         (fn [] [:card/id ::DeleteAccountForm])
   :initial-state {:open?               true
                   :delete-account-form {}}}
  (dom/div
    (dom/button {:onClick #(m/set-value! this :open? true) :type "button"} "Open")
    (when open?
      (delete-account-form/ui-delete-account-form
        delete-account-form
        {:close #(m/set-value! this :open? false)}))))

(def ui-launcher (comp/factory Launcher))

(defsc Root [_this {:keys [launcher]}]
  {:query         [:open?
                   {:launcher (comp/get-query Launcher)}
                   {:ui/current-credentials (comp/get-query session/Credentials)}]
   :initial-state {:launcher               {}
                   :ui/current-credentials {:id       "delete-account-form-account1"
                                            :username "fred-astaire"
                                            :status   :success}}
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root styles/body]
                   [:#modal-root styles/body]]
   :css-include   [dialog/Dialog]}
  (dom/div
    :.root
    (ui-launcher launcher)
    (inj/style-element {:component Root})))

(ws/defcard delete-account-form
            (ct.fulcro/fulcro-card {::ct.fulcro/root       Root
                                    ::ct.fulcro/wrap-root? false}))
