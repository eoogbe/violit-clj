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
(ns violit.threads.thread-item-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.ui.core.styles :as styles]
    [violit.ui.threads.thread-item :as thread-item]))

(def SLUG "lorem-ipsum")

(def TITLE "Lorem ipsum")

(def CREATED-AT "2020-06-14T12:52:56.000Z")

(def AUTHOR "fred-astaire")

(def SCORE 1)

(defsc Root [_this {:keys [thread-item]}]
  {:query         [{:thread-item (comp/get-query thread-item/ThreadItem)}]
   :initial-state (fn [params]
                    {:thread-item (comp/get-initial-state thread-item/ThreadItem params)})
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root styles/body]]}
  (dom/div
    :.root
    (thread-item/ui-thread-item thread-item)
    (inj/style-element {:component Root})))

(ws/defcard thread-item
            (ct.fulcro/fulcro-card {::ct.fulcro/root          Root
                                    ::ct.fulcro/wrap-root?    false
                                    ::ct.fulcro/initial-state {:slug       SLUG
                                                               :title      TITLE
                                                               :created-at CREATED-AT
                                                               :author     AUTHOR
                                                               :score      SCORE}}))
