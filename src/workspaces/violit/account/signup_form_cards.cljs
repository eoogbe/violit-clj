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
(ns violit.account.signup-form-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.ui.account.signup-form :as signup-form]
    [violit.ui.core.styles :as styles]))

(defsc Root [_this {:keys [signup-form]}]
  {:query         [{:signup-form (comp/get-query signup-form/SignupForm)}]
   :initial-state {:signup-form {:debug? true}}
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root styles/body]]}
  (dom/div
    :.root
    (signup-form/ui-signup-form signup-form)
    (inj/style-element {:component Root})))

(ws/defcard signup-form
            (ct.fulcro/fulcro-card {::ct.fulcro/root       Root
                                    ::ct.fulcro/wrap-root? false}))
