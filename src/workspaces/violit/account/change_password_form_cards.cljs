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
(ns violit.account.change-password-form-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.ui.account.change-password-form :as change-password-form]
    [violit.ui.core.styles :as styles]
    [violit.ui.dialog.dialog :as dialog]))

(defsc Root [this {:keys [change-password-form open?]}]
  {:query         [:open?
                   {:change-password-form (comp/get-query change-password-form/ChangePasswordForm)}]
   :ident         (fn [] [:card/id ::ChangePasswordForm])
   :initial-state {:open?                true
                   :change-password-form {}}
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root styles/body]
                   [:#modal-root styles/body]]
   :css-include   [dialog/Dialog]}
  (dom/div
    :.root
    (dom/button {:onClick #(m/set-value! this :open? true) :type "button"} "Open")
    (when open?
      (change-password-form/ui-change-password-form
        change-password-form
        {:close #(m/set-value! this :open? false)}))
    (inj/style-element {:component Root})))

(ws/defcard change-password-form
            (ct.fulcro/fulcro-card {::ct.fulcro/root Root}))
