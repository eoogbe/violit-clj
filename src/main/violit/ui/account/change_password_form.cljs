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
(ns violit.ui.account.change-password-form
  (:require
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [goog.object :as gobj]
    [violit.model.account :as model]
    [violit.schema.account :as account]
    [violit.ui.core.styles :as styles]
    [violit.ui.dialog.dialog :as dialog]))

(defn focus-old-password-input
  [this]
  (when-let [old-password-el (gobj/get this "old-password-input-el")]
    (.focus old-password-el)))

(defn validate-old-password
  [{::account/keys [old-password]}]
  (cond
    (empty? old-password) "Required."))

(defn validate-new-password
  [{::account/keys [new-password]}]
  (cond
    (empty? new-password) "Required."
    (< (count new-password) 8) "Must be at least 8 characters long."))

(defn validate-password-confirmation
  [{::account/keys [new-password password-confirmation]}]
  (cond
    (empty? password-confirmation) "Required."
    (not= password-confirmation new-password) "Must match password."))

(defsc ChangePasswordForm [this
                           {::account/keys [old-password new-password password-confirmation]
                            :ui/keys       [change-password-result busy?]
                            :as            props}
                           {:keys [close]}]
  {:query             [::account/old-password
                       ::account/new-password
                       ::account/password-confirmation
                       :ui/change-password-result
                       :ui/busy?
                       fs/form-config-join]
   :ident             (fn [] (model/change-password-form-path))
   :form-fields       #{::account/old-password
                        ::account/new-password
                        ::account/password-confirmation}
   :initial-state     {}
   :pre-merge         (fn [{:keys [current-normalized data-tree]}]
                        (let [defaults {::account/old-password          ""
                                        ::account/new-password          ""
                                        ::account/password-confirmation ""}]
                          (merge
                            defaults
                            current-normalized
                            (fs/add-form-config ChangePasswordForm data-tree))))
   :initLocalState    (fn [this _props]
                        {:save-old-password-input-ref #(gobj/set this "old-password-input-el" %)})
   :componentDidMount (fn [this]
                        (focus-old-password-input this))
   :css               [[:.form (merge styles/form {:justify-content "center"
                                                   :width           "30em"
                                                   :height          "22em"})]
                       [:#change-password-heading styles/page-heading]
                       [:.field-list styles/list-reset]
                       [:.input styles/input]
                       [:.form-error styles/form-error]
                       [:.submit-button styles/button-submit
                        [:&:disabled styles/button-disabled]]]}
  (let [{:keys [save-old-password-input-ref]} (comp/get-state this)

        get-error (fn [field validate]
                    (and
                      (not busy?)
                      (fs/checked? props field)
                      (validate props)))
        old-password-error (get-error ::account/old-password validate-old-password)
        new-password-error (get-error ::account/new-password validate-new-password)
        password-confirmation-error (get-error ::account/password-confirmation
                                               validate-password-confirmation)

        {::model/keys [change-password-error]} change-password-result

        disabled? (or
                    busy?
                    (not (fs/dirty? props))
                    (validate-old-password props)
                    (validate-new-password props)
                    (validate-password-confirmation props))

        mk-handle-blur (fn [field]
                         #(when-not busy?
                            (comp/transact! this [(fs/mark-complete! {:field field})])))
        mk-handle-change (fn [field]
                           #(when-not busy?
                              (m/set-string! this field :event %)))

        handle-change-password (fn [evt]
                                 (evt/prevent-default! evt)
                                 (when-not busy?
                                   (comp/transact! this [(model/update-password
                                                           {:old-password old-password
                                                            :new-password new-password})])
                                   (focus-old-password-input this)))]
    (dialog/ui-dialog
      (comp/computed {:aria-labelledby "change-password-heading"}
                     {:close close})
      (dom/form
        :.form
        {:aria-describedby "change-password-error"
         :onSubmit         handle-change-password}
        (dom/h2 :#change-password-heading "Password change")
        (when change-password-error
          (dom/p :.form-error#change-password-error change-password-error))
        (dom/ul
          :.field-list
          (dom/li
            (dom/label {:htmlFor "change-password-old-password"} "Old password")
            (dom/input
              :.input#change-password-old-password
              {:aria-describedby (when old-password-error "change-password-old-password-error")
               :aria-required    "true"
               :autoComplete     "current-password"
               :onBlur           (mk-handle-blur ::account/old-password)
               :onChange         (mk-handle-change ::account/old-password)
               :ref              save-old-password-input-ref
               :type             "password"
               :value            old-password})
            (when old-password-error
              (dom/p :.form-error#change-password-old-password-error old-password-error)))
          (dom/li
            (dom/label {:htmlFor "change-password-new-password"} "New password")
            (dom/input
              :.input#change-password-new-password
              {:aria-describedby (when new-password-error "change-password-new-password-error")
               :aria-required    "true"
               :autoComplete     "new-password"
               :onBlur           (mk-handle-blur ::account/new-password)
               :onChange         (mk-handle-change ::account/new-password)
               :type             "password"
               :value            new-password})
            (when new-password-error
              (dom/p :.form-error#change-password-new-password-error new-password-error)))
          (dom/li
            (dom/label
              {:htmlFor "change-password-password-confirmation"}
              "New password confirmation")
            (dom/input
              :.input#change-password-password-confirmation
              {:aria-describedby (when password-confirmation-error
                                   "change-password-password-confirmation-error")
               :aria-required    "true"
               :autoComplete     "new-password"
               :onBlur           (mk-handle-blur ::account/password-confirmation)
               :onChange         (mk-handle-change ::account/password-confirmation)
               :type             "password"
               :value            password-confirmation})
            (when password-confirmation-error
              (dom/p :.form-error#change-password-password-confirmation-error
                     password-confirmation-error))))
        (dom/button
          :.submit-button
          {:disabled disabled? :type "submit"}
          "Change password")))))

(def ui-change-password-form (comp/computed-factory ChangePasswordForm))
