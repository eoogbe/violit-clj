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
(ns violit.ui.comments.comment-edit-history
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [violit.lib.routing :as routing]
    [violit.schema.comment :as comment]
    [violit.schema.thread :as thread]
    [violit.ui.article.revision :as revision]
    [violit.ui.core.main-container :as main-container]
    [violit.ui.core.not-found-page :as not-found-page]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.title :as title]))

(defmutation nav-to-comment [{:keys [id slug]}]
  (action [{:keys [state app]}]
          (routing/push-route! app {:route ["threads" slug]})
          (swap! state assoc-in (comment/comment-path id :ui/scroll-to?) true)))

(defsc CommentEditHistory [this {::comment/keys [id revisions] ::thread/keys [slug]}]
  {:query             [::comment/cursor
                       ::comment/id
                       ::thread/slug
                       ::thread/title
                       {::comment/revisions (comp/get-query revision/Revision)}]
   :ident             [::comment/edit-history ::comment/cursor]
   :initial-state     {::comment/cursor    :param/cursor
                       ::thread/slug       :param/slug
                       ::comment/revisions :param/revisions}
   :route-segment     ["comment-revisions" ::comment/cursor]
   :will-enter        (fn [app params]
                        (let [ident (comp/get-ident CommentEditHistory params)]
                          (dr/route-deferred ident
                                             #(df/load! app ident CommentEditHistory
                                                        {:post-mutation        `dr/target-ready
                                                         :post-mutation-params {:target ident}
                                                         :marker               false}))))
   :componentDidMount (fn [this]
                        (let [{::thread/keys [title]} (comp/props this)]
                          (title/set-title! (str "Revisions to comment on " title))))
   :css               [[:.heading styles/page-heading-link]
                       [:.revision-list {:margin       0
                                         :padding-left "2.5em"}]]}
  (main-container/ui-main-container
    {}
    (not-found-page/ui-not-found-page
      {:found? (some? slug)}
      (dom/button
        :.heading
        {:onClick #(comp/transact! this [(nav-to-comment {:id id :slug slug})])
         :type    "button"}
        "Return to comment")
      (dom/ol
        :.revision-list
        (map revision/ui-revision revisions)))))

(def ui-comment-edit-history (comp/factory CommentEditHistory))
