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
(ns violit.ui.board.board
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.dom.icons :as icons]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro-css.css :as css]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [violit.model.board :as model]
    [violit.model.session :as session]
    [violit.schema.board :as board]
    [violit.ui.core.link :as link]
    [violit.ui.core.main-container :as main-container]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]
    [violit.ui.core.title :as title]
    [violit.ui.threads.new-thread-form :as thread-form]
    [violit.ui.threads.thread-item :as thread-item]))

(defn ui-board-sidebar
  [{::board/keys [description]}]
  (when description
    (dom/p :.description description)))

(defsc Board [this {::board/keys [name threads created-threads-before next-thread-cursor]
                    :ui/keys     [current-credentials]
                    :as          props}]
  {:query             [::board/name
                       ::board/description
                       ::board/created-threads-before
                       ::board/next-thread-cursor
                       {::board/threads (comp/get-query thread-item/ThreadItem)}
                       [df/marker-table ::threads]
                       {[:ui/current-credentials '_] (comp/get-query session/Credentials)}
                       :ui/load-failed?]
   :ident             (fn [] (board/board-path))
   :initial-state     {::board/name               :param/name
                       ::board/description        :param/description
                       ::board/next-thread-cursor :param/next-thread-cursor
                       ::board/threads            :param/threads
                       :ui/load-failed?           :param/load-failed?}
   :route-segment     [""]
   :will-enter        (fn [app _params]
                        (let [ident (comp/get-ident Board {})]
                          (model/load-threads! app {:targeting :replace})
                          (dr/route-deferred
                            ident
                            #(df/load! app ident Board
                                       {:without              #{::board/created-threads-before
                                                                ::board/next-thread-cursor
                                                                ::board/threads
                                                                [df/marker-table ::threads]}
                                        :post-mutation        `dr/target-ready
                                        :post-mutation-params {:target ident}
                                        :marker               false}))))
   :componentDidMount (fn [] (title/set-title! nil))
   :css               [[:.description {:margin 0}]
                       [:.header {:display         "flex"
                                  :justify-content "space-between"
                                  :align-items     "flex-end"
                                  :border-bottom   [["1px" "solid" theme/gray200]]}]
                       [:.name {:font-size "1rem"
                                :margin    [[theme/spacing-sm 0]]}]
                       [:.new-thread-icon {:fill theme/primary-color}]
                       [:.thread-list styles/list-reset]
                       [:.no-list-container styles/no-list-container]
                       [:.no-list-paragraph styles/no-list-paragraph]
                       [:.reload-button styles/button-tertiary]
                       [:.load-more-container {:display         "flex"
                                               :justify-content "center"}]
                       [:.load-more-button styles/button-primary]]}
  (let [{:keys [new-thread-icon]} (css/get-classnames Board)
        marker (get props [df/marker-table ::model/thread-page])
        load-failed? (or (:ui/load-failed? props) (df/failed? marker))]
    (main-container/ui-main-container
      {:sidebar (ui-board-sidebar props)}
      (dom/article
        (dom/header
          :.header
          (dom/h2 :.name name)
          (when (session/logged-in? current-credentials)
            (link/ui-link
              {:route-targets [thread-form/NewThreadForm]}
              (icons/ui-icon {:className new-thread-icon
                              :icon      :add-box
                              :title     "New thread"}))))
        (cond
          (seq threads)
          (dom/ul
            :.thread-list
            (map thread-item/ui-thread-item threads))

          (not load-failed?)
          (dom/p :.no-list-paragraph "No threads"))
        (cond
          load-failed?
          (dom/div
            :.no-list-container
            (dom/p :.no-list-paragraph "Oops! Something went wrong with loading the threads.")
            (dom/button
              :.reload-button
              {:onClick #(model/load-threads! this {:before    created-threads-before
                                                    :after     next-thread-cursor
                                                    :targeting :replace})
               :type    "button"}
              "Reload"))

          next-thread-cursor
          (dom/div
            :.load-more-container
            (dom/button
              :.load-more-button
              {:onClick #(model/load-threads! this {:before    created-threads-before
                                                    :after     next-thread-cursor
                                                    :targeting :append})
               :type    "button"}
              "Load more threads")))))))

(def ui-board (comp/factory Board))
