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
(ns violit.article.article-footer-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.ui.article.article-footer :as article-footer]
    [violit.ui.core.dropdown-menu :as dropdown-menu]
    [violit.ui.core.styles :as styles]))

(defsc Root [_this {:keys [article-footer can-vote? can-delete? can-update?]}]
  {:query         [{:article-footer (comp/get-query article-footer/ArticleFooter)}
                   :can-vote?
                   :can-delete?
                   :can-update?]
   :initial-state (fn [{:keys [can-vote? can-delete? can-update?] :as params}]
                    {:article-footer (comp/get-initial-state article-footer/ArticleFooter params)
                     :can-vote?      can-vote?
                     :can-delete?    can-delete?
                     :can-update?    can-update?})
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root styles/body]]
   :css-include   [dropdown-menu/DropdownMenu]}
  (dom/div
    :.root
    (article-footer/ui-article-footer article-footer
                                      {:on-upvote         (when can-vote? #(do))
                                       :on-downvote       (when can-vote? #(do))
                                       :on-open-edit-form (when can-update? #(do))
                                       :on-delete         (when can-delete? #(do))})
    (inj/style-element {:component Root})))

(ws/defcard article-footer-upvoted
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:vote-status :upvoted
                                          :can-vote?   true}}))

(ws/defcard article-footer-downvoted
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:vote-status :downvoted
                                          :can-vote?   true}}))

(ws/defcard article-footer-not-voted
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:vote-status :not-voted
                                          :can-vote?   true}}))

(ws/defcard article-footer-can-delete
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:vote-status :not-voted
                                          :can-vote?   true
                                          :can-delete? true}}))

(ws/defcard article-footer-cannot-vote
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:vote-status :not-voted
                                          :can-vote?   false}}))

(ws/defcard article-footer-can-update
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:vote-status :not-voted
                                          :can-vote?   true
                                          :can-update? true}}))

(ws/defcard article-footer-can-delete-or-update
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:vote-status :not-voted
                                          :can-vote?   true
                                          :can-delete? true
                                          :can-update? true}}))
