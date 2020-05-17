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
(ns violit.article.revision-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [tick.alpha.api :as t]
    [violit.ui.article.revision :as revision]
    [violit.ui.core.styles :as styles]))

(def UPDATED-AT (t/instant "2020-06-13T09:26:28.200Z"))

(def OLD-TEXT "in voluptate velit esse cillum dolore eu fugiat nulla pariatur")

(def NEW-TEXT "in velit esse excepteur cillum dolore sint fugiat nulla pariatus")

(defsc Root [_this {:keys [revision]}]
  {:query         [{:revision (comp/get-query revision/Revision)}]
   :initial-state (fn [params]
                    {:revision (comp/get-initial-state revision/Revision params)})
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root styles/body]]}
  (dom/div
    :.root
    (revision/ui-revision revision)
    (inj/style-element {:component Root})))

(ws/defcard revision
            (ct.fulcro/fulcro-card {::ct.fulcro/root          Root
                                    ::ct.fulcro/wrap-root?    false
                                    ::ct.fulcro/initial-state {:updated-at UPDATED-AT
                                                               :old-text   OLD-TEXT
                                                               :new-text   NEW-TEXT}}))
