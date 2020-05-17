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
(ns violit.model.session
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.ui-state-machines :as uism :refer [defstatemachine]]
    [violit.application :refer [app]]
    [violit.lib.routing :as routing]
    [violit.schema.account :as account]
    [violit.ui.core.loading-indicator :as loading-indicator]))

(defn credentials-path
  ([] [:component/id ::Credentials])
  ([field] [:component/id ::Credentials field]))

(defsc Credentials [_this _props]
  {:query         [::account/id ::account/username ::status]
   :ident         (fn [] (credentials-path))
   :initial-state {::account/id       :param/id
                   ::account/username :param/username
                   ::status           :param/status}})

(defn logged-in?
  [{:keys [::status]}]
  (= status :success))

(defn busy?
  [app-or-comp]
  (let [session-state (uism/get-active-state app-or-comp ::sessionsm)]
    (= session-state :state/checking-credentials)))

(defn unauthenticated-callbacks
  [comp]
  {:on-logged-in  #(routing/return-to-saved-route! comp [""])
   :on-logged-out (fn []
                    (m/set-value! comp :ui/loaded? true)
                    (comp/transact! comp [(loading-indicator/set-visible {:visible? false})]))})

(defn set-unauthenticated-callbacks!
  [comp]
  (uism/trigger! comp ::sessionsm :event/set-session-callbacks (unauthenticated-callbacks comp)))

(defn redirect-if-authenticated
  [comp]
  (let [session-state (uism/get-active-state comp ::sessionsm)
        {:keys [on-logged-in on-logged-out]} (unauthenticated-callbacks comp)]
    (case session-state
      (nil :initial :state/checking-credentials)
      (set-unauthenticated-callbacks! comp)

      :state/logged-out
      (do
        (on-logged-out)
        (set-unauthenticated-callbacks! comp))

      :state/logged-in
      (on-logged-in))))

(defn redirect-if-unauthenticated
  [comp login-class]
  (let [session-state (uism/get-active-state comp ::sessionsm)
        on-logged-in (fn []
                       (m/set-value! comp :ui/loaded? true)
                       (comp/transact! comp [(loading-indicator/set-visible {:visible? false})]))
        on-logged-out #(routing/replace-route! comp {:route-targets [login-class]
                                                     :save-route?   true})]
    (case session-state
      (nil :initial :state/checking-credentials)
      (uism/trigger! comp ::sessionsm :event/set-session-callbacks
                     {:on-logged-in  on-logged-in
                      :on-logged-out on-logged-out})

      :state/logged-out
      (on-logged-out)

      :state/logged-in
      (do
        (on-logged-in)
        (uism/trigger! comp ::sessionsm :event/set-session-callbacks
                       {:on-logged-in  on-logged-in
                        :on-logged-out on-logged-out})))))

(defn check-credentials
  [env]
  (-> env
      (uism/assoc-aliased :has-loading-indicator? true)
      (uism/load ::current-credentials :actor/credentials
                 {::uism/target-alias :current-credentials
                  ::uism/ok-event     :event/complete
                  ::uism/error-event  :event/failed})))

(defn process-logged-in-result
  [env]
  (when-let [on-logged-in (uism/retrieve env :on-logged-in)]
    (on-logged-in))
  (-> env
      (uism/store :on-logged-in nil)
      (uism/store :on-logged-out nil)
      (uism/assoc-aliased :has-loading-indicator? false :error nil)
      (uism/activate :state/logged-in)))

(defn process-logged-out-result
  [env error]
  (when-let [on-logged-out (uism/retrieve env :on-logged-out)]
    (on-logged-out))
  (-> env
      (uism/assoc-aliased :has-loading-indicator? false
                          :password ""
                          :error error)
      (uism/store :on-logged-in nil)
      (uism/store :on-logged-out nil)
      (uism/activate :state/logged-out)))

(defn process-credentials-result
  ([env] (process-credentials-result env {}))
  ([env {:keys [error] :or {error ""}}]
   (let [status (uism/alias-value env :status)]
     (if (= status :success)
       (process-logged-in-result env)
       (process-logged-out-result env error)))))

(defn process-login-result
  [env]
  (process-credentials-result env {:error "Username or password is incorrect"}))

(defn show-server-error
  [env]
  (uism/assoc-aliased env
                      :has-loading-indicator? false
                      :error "Oops! Something went wrong."))

(defn set-session-callbacks
  [{::uism/keys [event-data] :as env}]
  (let [{:keys [on-logged-in on-logged-out]} event-data]
    (-> env
        (uism/store :on-logged-in on-logged-in)
        (uism/store :on-logged-out on-logged-out))))

(defn log-in
  [{::uism/keys [event-data] :as env}]
  (let [{:keys [username password]} event-data]
    (-> env
        (uism/assoc-aliased :has-loading-indicator? true)
        (uism/trigger-remote-mutation
          :actor/form
          'violit.model.session/log-in
          {::account/username  username
           ::account/password  password
           ::m/returning       (uism/actor-class env :actor/credentials)
           ::uism/target-alias :current-credentials
           ::uism/ok-event     :event/login-complete
           ::uism/error-event  :event/login-failed}))))

(defn log-out
  [env]
  (-> env
      (uism/assoc-aliased :status :logged-out)
      (uism/trigger-remote-mutation
        :actor/form
        'violit.model.session/log-out
        {::m/returning       (uism/actor-class env :actor/credentials)
         ::uism/target-alias :current-credentials})))

(defstatemachine session-machine
  {::uism/actors  #{:actor/form :actor/credentials :actor/loading-indicator}
   ::uism/aliases {:username               [:actor/form ::account/username]
                   :password               [:actor/form ::account/password]
                   :error                  [:actor/form :ui/login-error]
                   :status                 [:actor/credentials ::status]
                   :current-credentials    [:actor/form :ui/current-credentials]
                   :has-loading-indicator? [:actor/loading-indicator :ui/visible?]}
   ::uism/states  {:initial
                   {::uism/events
                    {::uism/started {::uism/target-state :state/checking-credentials
                                     ::uism/handler      check-credentials}}}

                   :state/checking-credentials
                   {::uism/events
                    {:event/complete              {::uism/handler process-credentials-result}
                     :event/failed                {::uism/target-state :state/logged-out
                                                   ::uism/handler      show-server-error}
                     :event/set-session-callbacks {::uism/handler set-session-callbacks}}}

                   :state/logged-out
                   {::uism/events
                    {:event/log-in                {::uism/handler log-in}
                     :event/login-complete        {::uism/handler process-login-result}
                     :event/login-failed          {::uism/handler show-server-error}
                     :event/set-session-callbacks {::uism/handler set-session-callbacks}
                     ::uism/value-changed         {}}}

                   :state/logged-in
                   {::uism/events
                    {:event/log-out               {::uism/target-state :state/logged-out
                                                   ::uism/handler      log-out}
                     :event/set-session-callbacks {::uism/handler set-session-callbacks}}}}})
