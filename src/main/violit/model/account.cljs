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
(ns violit.model.account
  (:require
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [taoensso.timbre :as log]
    [violit.logger :as logger]
    [violit.model.session :as session]
    [violit.schema.account :as account]
    [violit.ui.core.loading-indicator :as loading-indicator]))

(defn signup-form-path
  ([] [:component/id ::SignupForm])
  ([field] [:component/id ::SignupForm field]))

(defn create-account*
  [state]
  (let [signup-form-class (comp/registry-key->class :violit.ui.account.signup-form/SignupForm)]
    (-> state
        (loading-indicator/set-visible* true)
        (merge/merge-component signup-form-class {:ui/busy? true}))))

(defmutation create-account [{:keys [diff] :as params}]
  (action [{:keys [state]}]
          (logger/log-entity "Creating account for" params)
          (swap! state create-account*))
  (ok-action [{:keys [state ref component]}]
             (let [{::account/keys [username password]} (-> diff vals first)
                   {::keys [signup-error]} (get-in @state (conj ref :ui/signup-result))]
               (swap! state assoc-in (conj ref :ui/busy?) false)
               (if signup-error
                 (swap! state loading-indicator/set-visible* false)
                 (do
                   (session/set-unauthenticated-callbacks! component)
                   (uism/trigger! component
                                  ::session/sessionsm
                                  :event/log-in
                                  {:username username
                                   :password password})))))
  (remote [env]
          (m/with-target env (signup-form-path :ui/signup-result))))

(defn open-delete-account-form*
  [state profile-ident]
  (let [delete-account-form-class (comp/registry-key->class
                                    :violit.ui.account.delete-account-form/DeleteAccountForm)]
    (-> state
        (merge/merge-component delete-account-form-class
                               {:ui/username ""}
                               :replace (conj profile-ident :ui/delete-account-form))
        (assoc-in (conj profile-ident :ui/open-delete-account-form?) true))))

(defmutation open-delete-account-form [_params]
  (action [{:keys [state ref]}]
          (swap! state open-delete-account-form* ref)))

(defmutation delete-account [_params]
  (action [_env] (log/info "Permanently deleting current account"))
  (remote [_env] true))

(defn change-password-form-path
  ([] [:component/id ::ChangePasswordForm])
  ([field] [:component/id ::ChangePasswordForm field]))

(defn open-change-password-form*
  [state profile-ident]
  (let [change-password-form-class (comp/registry-key->class
                                     :violit.ui.account.change-password-form/ChangePasswordForm)]
    (-> state
        (merge/merge-component
          change-password-form-class
          {::account/old-password          ""
           ::account/new-password          ""
           ::account/password-confirmation ""
           :ui/change-password-result      {::change-password-error false}}
          :replace (conj profile-ident :ui/change-password-form))
        (fs/clear-complete* (change-password-form-path))
        (assoc-in (conj profile-ident :ui/open-change-password-form?) true))))

(defmutation open-change-password-form [_params]
  (action [{:keys [state ref]}]
          (swap! state open-change-password-form* ref)))

(defn update-password*
  [state]
  (let [change-password-form-class (comp/registry-key->class
                                     :violit.ui.account.change-password-form/ChangePasswordForm)]
    (log/info "Updating current account password")
    (-> state
        (loading-indicator/set-visible* true)
        (merge/merge-component change-password-form-class {:ui/busy? true}))))

(defn finish-update-password*
  [state change-password-form-ident]
  (let [{::keys [change-password-error]}
        (get-in state (conj change-password-form-ident :ui/change-password-result))

        {::account/keys [username]} (get-in state (session/credentials-path))

        open-change-password-form-path
        (account/account-path username :ui/open-change-password-form?)]
    (if change-password-error
      (-> state
          (update-in change-password-form-ident merge {::account/old-password ""
                                                       :ui/busy?              false})
          (loading-indicator/set-visible* false))
      (-> state
          (assoc-in open-change-password-form-path false)
          (assoc-in (conj change-password-form-ident :ui/busy?) false)
          (loading-indicator/set-visible* false)))))

(defmutation update-password [_params]
  (action [{:keys [state]}]
          (swap! state update-password*))
  (ok-action [{:keys [state ref]}]
             (swap! state finish-update-password* ref))
  (remote [env]
          (m/with-target env (change-password-form-path :ui/change-password-result))))

(defn close-pronouns-form*
  [state {:keys [username]}]
  (log/info "Closing pronouns form")
  (-> state
      (fs/pristine->entity* (account/account-path username))
      (update-in (account/account-path username) dissoc :ui/pronouns-form)))

(defmutation close-pronouns-form [params]
  (action [{:keys [state]}]
          (swap! state close-pronouns-form* params)))

(defn open-pronouns-form*
  [state {:keys [username]}]
  (log/info "Opening pronouns form")
  (let [pronouns-form-class (comp/registry-key->class :violit.ui.account.pronouns-form/PronounsForm)
        pronouns-form-ident (account/account-path username)]
    (-> state
        (fs/add-form-config* pronouns-form-class pronouns-form-ident)
        (fs/mark-complete* pronouns-form-ident)
        (assoc-in (account/account-path username :ui/pronouns-form) pronouns-form-ident))))

(defmutation open-pronouns-form [params]
  (action [{:keys [state]}]
          (swap! state open-pronouns-form* params)))

(defn update-pronouns*
  [state {:keys [username diff] :as params}]
  (log/info "Updating pronouns" params)
  (let [new-pronouns (-> diff first second ::account/pronouns :after account/mk-pronouns)]
    (-> state
        (fs/entity->pristine* (account/account-path username))
        (update-in (account/account-path username)
                   #(-> %
                        (dissoc :ui/pronouns-form)
                        (assoc ::account/pronouns new-pronouns))))))

(defmutation update-pronouns [params]
  (action [{:keys [state]}]
          (swap! state update-pronouns* params))
  (remote [_env] true))
