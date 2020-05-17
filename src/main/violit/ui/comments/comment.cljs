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
(ns violit.ui.comments.comment
  (:require
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [goog.object :as gobj]
    [violit.model.comment :as model]
    [violit.model.session :as session]
    [violit.schema.comment :as comment]
    [violit.ui.article.article-footer :as article-footer]
    [violit.ui.article.article-header :as article-header]
    [violit.ui.comments.edit-comment-form :as edit-comment-form]
    [violit.ui.comments.comment-edit-history :as comment-edit-history]
    [violit.ui.core.markdown :as markdown]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]))

(defn scroll-to-comment
  [this]
  (m/set-value! this :ui/scroll-to? false)
  (when-let [comment-el (gobj/get this "comment-el")]
    (.scrollIntoView comment-el)))

(defsc Comment [this {::comment/keys [id text deleted-at]
                      :ui/keys       [last-created-id
                                      article-header
                                      article-footer
                                      edit-comment-form
                                      current-credentials]
                      :as            props}
                {:keys [disabled?]}]
  {:query             [::comment/id
                       ::comment/cursor
                       ::comment/text
                       ::comment/created-at
                       ::comment/author
                       ::comment/author-pronouns
                       ::comment/deleted-at
                       ::comment/updated-at
                       ::comment/score
                       ::comment/vote-status
                       :ui/last-created-id
                       :ui/scroll-to?
                       {:ui/article-header (comp/get-query article-header/ArticleHeader)}
                       {:ui/article-footer (comp/get-query article-footer/ArticleFooter)}
                       {:ui/edit-comment-form (comp/get-query edit-comment-form/EditCommentForm)}
                       {[:ui/current-credentials '_] (comp/get-query session/Credentials)}]
   :ident             ::comment/id
   :initial-state     {::comment/id          :param/id
                       ::comment/text        :param/text
                       ::comment/created-at  :param/created-at
                       ::comment/author      :param/author
                       ::comment/deleted-at  :param/deleted-at
                       ::comment/score       :param/score
                       ::comment/vote-status :param/vote-status
                       :ui/last-created-id   :param/last-created-id
                       :ui/edit-comment-form :param/edit-comment-form}
   :pre-merge         (fn [{:keys [current-normalized data-tree]}]
                        (let [{::comment/keys [id
                                               cursor
                                               author
                                               author-pronouns
                                               created-at
                                               deleted-at
                                               updated-at
                                               score
                                               vote-status]}
                              (merge current-normalized data-tree)

                              article-header-id (str "article-header-comment-" id)
                              edit-history-rt (when-not (merge/nilify-not-found deleted-at)
                                                [comment-edit-history/CommentEditHistory cursor])
                              article-footer-id (str "article-footer-comment-" id)

                              defaults
                              {:ui/article-header
                               (comp/get-initial-state
                                 article-header/ArticleHeader {:id              article-header-id
                                                               :author          author
                                                               :author-pronouns author-pronouns
                                                               :created-at      created-at
                                                               :updated-at      updated-at
                                                               :edit-history-rt edit-history-rt
                                                               :score           score})

                               :ui/article-footer
                               (comp/get-initial-state
                                 article-footer/ArticleFooter {:id          article-footer-id
                                                               :vote-status vote-status})}]
                          (merge
                            defaults
                            current-normalized
                            data-tree)))
   :initLocalState    (fn [this _props]
                        {:save-comment-ref #(gobj/set this "comment-el" %)})
   :componentDidMount (fn [this]
                        (let [{:ui/keys [scroll-to?]} (comp/props this)]
                          (when scroll-to?
                            (scroll-to-comment this))))
   :css               [[:.comment {:margin      [[theme/spacing-lg 0]]
                                   :padding     [["1px" theme/spacing-md]]
                                   :border-left [["2px" "solid" theme/gray300]]
                                   :list-style  "none"}]
                       [:.highlight {:background-color theme/primary-color-lightest}]
                       [:.no-text-paragraph styles/no-text-paragraph]]}
  (let [{:keys [save-comment-ref]} (comp/get-state this)
        last-created-comment? (= id last-created-id)
        can-vote? (and (not disabled?) (comment/can-vote? current-credentials props))
        handle-upvote (when can-vote?
                        #(comp/transact! this [(model/upvote-comment {:id id})]))
        handle-downvote (when can-vote?
                          #(comp/transact! this [(model/downvote-comment {:id id})]))
        handle-open-edit-form (when (and
                                      (not disabled?)
                                      (comment/can-update? current-credentials props))
                                #(comp/transact! this [(model/open-edit-comment-form {:id id})]))
        handle-delete (when (and (not disabled?) (comment/can-delete? current-credentials props))
                        #(comp/transact! this [(model/start-delete-comment {:id id})]))]
    (dom/li
      :.comment
      {:classes [(when last-created-comment? :.highlight)]
       :ref     save-comment-ref}
      (dom/article
        {:itemProp  "comment"
         :itemScope true
         :itemType  "http://schema.org/Comment"}
        (article-header/ui-article-header article-header)
        (if edit-comment-form
          (edit-comment-form/ui-edit-comment-form edit-comment-form)
          (comp/fragment
            (if deleted-at
              (dom/p :.no-text-paragraph {:itemProp "text"} "[deleted]")
              (dom/div
                {:itemProp "text"}
                (markdown/ui-markdown {:source text})))
            (article-footer/ui-article-footer
              article-footer
              {:on-upvote         handle-upvote
               :on-downvote       handle-downvote
               :on-open-edit-form handle-open-edit-form
               :on-delete         handle-delete})))))))

(def ui-comment (comp/computed-factory Comment {:keyfn ::comment/id}))
