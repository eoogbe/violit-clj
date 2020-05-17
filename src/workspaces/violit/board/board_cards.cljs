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
(ns violit.board.board-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.model.session :as session]
    [violit.ui.board.board :as board]
    [violit.ui.core.main-container :as main-container]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]
    [violit.ui.threads.thread-item :as thread-item]))

(def NAME "dance")

(def DESCRIPTION "Commodo consequat duis aute irure dolor in reprehenderit.")

(def THREADS
  [{:slug       "lorem-ipsum"
    :title      "Lorem ipsum"
    :created-at "2020-06-14T12:47:40.000Z"
    :author     "fred-astaire"}
   {:slug       "aliquip-ex-ea'"
    :title      "Aliquip ex ea"
    :created-at "2020-06-14T12:48:40.000Z"
    :author     "ginger-rogers"}
   {:slug       "commodo-consequat"
    :title      "Commodo consequat"
    :created-at "2020-06-14T12:49:40.000Z"
    :author     "ginger-rogers"}])

(def USERNAME "fred-astaire")

(defsc Root [_this {:keys [board]}]
  {:query         [{:board (comp/get-query board/Board)}
                   {:ui/current-credentials (comp/get-query session/Credentials)}]
   :initial-state (fn [{:keys [board credentials]}]
                    {:board
                     (comp/get-initial-state board/Board board)

                     :ui/current-credentials
                     (comp/get-initial-state session/Credentials credentials)})
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root (merge styles/body {:width            "65vw"
                                               :border           [["1px" "solid" theme/gray200]]
                                               :background-color theme/gray200})]]
   :css-include   [main-container/MainContainer]}
  (dom/div
    :.root
    (board/ui-board board)
    (inj/style-element {:component Root})))

(ws/defcard board-with-threads
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root
               Root

               ::ct.fulcro/wrap-root?
               false

               ::ct.fulcro/initial-state
               {:board
                {:name        NAME
                 :description DESCRIPTION
                 :threads     (mapv #(comp/get-initial-state thread-item/ThreadItem %) THREADS)}

                :credentials
                {:id       "board-account1"
                 :username USERNAME
                 :status   :success}}}))

(ws/defcard board-without-threads
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:board       {:name        NAME
                                                        :description DESCRIPTION
                                                        :threads     []}
                                          :credentials {:id       "board-account2"
                                                        :username USERNAME
                                                        :status   :success}}}))

(ws/defcard board-with-thread-load-failed
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:board       {:name         NAME
                                                        :load-failed? true}
                                          :credentials {:id       "board-account3"
                                                        :username USERNAME
                                                        :status   :success}}}))

(ws/defcard logged-out-board
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root
               Root

               ::ct.fulcro/wrap-root?
               false

               ::ct.fulcro/initial-state
               {:board
                {:name        NAME
                 :description DESCRIPTION
                 :threads     (mapv #(comp/get-initial-state thread-item/ThreadItem %) THREADS)}

                :credentials
                {:status :logged-out}}}))

(ws/defcard board-with-load-more-button
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root
               Root

               ::ct.fulcro/wrap-root?
               false

               ::ct.fulcro/initial-state
               {:board
                {:name
                 NAME

                 :description
                 DESCRIPTION

                 :next-thread-cursor
                 "board-thread1="

                 :threads
                 (mapv #(comp/get-initial-state thread-item/ThreadItem %) THREADS)}

                :credentials
                {:id       "board-account1"
                 :username USERNAME
                 :status   :success}}}))

(ws/defcard board-without-description
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root
               Root

               ::ct.fulcro/wrap-root?
               false

               ::ct.fulcro/initial-state
               {:board
                {:name    NAME
                 :threads (mapv #(comp/get-initial-state thread-item/ThreadItem %) THREADS)}

                :credentials
                {:id       "board-account1"
                 :username USERNAME
                 :status   :success}}}))
