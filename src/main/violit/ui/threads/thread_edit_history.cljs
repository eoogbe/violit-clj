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
(ns violit.ui.threads.thread-edit-history
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [violit.schema.thread :as thread]
    [violit.ui.article.revision :as revision]
    [violit.ui.core.link :as link]
    [violit.ui.core.main-container :as main-container]
    [violit.ui.core.not-found-page :as not-found-page]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.title :as title]))

(defsc ThreadEditHistory [_this {::thread/keys [slug revisions]}]
  {:query             [::thread/cursor
                       ::thread/slug
                       ::thread/title
                       {::thread/revisions (comp/get-query revision/Revision)}]
   :ident             [::thread/edit-history ::thread/cursor]
   :initial-state     {::thread/cursor    :param/cursor
                       ::thread/slug      :param/slug
                       ::thread/revisions :param/revisions}
   :route-segment     ["thread-revisions" ::thread/cursor]
   :will-enter        (fn [app params]
                        (let [ident (comp/get-ident ThreadEditHistory params)]
                          (dr/route-deferred ident
                                             #(df/load! app ident ThreadEditHistory
                                                        {:post-mutation        `dr/target-ready
                                                         :post-mutation-params {:target ident}
                                                         :marker               false}))))
   :componentDidMount (fn [this]
                        (let [{::thread/keys [title]} (comp/props this)]
                          (title/set-title! (str "Revisions to " title))))
   :css               [[:.heading styles/page-heading-link]
                       [:.revision-list {:margin       0
                                         :padding-left "2.5em"}]]}
  (main-container/ui-main-container
    {}
    (not-found-page/ui-not-found-page
      {:found? (some? slug)}
      (link/ui-link :.heading {:route ["threads" slug]} "Return to thread")
      (dom/ol
        :.revision-list
        (map revision/ui-revision revisions)))))

(def ui-thread-edit-history (comp/factory ThreadEditHistory))
