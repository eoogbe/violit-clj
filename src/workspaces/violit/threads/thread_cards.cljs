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
(ns violit.threads.thread-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.model.session :as session]
    [violit.schema.thread :as thread]
    [violit.ui.core.dropdown-menu :as dropdown-menu]
    [violit.ui.comments.comment :as comment]
    [violit.ui.core.markdown :as markdown]
    [violit.ui.core.styles :as styles]
    [violit.ui.threads.thread :as ui.thread]))

(def TITLE "Lorem ipsum")

(def BODY "Dolor sit amet, consectetur adipiscing elit.")

(def CREATED-AT "2020-06-14T12:47:38.000Z")

(def AUTHOR "fred-astaire")

(def AUTHOR2 "ginger-rogers")

(def DELETED-AT "2020-06-14T12:51:38.000Z")

(def SCORE 1)

(def VOTE-STATUS :not-voted)

(def COMMENTS
  [{:id         "thread-comment1"
    :text       "Sed do eiusmod tempor incididunt ut labore et dolore."
    :created-at "2020-06-14T12:48:38.000Z"
    :author     AUTHOR}
   {:id         "thread-comment2"
    :text       "Ut enim ad minim veniam."
    :created-at "2020-06-14T12:49:38.000Z"
    :author     AUTHOR2}
   {:id         "thread-comment3"
    :text       "Quis nostrud exercitation ullamco laboris nisi ut."
    :created-at "2020-06-14T12:50:38.000Z"
    :author     AUTHOR2}])

(defsc Root [_this {:keys [thread]}]
  {:query         [{:thread (comp/get-query ui.thread/Thread)}
                   {:ui/current-credentials (comp/get-query session/Credentials)}]
   :initial-state (fn [{:keys [thread credentials]}]
                    {:thread
                     (comp/get-initial-state ui.thread/Thread thread)

                     :ui/current-credentials
                     (comp/get-initial-state session/Credentials credentials)})
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root styles/body]]
   :css-include   [dropdown-menu/DropdownMenu markdown/Markdown]}
  (dom/div
    :.root
    (ui.thread/ui-thread thread)
    (inj/style-element {:component Root})))

(ws/defcard thread-with-comments
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root
               Root

               ::ct.fulcro/wrap-root?
               false

               ::ct.fulcro/initial-state
               {:thread
                {:slug        "lorem-ipsum1"
                 :title       TITLE
                 :body        BODY
                 :created-at  CREATED-AT
                 :author      AUTHOR
                 :score       SCORE
                 :vote-status VOTE-STATUS
                 :comments    (mapv #(comp/get-initial-state comment/Comment %) COMMENTS)}

                :credentials
                {:id       "thread-account1"
                 :username AUTHOR
                 :status   :success}}}))

(ws/defcard thread-without-comments
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:thread      {:slug        "lorem-ipsum2"
                                                        :title       TITLE
                                                        :body        BODY
                                                        :created-at  CREATED-AT
                                                        :author      AUTHOR
                                                        :score       SCORE
                                                        :vote-status VOTE-STATUS
                                                        :comments    []}
                                          :credentials {:id       "thread-account2"
                                                        :username AUTHOR
                                                        :status   :success}}}))

(ws/defcard thread-without-body
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root
               Root

               ::ct.fulcro/wrap-root?
               false

               ::ct.fulcro/initial-state
               {:thread
                {:slug        "lorem-ipsum3"
                 :title       TITLE
                 :created-at  CREATED-AT
                 :author      AUTHOR
                 :score       SCORE
                 :vote-status VOTE-STATUS
                 :comments    (mapv #(comp/get-initial-state comment/Comment %) COMMENTS)}

                :credentials
                {:id       "thread-account3"
                 :username AUTHOR
                 :status   :success}}}))

(ws/defcard logged-out-thread
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root
               Root

               ::ct.fulcro/wrap-root?
               false

               ::ct.fulcro/initial-state
               {:thread
                {:slug        "lorem-ipsum4"
                 :title       TITLE
                 :body        BODY
                 :created-at  CREATED-AT
                 :author      AUTHOR
                 :score       SCORE
                 :vote-status VOTE-STATUS
                 :comments    (mapv #(comp/get-initial-state comment/Comment %) COMMENTS)}

                :credentials
                {:status :logged-out}}}))

(ws/defcard thread-with-comment-load-failed
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:thread      {:slug         "lorem-ipsum5"
                                                        :title        TITLE
                                                        :body         BODY
                                                        :created-at   CREATED-AT
                                                        :author       AUTHOR
                                                        :score        SCORE
                                                        :vote-status  VOTE-STATUS
                                                        :load-failed? true}

                                          :credentials {:id       "thread-account4"
                                                        :username AUTHOR
                                                        :status   :success}}}))

(ws/defcard deleted-thread
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root
               Root

               ::ct.fulcro/wrap-root?
               false

               ::ct.fulcro/initial-state
               {:thread
                {:slug        "lorem-ipsum6"
                 :title       TITLE
                 :body        BODY
                 :created-at  CREATED-AT
                 :author      AUTHOR
                 :deleted-at  DELETED-AT
                 :score       SCORE
                 :vote-status VOTE-STATUS
                 :comments    (mapv #(comp/get-initial-state comment/Comment %) COMMENTS)}

                :credentials
                {:id       "thread-account5"
                 :username AUTHOR
                 :status   :success}}}))

(ws/defcard thread-with-load-more-button
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root
               Root

               ::ct.fulcro/wrap-root?
               false

               ::ct.fulcro/initial-state
               {:thread
                {:slug                "lorem-ipsum7"
                 :title               TITLE
                 :body                BODY
                 :created-at          CREATED-AT
                 :author              AUTHOR
                 :score               SCORE
                 :vote-status         VOTE-STATUS
                 :next-comment-cursor "thread-comment1="
                 :comments            (mapv #(comp/get-initial-state comment/Comment %) COMMENTS)}

                :credentials
                {:id       "thread-account6"
                 :username AUTHOR
                 :status   :success}}}))

(ws/defcard editing-thread
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root
               Root

               ::ct.fulcro/wrap-root?
               false

               ::ct.fulcro/initial-state
               {:thread
                {:slug             "lorem-ipsum8"
                 :title            TITLE
                 :body             BODY
                 :created-at       CREATED-AT
                 :author           AUTHOR
                 :score            SCORE
                 :vote-status      VOTE-STATUS
                 :comments         (mapv #(comp/get-initial-state comment/Comment %) COMMENTS)
                 :edit-thread-form (thread/thread-path "lorem-ipsum8")}

                :credentials
                {:id       "thread-account7"
                 :username AUTHOR
                 :status   :success}}}))
