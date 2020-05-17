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
(ns violit.comments.comment-cards
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.model.session :as session]
    [violit.schema.comment :as comment]
    [violit.ui.comments.comment :as ui.comment]
    [violit.ui.core.dropdown-menu :as dropdown-menu]
    [violit.ui.core.markdown :as markdown]
    [violit.ui.core.styles :as styles]))

(def TEXT "Sed do eiusmod tempor incididunt ut labore et dolore.")

(def CREATED-AT "2020-06-14T12:49:46.000Z")

(def AUTHOR "fred-astaire")

(def DELETED-AT "2020-06-14T12:50:46.000Z")

(def SCORE 1)

(def VOTE-STATUS :not-voted)

(defsc Root [_this {:keys [comment]}]
  {:query         [{:comment (comp/get-query ui.comment/Comment)}
                   {:ui/current-credentials (comp/get-query session/Credentials)}]
   :initial-state (fn [{:keys [comment credentials]}]
                    {:comment
                     (comp/get-initial-state ui.comment/Comment comment)

                     :ui/current-credentials
                     (comp/get-initial-state session/Credentials credentials)})
   :css           [[:* {:box-sizing "border-box"}]
                   [:.root styles/body]]
   :css-include   [dropdown-menu/DropdownMenu markdown/Markdown]}
  (dom/div
    :.root
    (ui.comment/ui-comment comment)
    (inj/style-element {:component Root})))

(ws/defcard comment-without-highlight
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:comment     {:id          "comment1"
                                                        :text        TEXT
                                                        :created-at  CREATED-AT
                                                        :author      AUTHOR
                                                        :score       SCORE
                                                        :vote-status VOTE-STATUS}
                                          :credentials {:status :logged-out}}}))

(ws/defcard comment-with-highlight
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:comment     {:id              "comment2"
                                                        :text            TEXT
                                                        :created-at      CREATED-AT
                                                        :author          AUTHOR
                                                        :score           SCORE
                                                        :vote-status     VOTE-STATUS
                                                        :last-created-id "comment2"}
                                          :credentials {:status :logged-out}}}))

(ws/defcard deleted-comment
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:comment     {:id          "comment3"
                                                        :text        TEXT
                                                        :created-at  CREATED-AT
                                                        :author      AUTHOR
                                                        :deleted-at  DELETED-AT
                                                        :score       SCORE
                                                        :vote-status VOTE-STATUS}
                                          :credentials {:status :logged-out}}}))

(ws/defcard logged-in-comment
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root          Root
               ::ct.fulcro/wrap-root?    false
               ::ct.fulcro/initial-state {:comment     {:id          "comment4"
                                                        :text        TEXT
                                                        :created-at  CREATED-AT
                                                        :author      AUTHOR
                                                        :score       SCORE
                                                        :vote-status VOTE-STATUS}
                                          :credentials {:id       "account1"
                                                        :username AUTHOR
                                                        :status   :success}}}))

(ws/defcard editing-comment
            (ct.fulcro/fulcro-card
              {::ct.fulcro/root
               Root

               ::ct.fulcro/wrap-root?
               false

               ::ct.fulcro/initial-state
               {:comment     {:id                "comment5"
                              :text              TEXT
                              :created-at        CREATED-AT
                              :author            AUTHOR
                              :score             SCORE
                              :vote-status       VOTE-STATUS
                              :edit-comment-form (comment/comment-path "comment5")}
                :credentials {:id       "account2"
                              :username AUTHOR
                              :status   :success}}}))
