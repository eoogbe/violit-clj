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
(ns violit.ui.app.main-router
  (:require
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.routing.dynamic-routing :refer [defrouter]]
    [violit.ui.account.profile :as profile]
    [violit.ui.account.signup-form :as signup-form]
    [violit.ui.auth.login-form :as login-form]
    [violit.ui.board.board :as board]
    [violit.ui.comments.comment-edit-history :as comment-edit-history]
    [violit.ui.core.loading-indicator :as loading-indicator]
    [violit.ui.core.main-container :as main-container]
    [violit.ui.core.not-found-page :as not-found-page]
    [violit.ui.threads.new-thread-form :as new-thread-form]
    [violit.ui.threads.thread :as thread]
    [violit.ui.threads.thread-edit-history :as thread-edit-history]))

(defrouter MainRouter [_this {:keys [current-state]}]
  {:router-targets     [board/Board
                        profile/Profile
                        login-form/LoginForm
                        signup-form/SignupForm
                        thread/Thread
                        new-thread-form/NewThreadForm
                        comment-edit-history/CommentEditHistory
                        thread-edit-history/ThreadEditHistory]
   :componentDidUpdate (fn [this {prev-route-state :current-state} _pstate]
                         (let [{:keys [current-state]} (comp/props this)
                               has-loading-indicator? (#{:initial :pending} current-state)]
                           (when-not (= current-state prev-route-state)
                             (comp/transact! this
                                             [(loading-indicator/set-visible
                                                {:visible? has-loading-indicator?})]))))}
  (main-container/ui-main-container
    {}
    (not-found-page/ui-not-found-page
      {:found? (not= current-state :failed)})))

(def ui-main-router (comp/factory MainRouter))
