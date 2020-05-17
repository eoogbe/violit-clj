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
(ns violit.article.article-header-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.ui.article.article-header :as article-header]
    [violit.ui.core.styles :as styles]))

(def HEADING "Aliquid ex ea")

(def CREATED-AT "2020-06-13T09:23:01.000Z")

(def UPDATED-AT "2020-06-13T09:24:01.000Z")

(def AUTHOR "fred-astaire")

(def AUTHOR-PRONOUNS "he/him")

(def SCORE 2)

(defsc Root [_this {:keys [article-header heading]}]
  {:query         [{:article-header (comp/get-query article-header/ArticleHeader)}
                   :heading]
   :initial-state (fn [{:keys [heading] :as params}]
                    {:article-header (comp/get-initial-state article-header/ArticleHeader params)
                     :heading        heading})
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root styles/body]]}
  (dom/div
    :.root
    (article-header/ui-article-header article-header {:heading heading})
    (inj/style-element {:component Root})))

(ws/defcard article-header-with-author
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    true
               ::ct.fulcro/initial-state {:id              "article-header1"
                                          :author          AUTHOR
                                          :author-pronouns AUTHOR-PRONOUNS
                                          :score           SCORE
                                          :created-at      CREATED-AT}}))

(ws/defcard article-header-with-heading
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    true
               ::ct.fulcro/initial-state {:id              "article-header2"
                                          :heading         HEADING
                                          :author          AUTHOR
                                          :author-pronouns AUTHOR-PRONOUNS
                                          :score           SCORE
                                          :created-at      CREATED-AT}}))

(ws/defcard article-header-without-author
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    true
               ::ct.fulcro/initial-state {:id         "article-header3"
                                          :author     nil
                                          :score      SCORE
                                          :created-at CREATED-AT}}))

(ws/defcard article-header-with-1-point
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    true
               ::ct.fulcro/initial-state {:id              "article-header4"
                                          :author          AUTHOR
                                          :author-pronouns AUTHOR-PRONOUNS
                                          :score           1
                                          :created-at      CREATED-AT}}))

(ws/defcard article-header-with-negative-points
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    true
               ::ct.fulcro/initial-state {:id              "article-header5"
                                          :author          AUTHOR
                                          :author-pronouns AUTHOR-PRONOUNS
                                          :score           -1
                                          :created-at      CREATED-AT}}))

(ws/defcard article-header-without-author-pronouns
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    true
               ::ct.fulcro/initial-state {:id         "article-header6"
                                          :author     AUTHOR
                                          :score      SCORE
                                          :created-at CREATED-AT}}))

(ws/defcard article-header-for-updated
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    true
               ::ct.fulcro/initial-state {:id              "article-header7"
                                          :author          AUTHOR
                                          :author-pronouns AUTHOR-PRONOUNS
                                          :score           SCORE
                                          :created-at      CREATED-AT
                                          :updated-at      UPDATED-AT
                                          :edit-history-rt [""]}}))

(ws/defcard article-header-without-edit-history-route-targets
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    true
               ::ct.fulcro/initial-state {:id              "article-header8"
                                          :author          AUTHOR
                                          :author-pronouns AUTHOR-PRONOUNS
                                          :score           SCORE
                                          :created-at      CREATED-AT
                                          :updated-at      UPDATED-AT}}))
