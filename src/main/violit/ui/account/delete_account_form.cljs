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
(ns violit.ui.account.delete-account-form
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [goog.object :as gobj]
    [violit.lib.routing :as routing]
    [violit.model.account :as model]
    [violit.model.session :as session]
    [violit.schema.account :as account]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]
    [violit.ui.dialog.dialog :as dialog]))

(defn focus-username-input
  [this]
  (when-let [username-input-el (gobj/get this "username-input-el")]
    (.focus username-input-el)))

(defsc DeleteAccountForm [this {:ui/keys [username current-credentials]} {:keys [close]}]
  {:query             [:ui/username
                       {[:ui/current-credentials '_] (comp/get-query session/Credentials)}]
   :ident             (fn [] [:component/id ::DeleteAccountForm])
   :initial-state     {}
   :pre-merge         (fn [{:keys [current-normalized data-tree]}]
                        (let [defaults {:ui/username ""}]
                          (merge
                            defaults
                            current-normalized
                            data-tree)))
   :initLocalState    (fn [this _props]
                        {:save-username-input-ref #(gobj/set this "username-input-el" %)})
   :componentDidMount (fn [this]
                        (focus-username-input this))
   :css               [[:.form (merge styles/form {:width "30em"})]
                       [:#delete-account-heading styles/page-heading]
                       [:#delete-account-description {:margin        0
                                                      :margin-bottom theme/spacing-lg}]
                       [:.field-list styles/list-reset]
                       [:.input styles/input]
                       [:.submit-button (merge styles/button-submit
                                               {:background-color theme/error-color})
                        [:&:disabled styles/button-disabled]]]}
  (let [{:keys [save-username-input-ref]} (comp/get-state this)
        disabled? (not= username (::account/username current-credentials))
        handle-delete-account (fn [evt]
                                (evt/prevent-default! evt)
                                (comp/transact! this [(model/delete-account {})])
                                (uism/trigger! this ::session/sessionsm :event/log-out)
                                (routing/replace-route! this {:route [""]}))]
    (dialog/ui-dialog
      (comp/computed {:aria-describedby "delete-account-description"
                      :aria-labelledby  "delete-account-heading"}
                     {:close close})
      (dom/form
        :.form
        {:onSubmit handle-delete-account}
        (dom/h2 :#delete-account-heading "Confirm delete account")
        (dom/p :#delete-account-description
               "Enter your username to permanently delete your account. This cannot be undone.")
        (dom/ul
          :.field-list
          (dom/li
            (dom/label {:htmlFor "delete-account-username"} "Username")
            (dom/input :.input#delete-account-username
                       {:aria-required "true"
                        :autoComplete  "username"
                        :onChange      #(m/set-string! this :ui/username :event %)
                        :ref           save-username-input-ref
                        :type          "text"
                        :value         username})))
        (dom/button
          :.submit-button
          {:disabled disabled? :type "submit"}
          "Delete Account")))))

(def ui-delete-account-form (comp/computed-factory DeleteAccountForm))
