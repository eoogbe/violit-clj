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
(ns violit.ui.account.signup-form
  (:require
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [goog.functions :as gf]
    [violit.model.account :as model]
    [violit.model.session :as session]
    [violit.schema.account :as account]
    [violit.ui.core.main-container :as main-container]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.title :as title]))

(defn validate-username
  [{::account/keys [username has-username?]}]
  (cond
    (empty? username) "Required."
    (< (count username) 2) "Must be at least 2 characters long."
    (not (re-matches #"^[\w-]+$" username)) "Can only have letters, numbers, '-', or '_'."
    (not (re-matches #"^[A-Za-z][\w-]+" username)) "Must start with a letter."
    has-username? "Already taken."))

(defn validate-password
  [{::account/keys [password]}]
  (cond
    (empty? password) "Required."
    (< (count password) 8) "Must be at least 8 characters long."))

(defn validate-password-confirmation
  [{::account/keys [password password-confirmation]}]
  (cond
    (empty? password-confirmation) "Required."
    (not= password-confirmation password) "Must match password."))

(def check-if-has-username
  (letfn [(load-has-username [app-or-comp username]
            (df/load! app-or-comp ::account/has-username? nil
                      {:params {::account/username username}
                       :target (model/signup-form-path ::account/has-username?)
                       :marker false}))]
    (gf/debounce load-has-username 500)))

(defsc SignupForm [this {::account/keys [username password password-confirmation]
                         :ui/keys       [signup-result busy? loaded? debug?]
                         :as            props}]
  {:query             [::account/username
                       ::account/password
                       ::account/password-confirmation
                       ::account/has-username?
                       :ui/signup-result
                       :ui/busy?
                       :ui/loaded?
                       :ui/debug?
                       fs/form-config-join
                       (uism/asm-ident ::session/sessionsm)]
   :ident             (fn [] (model/signup-form-path))
   :form-fields       #{::account/username ::account/password ::account/password-confirmation}
   :initial-state     {:ui/debug? :param/debug?}
   :pre-merge         (fn [{:keys [current-normalized data-tree]}]
                        (let [defaults {::account/username              ""
                                        ::account/password              ""
                                        ::account/password-confirmation ""}]
                          (merge
                            defaults
                            current-normalized
                            (fs/add-form-config SignupForm data-tree))))
   :route-segment     ["signup"]
   :will-enter        (fn [app _params]
                        (let [ident (comp/get-ident SignupForm {})]
                          (merge/merge-component!
                            app SignupForm
                            {::account/username              ""
                             ::account/password              ""
                             ::account/password-confirmation ""
                             :ui/signup-result               {::model/signup-error false}
                             :ui/loaded?                     false})
                          (comp/transact! app [(fs/clear-complete! {:entity-ident ident})])
                          (dr/route-immediate ident)))
   :componentDidMount (fn [this]
                        (let [{:ui/keys [debug?]} (comp/props this)]
                          (when-not debug?
                            (session/redirect-if-authenticated this))
                          (title/set-title! "Signup")))
   :css               [[:.form styles/form]
                       [:.heading styles/page-heading]
                       [:.field-list styles/list-reset]
                       [:.input styles/input]
                       [:.form-error styles/form-error]
                       [:.submit-button styles/button-submit
                        [:&:disabled styles/button-disabled]]]}
  (let [get-error (fn [field validate]
                    (and
                      (not busy?)
                      (fs/checked? props field)
                      (validate props)))
        username-error (get-error ::account/username validate-username)
        password-error (get-error ::account/password validate-password)
        password-confirmation-error (get-error ::account/password-confirmation
                                               validate-password-confirmation)

        {::model/keys [signup-error]} signup-result

        disabled? (or
                    busy?
                    (not (fs/dirty? props))
                    (validate-username props)
                    (validate-password props)
                    (validate-password-confirmation props))

        mk-handle-blur
        (fn [field]
          #(when-not busy?
             (comp/transact! this [(fs/mark-complete! {:field field})])))

        mk-handle-change
        (fn [field]
          #(when-not busy?
             (m/set-string! this field :event %)))

        handle-username-change
        (fn [evt]
          (when-not busy?
            (let [value (evt/target-value evt)]
              (m/set-string! this ::account/username :value value)
              (when-not (validate-username {::account/username value})
                (check-if-has-username this value)))))

        handle-sign-up
        (fn [evt]
          (evt/prevent-default! evt)
          (comp/transact! this [(model/create-account
                                  {:diff (fs/dirty-fields props false)})]))]
    (when (or loaded? debug?)
      (main-container/ui-main-container
        {}
        (dom/form
          :.form
          {:aria-describedby "signup-error"
           :onSubmit         handle-sign-up}
          (dom/h2 :.heading "Signup")
          (when signup-error
            (dom/p :.form-error#signup-error signup-error))
          (dom/ul
            :.field-list
            (dom/li
              (dom/label {:htmlFor "signup-username"} "Username")
              (dom/input
                :.input#signup-username
                {:aria-describedby (when username-error "signup-username-error")
                 :aria-required    "true"
                 :autoComplete     "username"
                 :autoFocus        true
                 :onBlur           (mk-handle-blur ::account/username)
                 :onChange         handle-username-change
                 :type             "text"
                 :value            username})
              (when username-error
                (dom/p :.form-error#signup-username-error username-error)))
            (dom/li
              (dom/label {:htmlFor "signup-password"} "Password")
              (dom/input
                :.input#signup-password
                {:aria-describedby (when password-error "signup-password-error")
                 :aria-required    "true"
                 :autoComplete     "new-password"
                 :onBlur           (mk-handle-blur ::account/password)
                 :onChange         (mk-handle-change ::account/password)
                 :type             "password"
                 :value            password})
              (when password-error
                (dom/p :.form-error#signup-password-error password-error)))
            (dom/li
              (dom/label {:htmlFor "signup-password-confirmation"} "Password confirmation")
              (dom/input
                :.input#signup-password-confirmation
                {:aria-describedby (when password-confirmation-error
                                     "signup-password-confirmation-error")
                 :aria-required    "true"
                 :autoComplete     "new-password"
                 :onBlur           (mk-handle-blur ::account/password-confirmation)
                 :onChange         (mk-handle-change ::account/password-confirmation)
                 :type             "password"
                 :value            password-confirmation})
              (when password-confirmation-error
                (dom/p :.form-error#signup-password-confirmation-error password-confirmation-error))))
          (dom/button
            :.submit-button
            {:disabled disabled? :type "submit"}
            "Sign up"))))))

(def ui-signup-form (comp/factory SignupForm))
