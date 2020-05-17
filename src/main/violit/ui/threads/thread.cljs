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
(ns violit.ui.threads.thread
  (:require
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [goog.object :as gobj]
    [violit.lib.routing :as routing]
    [violit.model.comment :as model.comment]
    [violit.model.session :as session]
    [violit.model.thread :as model]
    [violit.schema.thread :as thread]
    [violit.ui.article.article-footer :as article-footer]
    [violit.ui.article.article-header :as article-header]
    [violit.ui.auth.login-form :as login-form]
    [violit.ui.comments.comment :as ui.comment]
    [violit.ui.comments.new-comment-form :as new-comment-form]
    [violit.ui.core.link :as link]
    [violit.ui.core.main-container :as main-container]
    [violit.ui.core.markdown :as markdown]
    [violit.ui.core.not-found-page :as not-found-page]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]
    [violit.ui.core.title :as title]
    [violit.ui.threads.edit-thread-form :as edit-thread-form]
    [violit.ui.threads.thread-edit-history :as thread-edit-history]))

(defn scroll-to-comment-list
  [this]
  (when-let [comment-list-el (gobj/get this "comment-list-el")]
    (.scrollIntoView comment-list-el)))

(defsc Thread [this
               {::thread/keys [slug title body deleted-at next-comment-cursor comments]
                :ui/keys      [new-comment-form
                               article-header
                               article-footer
                               edit-thread-form
                               current-credentials]
                :as           props}]
  {:query             [::thread/slug
                       ::thread/cursor
                       ::thread/title
                       ::thread/body
                       ::thread/created-at
                       ::thread/author
                       ::thread/author-pronouns
                       ::thread/deleted-at
                       ::thread/updated-at
                       ::thread/score
                       ::thread/vote-status
                       ::thread/next-comment-cursor
                       {::thread/comments (comp/get-query ui.comment/Comment)}
                       {:ui/new-comment-form (comp/get-query new-comment-form/NewCommentForm)}
                       {:ui/article-header (comp/get-query article-header/ArticleHeader)}
                       {:ui/article-footer (comp/get-query article-footer/ArticleFooter)}
                       {:ui/edit-thread-form (comp/get-query edit-thread-form/EditThreadForm)}
                       {[:ui/current-credentials '_] (comp/get-query session/Credentials)}
                       [df/marker-table ::model/comment-page]
                       :ui/load-failed?]
   :ident             ::thread/slug
   :initial-state     {::thread/slug                :param/slug
                       ::thread/title               :param/title
                       ::thread/body                :param/body
                       ::thread/created-at          :param/created-at
                       ::thread/author              :param/author
                       ::thread/deleted-at          :param/deleted-at
                       ::thread/score               :param/score
                       ::thread/vote-status         :param/vote-status
                       ::thread/next-comment-cursor :param/next-comment-cursor
                       ::thread/comments            :param/comments
                       :ui/edit-thread-form         :param/edit-thread-form
                       :ui/load-failed?             :param/load-failed?}
   :pre-merge         (fn [{:keys [current-normalized data-tree]}]
                        (let [{::thread/keys [slug
                                              cursor
                                              author
                                              author-pronouns
                                              created-at
                                              deleted-at
                                              updated-at
                                              score
                                              vote-status]}
                              (merge current-normalized data-tree)

                              article-header-id (str "article-header-thread-" slug)
                              edit-history-rt (when-not (merge/nilify-not-found deleted-at)
                                                [thread-edit-history/ThreadEditHistory cursor])
                              article-footer-id (str "article-footer-thread-" slug)

                              defaults
                              {:ui/new-comment-form
                               (comp/get-initial-state new-comment-form/NewCommentForm {})

                               :ui/article-header
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
   :route-segment     ["threads" ::thread/slug]
   :will-enter        (fn [app {::thread/keys [slug] :as params}]
                        (let [ident (comp/get-ident Thread params)]
                          (comp/transact! app [(model/close-edit-thread-form {:slug slug})
                                               (model/clear-new-comment-form {:slug slug})
                                               (model.comment/close-any-edit-comment-form {})
                                               (model.comment/remove-last-created-id {})])
                          (model/load-comments! app {:slug      slug
                                                     :targeting :replace})
                          (dr/route-deferred
                            ident
                            #(df/load!
                               app ident Thread
                               {:without              #{::thread/next-comment-cursor
                                                        ::thread/comments
                                                        [df/marker-table ::model/comment-page]}
                                :post-mutation        `dr/target-ready
                                :post-mutation-params {:target ident}
                                :marker               false}))))
   :initLocalState    (fn [this _props]
                        {:save-comment-list-ref #(gobj/set this "comment-list-el" %)})
   :componentDidMount (fn [this]
                        (let [{:keys [::thread/title]} (comp/props this)]
                          (title/set-title! title)))
   :css               [[:.main {:border-bottom [["1px" "solid" theme/gray200]]}]
                       [:.no-text-paragraph styles/no-text-paragraph]
                       [:.no-comment-form-paragraph {:margin     [[theme/spacing-md 0]]
                                                     :color      theme/gray700
                                                     :text-align "center"}
                        [:a {:text-decoration "underline"
                             :cursor          "pointer"}]]
                       [:.comment-list styles/list-reset]
                       [:.no-list-container styles/no-list-container]
                       [:.no-list-paragraph styles/no-list-paragraph]
                       [:.reload-button styles/button-tertiary]
                       [:.load-more-container {:display         "flex"
                                               :justify-content "center"}]
                       [:.load-more-button styles/button-primary]]}
  (let [marker (get props [df/marker-table ::model/comment-page])
        load-failed? (or (:ui/load-failed? props) (df/failed? marker))
        {:keys [save-comment-list-ref]} (comp/get-state this)
        handle-upvote (when (thread/can-vote? current-credentials props)
                        #(comp/transact! this [(model/upvote-thread {:slug slug})]))
        handle-downvote (when (thread/can-vote? current-credentials props)
                          #(comp/transact! this [(model/downvote-thread {:slug slug})]))
        handle-open-edit-thread-form (when (thread/can-update? current-credentials props)
                                       #(comp/transact!
                                          this [(model/open-edit-thread-form {:slug slug})]))
        handle-delete (when (thread/can-delete? current-credentials props)
                        (fn []
                          (comp/transact! this [(model/start-delete-thread {:slug slug})])
                          (routing/replace-route! this {:route [""]})))]
    (main-container/ui-main-container
      {}
      (not-found-page/ui-not-found-page
        {:found? (some? title)}
        (dom/article
          {:itemScope true
           :itemType  "http://schema.org/DiscussionForumPosting"}
          (dom/div
            :.main
            (article-header/ui-article-header article-header {:heading title})
            (if edit-thread-form
              (edit-thread-form/ui-edit-thread-form edit-thread-form)
              (comp/fragment
                (cond
                  deleted-at (dom/p :.no-text-paragraph {:itemProp "articleBody"} "[deleted]")
                  body (dom/div
                         {:itemProp "articleBody"}
                         (markdown/ui-markdown {:source body})))
                (when-not deleted-at
                  (if (session/logged-in? current-credentials)
                    (new-comment-form/ui-new-comment-form
                      new-comment-form
                      {:thread-slug slug
                       :on-create   #(scroll-to-comment-list this)})
                    (dom/p :.no-comment-form-paragraph
                           "What are your thoughts? "
                           (link/ui-link
                             {:route-targets [login-form/LoginForm]
                              :save-route?   false}
                             "Log in"))))
                (article-footer/ui-article-footer
                  article-footer
                  {:on-upvote         handle-upvote
                   :on-downvote       handle-downvote
                   :on-open-edit-form handle-open-edit-thread-form
                   :on-delete         handle-delete}))))
          (cond
            (seq comments)
            (dom/ul :.comment-list
                    {:ref save-comment-list-ref}
                    (map #(ui.comment/ui-comment % {:disabled? (some? deleted-at)}) comments))

            (not load-failed?)
            (dom/p :.no-list-paragraph "No comments"))
          (cond
            load-failed?
            (dom/div
              :.no-list-container
              (dom/p :.no-list-paragraph "Oops! Something went wrong with loading the comments.")
              (dom/button
                :.reload-button
                {:onClick #(model/load-comments! this {:slug      slug
                                                       :after     next-comment-cursor
                                                       :targeting :replace})
                 :type    "button"}
                "Reload"))

            next-comment-cursor
            (dom/div
              :.load-more-container
              (dom/button
                :.load-more-button
                {:onClick #(model/load-comments! this {:slug      slug
                                                       :after     next-comment-cursor
                                                       :targeting :append})
                 :type    "button"}
                "Load more comments"))))))))

(def ui-thread (comp/factory Thread))
