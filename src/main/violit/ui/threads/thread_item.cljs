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
(ns violit.ui.threads.thread-item
  (:require
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [violit.schema.thread :as thread]
    [violit.ui.article.article-header :as article-header]
    [violit.ui.core.link :as link]
    [violit.ui.core.theme :as theme]
    [violit.ui.threads.thread :as ui.thread]
    [violit.ui.threads.thread-edit-history :as thread-edit-history]))

(defsc ThreadItem [_this {::thread/keys [slug title] :ui/keys [article-header]}]
  {:query         [::thread/slug
                   ::thread/title
                   ::thread/created-at
                   ::thread/author
                   ::thread/author-pronouns
                   ::thread/score
                   {:ui/article-header (comp/get-query article-header/ArticleHeader)}]
   :ident         ::thread/slug
   :initial-state {::thread/slug       :param/slug
                   ::thread/title      :param/title
                   ::thread/created-at :param/created-at
                   ::thread/author     :param/author
                   ::thread/score      :param/score}
   :pre-merge     (fn [{:keys [current-normalized data-tree]}]
                    (let [{::thread/keys [slug
                                          cursor
                                          author
                                          author-pronouns
                                          created-at
                                          deleted-at
                                          updated-at
                                          score]}
                          (merge current-normalized data-tree)

                          article-header-id (str "article-header-thread-" slug)
                          edit-history-rt (when-not (merge/nilify-not-found deleted-at)
                                            [thread-edit-history/ThreadEditHistory cursor])

                          defaults
                          {:ui/article-header
                           (comp/get-initial-state
                             article-header/ArticleHeader {:id              article-header-id
                                                           :author          author
                                                           :author-pronouns author-pronouns
                                                           :created-at      created-at
                                                           :updated-at      updated-at
                                                           :edit-history-rt edit-history-rt
                                                           :score           score})}]
                      (merge
                        defaults
                        current-normalized
                        data-tree)))
   :css           [[:.thread-item {:margin     [[theme/spacing-lg 0]]
                                   :list-style "none"}]
                   [:.title {:cursor "pointer"}]]}
  (dom/li
    :.thread-item
    (dom/article
      {:itemScope true
       :itemType  "http://schema.org/DiscussionForumPosting"}
      (article-header/ui-article-header article-header)
      (link/ui-link
        :.title
        {:itemProp      "headline"
         :route-targets [ui.thread/Thread slug]}
        title))))

(def ui-thread-item (comp/factory ThreadItem {:keyfn ::thread/slug}))
