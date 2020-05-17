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
(ns violit.comments.comment-edit-history-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [tick.alpha.api :as t]
    [violit.ui.article.revision :as revision]
    [violit.ui.comments.comment-edit-history :as comment-edit-history]
    [violit.ui.core.styles :as styles]))

(def CURSOR "ABCDEF")

(def SLUG "lorem-ipsum")

(def REVISIONS
  [{:updated-at (t/instant "2020-06-13T09:26:28.200Z")
    :old-text   "in voluptate velit esse cillum dolore eu fugiat nulla pariatur"
    :new-text   "in velit esse excepteur cillum dolore sint fugiat nulla pariatus"}
   {:updated-at (t/instant "2020-06-13T09:47:15.000Z")
    :old-text   "occaecat cupidatat non proident"
    :new-text   "occaecat cupidatat non proident sunt"}
   {:updated-at (t/instant "2020-06-13T09:48:55.000Z")
    :old-text   "in culpa qui officia deserunt mollit anim id est laborum"
    :new-text   "culpa qui officia deserunt mollit anim id est laborum"}])

(defsc Root [_this {:keys [comment-edit-history]}]
  {:query         [{:comment-edit-history (comp/get-query comment-edit-history/CommentEditHistory)}]
   :initial-state (fn [params]
                    {:comment-edit-history
                     (comp/get-initial-state comment-edit-history/CommentEditHistory params)})
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root styles/body]]}
  (dom/div
    :.root
    (comment-edit-history/ui-comment-edit-history comment-edit-history)
    (inj/style-element {:component Root})))

(ws/defcard comment-edit-history
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root
               Root

               ::ct.fulcro/wrap-root?
               false

               ::ct.fulcro/initial-state
               {:cursor    CURSOR
                :slug      SLUG
                :revisions (mapv #(comp/get-initial-state revision/Revision %) REVISIONS)}}))
