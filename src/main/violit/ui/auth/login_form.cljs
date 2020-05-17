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
(ns violit.ui.auth.login-form
  (:require
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [violit.model.session :as session]
    [violit.schema.account :as account]
    [violit.ui.core.main-container :as main-container]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.title :as title]))

(defn validate-username
  [{::account/keys [username]}]
  (when (empty? username)
    "Required."))

(defn validate-password
  [{::account/keys [password]}]
  (when (empty? password)
    "Required."))

(defsc LoginForm [this {::account/keys [username password]
                        :ui/keys       [login-error loaded? debug?]
                        :as            props}]
  {:query             [::account/username
                       ::account/password
                       :ui/login-error
                       :ui/loaded?
                       :ui/debug?
                       fs/form-config-join
                       (uism/asm-ident ::session/sessionsm)
                       {[:ui/current-credentials '_] (comp/get-query session/Credentials)}]
   :ident             (fn [] [:component/id ::LoginForm])
   :form-fields       #{::account/username ::account/password}
   :initial-state     {:ui/debug? :param/debug?}
   :pre-merge         (fn [{:keys [current-normalized data-tree]}]
                        (let [defaults {::account/username ""
                                        ::account/password ""}]
                          (merge
                            defaults
                            current-normalized
                            (fs/add-form-config LoginForm data-tree))))
   :route-segment     ["login"]
   :will-enter        (fn [app _params]
                        (let [ident (comp/get-ident LoginForm {})]
                          (merge/merge-component! app LoginForm {::account/username ""
                                                                 ::account/password ""
                                                                 :ui/loaded?        false})
                          (comp/transact! app [(fs/clear-complete! {:entity-ident ident})])
                          (dr/route-immediate ident)))
   :componentDidMount (fn [this]
                        (let [{:ui/keys [debug?]} (comp/props this)]
                          (when-not debug?
                            (session/redirect-if-authenticated this))
                          (title/set-title! "Login")))
   :css               [[:.form styles/form]
                       [:.heading styles/page-heading]
                       [:.field-list styles/list-reset]
                       [:.input styles/input]
                       [:.form-error styles/form-error]
                       [:.submit-button styles/button-submit
                        [:&:disabled styles/button-disabled]]]}
  (let [busy? (session/busy? this)

        get-error (fn [field validate]
                    (and
                      (not busy?)
                      (fs/checked? props field)
                      (validate props)))
        username-error (get-error ::account/username validate-username)
        password-error (get-error ::account/password validate-password)

        disabled? (or
                    busy?
                    (not (fs/dirty? props))
                    (validate-username props)
                    (validate-password props))

        mk-handle-blur
        (fn [field]
          #(when-not busy?
             (comp/transact! this [(fs/mark-complete! {:field field})])))

        mk-handle-change
        (fn [field]
          #(when-not busy?
             (uism/set-string! this ::session/sessionsm field %)))

        handle-log-in
        (fn [evt]
          (evt/prevent-default! evt)
          (session/set-unauthenticated-callbacks! this)
          (uism/trigger! this
                         ::session/sessionsm
                         :event/log-in
                         {:username username
                          :password password}))]
    (when (or loaded? debug?)
      (main-container/ui-main-container
        {}
        (dom/form
          :.form
          {:aria-describedby "login-error"
           :onSubmit         handle-log-in}
          (dom/h2 :.heading "Login")
          (when login-error
            (dom/p :.form-error#login-error login-error))
          (dom/ul
            :.field-list
            (dom/li
              (dom/label {:htmlFor "login-username"} "Username")
              (dom/input
                :.input#login-username
                {:aria-describedby (when username-error "login-username-error")
                 :aria-required    "true"
                 :autoComplete     "username"
                 :autoFocus        true
                 :onBlur           (mk-handle-blur ::account/username)
                 :onChange         (mk-handle-change :username)
                 :type             "text"
                 :value            username})
              (when username-error
                (dom/p :.form-error#login-username-error username-error)))
            (dom/li
              (dom/label {:htmlFor "login-password"} "Password")
              (dom/input
                :.input#login-password
                {:aria-describedby (when password-error "login-password-error")
                 :aria-required    "true"
                 :autoComplete     "current-password"
                 :onBlur           (mk-handle-blur ::account/password)
                 :onChange         (mk-handle-change :password)
                 :type             "password"
                 :value            password})
              (when password-error
                (dom/p :.form-error#login-password-error password-error))))
          (dom/button
            :.submit-button
            {:disabled disabled? :type "submit"}
            "Log in"))))))

(def ui-login-form (comp/factory LoginForm))
