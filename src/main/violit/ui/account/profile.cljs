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
(ns violit.ui.account.profile
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.dom.icons :as icons]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [violit.model.account :as model]
    [violit.model.session :as session]
    [violit.schema.account :as account]
    [violit.ui.account.change-password-form :as change-password-form]
    [violit.ui.account.delete-account-form :as delete-account-form]
    [violit.ui.account.pronouns-form :as pronouns-form]
    [violit.ui.core.main-container :as main-container]
    [violit.ui.core.not-found-page :as not-found-page]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]
    [violit.ui.core.time-ago :as time-ago]
    [violit.ui.core.title :as title]))

(defn ui-pronouns
  [{:keys [pronouns]}]
  (if pronouns
    (dom/span "Pronouns: " pronouns)
    "No pronouns specified"))

(defsc Profile [this {::account/keys [username created-at pronouns]
                      :ui/keys       [pronouns-form
                                      open-change-password-form?
                                      change-password-form
                                      open-delete-account-form?
                                      delete-account-form
                                      current-credentials]}]
  {:query             [::account/username
                       ::account/created-at
                       ::account/pronouns
                       {:ui/pronouns-form (comp/get-query pronouns-form/PronounsForm)}
                       :ui/open-change-password-form?
                       {:ui/change-password-form
                        (comp/get-query change-password-form/ChangePasswordForm)}
                       :ui/open-delete-account-form?
                       {:ui/delete-account-form
                        (comp/get-query delete-account-form/DeleteAccountForm)}
                       {[:ui/current-credentials '_] (comp/get-query session/Credentials)}]
   :ident             ::account/username
   :initial-state     {::account/username   :param/username
                       ::account/created-at :param/created-at
                       ::account/pronouns   :param/pronouns
                       :ui/pronouns-form    :param/pronouns-form}
   :pre-merge         (fn [{:keys [current-normalized data-tree]}]
                        (let [defaults {:ui/change-password-form
                                        (comp/get-initial-state
                                          change-password-form/ChangePasswordForm {})

                                        :ui/delete-account-form
                                        (comp/get-initial-state
                                          delete-account-form/DeleteAccountForm {})}]
                          (merge
                            defaults
                            current-normalized
                            data-tree)))
   :route-segment     ["users" ::account/username]
   :will-enter        (fn [app {::account/keys [username] :as params}]
                        (let [ident (comp/get-ident Profile params)]
                          (comp/transact! app [(model/close-pronouns-form {:username username})])
                          (dr/route-deferred ident
                                             #(df/load! app ident Profile
                                                        {:post-mutation        `dr/target-ready
                                                         :post-mutation-params {:target ident}
                                                         :marker               false}))))
   :componentDidMount (fn [this]
                        (let [{::account/keys [username]} (comp/props this)]
                          (title/set-title! username)))
   :css               [[:.username styles/page-heading]
                       [:.joined-at {:margin        0
                                     :margin-bottom theme/spacing-md}]
                       [:.pronouns-container {:display     "flex"
                                              :align-items "center"
                                              :margin-top  "-0.5em"}]
                       [:.edit-pronouns-button styles/button-tertiary]
                       [:.edit-pronouns-icon {:fill theme/primary-color}]
                       [:.change-password-button
                        (merge
                          styles/link
                          {:display "block"
                           :margin  [[theme/spacing-xl 0 theme/spacing-md]]})]

                       [:.delete-account-button (merge styles/link
                                                       {:display "block"
                                                        :color   theme/error-color-dark})]]}
  (let [{:keys [edit-pronouns-icon]} (css/get-classnames Profile)
        current-user? (= username (::account/username current-credentials))]
    (main-container/ui-main-container
      {}
      (not-found-page/ui-not-found-page
        {:found? (some? created-at)}
        (dom/div
          {:itemScope true
           :itemType  "http://schema.org/ProfilePage"}
          (dom/h2 :.username {:itemProp "about"} username)
          (when created-at
            (dom/p
              :.joined-at
              "Joined "
              (time-ago/ui-time-ago {:date     created-at
                                     :itemProp "dateCreated"})))
          (if current-user?
            (if pronouns-form
              (pronouns-form/ui-pronouns-form pronouns-form)
              (dom/div
                :.pronouns-container
                (ui-pronouns {:pronouns pronouns})
                (dom/button
                  :.edit-pronouns-button
                  {:onClick #(comp/transact!
                               this [(model/open-pronouns-form {:username username})])
                   :type    "button"}
                  (icons/ui-icon {:className edit-pronouns-icon
                                  :icon      :edit
                                  :title     "Edit pronouns"}))))
            (ui-pronouns {:pronouns pronouns}))
          (when current-user?
            (comp/fragment
              (dom/button
                :.change-password-button
                {:onClick #(comp/transact! this [(model/open-change-password-form {})])
                 :type    "button"}
                "Change password")
              (dom/button
                :.delete-account-button
                {:onClick #(comp/transact! this [(model/open-delete-account-form {})])
                 :type    "button"}
                "Delete Account")))
          (when open-change-password-form?
            (change-password-form/ui-change-password-form
              change-password-form
              {:close #(m/set-value! this :ui/open-change-password-form? false)}))
          (when open-delete-account-form?
            (delete-account-form/ui-delete-account-form
              delete-account-form
              {:close #(m/set-value! this :ui/open-delete-account-form? false)})))))))

(def ui-profile (comp/factory Profile))
