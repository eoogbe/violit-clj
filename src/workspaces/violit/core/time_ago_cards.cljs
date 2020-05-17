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
(ns violit.core.time-ago-cards
  (:require
    [com.fulcrologic.fulcro.components :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.time-ago :as time-ago]))

(def DATE "2020-06-14T19:53:28.000Z")

(defsc Root [_this _props]
  {:css [[:* {:box-sizing "border-box"}]
         [:.root (merge styles/body {:position "relative"})]]}
  (dom/div
    :.root
    (time-ago/ui-time-ago {:date DATE})
    (inj/style-element {:component Root})))

(ws/defcard time-ago
            (ct.fulcro/fulcro-card {::ct.fulcro/root       Root
                                    ::ct.fulcro/wrap-root? false}))
