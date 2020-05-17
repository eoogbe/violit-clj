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
(ns violit.account.profile-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.model.session :as session]
    [violit.schema.account :as account]
    [violit.ui.account.profile :as profile]
    [violit.ui.core.styles :as styles]))

(def USERNAME "fred-astaire")

(def CREATED-AT "2020-06-14T12:48:40.000Z")

(def PRONOUNS "he/him")

(defsc Root [_this {:keys [profile]}]
  {:query         [{:profile (comp/get-query profile/Profile)}
                   {:ui/current-credentials (comp/get-query session/Credentials)}]
   :initial-state (fn [{:keys [profile credentials]}]
                    {:profile
                     (comp/get-initial-state profile/Profile profile)

                     :ui/current-credentials
                     (comp/get-initial-state session/Credentials credentials)})
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root styles/body]]}
  (dom/div
    :.root
    (profile/ui-profile profile)
    (inj/style-element {:component Root})))

(ws/defcard profile-for-current-user
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:profile     {:username   USERNAME
                                                        :created-at CREATED-AT
                                                        :pronouns   PRONOUNS}
                                          :credentials {:id       "profile-account1"
                                                        :username USERNAME
                                                        :status   :success}}}))

(ws/defcard logged-out-profile
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:profile     {:username   USERNAME
                                                        :created-at CREATED-AT
                                                        :pronouns   PRONOUNS}
                                          :credentials {:status :logged-out}}}))

(ws/defcard profile-without-pronouns
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:profile     {:username   USERNAME
                                                        :created-at CREATED-AT}
                                          :credentials {:id       "profile-account2"
                                                        :username USERNAME
                                                        :status   :success}}}))

(ws/defcard profile-editing-pronouns
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root
               Root

               ::ct.fulcro/wrap-root?
               false

               ::ct.fulcro/initial-state
               {:profile     {:username      USERNAME
                              :created-at    CREATED-AT
                              :pronouns-form (account/account-path USERNAME)}
                :credentials {:id       "profile-account3"
                              :username USERNAME
                              :status   :success}}}))
