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
(ns violit.core.loading-indicator-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.ui.core.loading-indicator :as loading-indicator]
    [violit.ui.core.styles :as styles]))

(defsc Root [_this {:keys [loading-indicator]}]
  {:query         [{:loading-indicator (comp/get-query loading-indicator/LoadingIndicator)}]
   :initial-state {:loading-indicator {:visible? true}}
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root (merge styles/body {:position "relative"})]]}
  (dom/div
    :.root
    (loading-indicator/ui-loading-indicator loading-indicator)
    (inj/style-element {:component Root})))

(ws/defcard loading-indicator
            (ct.fulcro/fulcro-card {::ct.fulcro/root       Root
                                    ::ct.fulcro/wrap-root? false}))
