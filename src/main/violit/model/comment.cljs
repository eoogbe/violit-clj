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
(ns violit.model.comment
  (:require
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [taoensso.timbre :as log]
    [tick.alpha.api :as t]
    [violit.logger :as logger]
    [violit.model.session :as session]
    [violit.schema.account :as account]
    [violit.schema.comment :as comment]
    [violit.schema.thread :as thread]
    [violit.ui.article.article-footer :as article-footer]
    [violit.ui.article.article-header :as article-header]
    [violit.ui.core.alert :as alert]))

(defn remove-last-created-id*
  [state]
  (let [last-created-id (->> state ::comment/id vals (some :ui/last-created-id))]
    (cond-> state
            last-created-id
            (update-in (comment/comment-path last-created-id) dissoc :ui/last-created-id))))

(defmutation remove-last-created-id [_params]
  (action [{:keys [state]}]
          (swap! state remove-last-created-id*)))

(defn create-comment* [state {:keys [id diff thread-slug]}]
  (let [author (get-in state (session/credentials-path ::account/username))
        [_ props] (first diff)
        comment (-> props
                    (assoc
                      ::comment/id id
                      ::comment/author author
                      ::comment/score 1
                      ::comment/vote-status :upvoted
                      :ui/last-created-id id)
                    comment/mk-comment)
        comment-class (comp/registry-key->class :violit.ui.comments.comment/Comment)

        new-comment-form-class
        (comp/registry-key->class :violit.ui.comments.new-comment-form/NewCommentForm)]
    (-> state
        remove-last-created-id*
        (merge/merge-component comment-class comment
                               :prepend (thread/thread-path thread-slug ::thread/comments))
        (merge/merge-component new-comment-form-class {}
                               :replace (thread/thread-path thread-slug :ui/new-comment-form)))))

(defmutation create-comment [params]
  (action [{:keys [state]}]
          (logger/log-entity "Creating comment for" params)
          (swap! state create-comment* params))
  (remote [env]
          (let [comment-class (comp/registry-key->class :violit.ui.comments.comment/Comment)]
            (m/returning env comment-class))))

(defmutation delete-comment [{:keys [id]}]
  (action [{:keys [state]}]
          (log/info "Deleting comment" id)
          (swap! state #(-> %
                            alert/hide-alert*
                            (assoc-in (comment/comment-path id ::comment/deleted-at) (t/now)))))
  (remote [_env] true))

(defmutation start-delete-comment [{:keys [id]}]
  (action [{:keys [state app]}]
          (log/info "Start deleting comment" id)
          (let [timeout-id (.setTimeout js/window
                                        #(comp/transact! app [(delete-comment {:id id})]) 5000)]
            (swap! state alert/show-alert* {:message         "Undo delete comment"
                                            :hide-timeout-id timeout-id}))))

(defn close-edit-comment-form*
  [state {:keys [id]}]
  (log/info "Closing edit comment form for" id)
  (-> state
      (update-in (comment/comment-path id) dissoc :ui/edit-comment-form)
      (fs/pristine->entity* (comment/comment-path id))))

(defmutation close-edit-comment-form [params]
  (action [{:keys [state]}]
          (swap! state close-edit-comment-form* params)))

(defn close-any-edit-comment-form*
  [state]
  (let [editing-id (some->> state
                            ::comment/id
                            vals
                            (some :ui/edit-comment-form)
                            second)]
    (cond-> state
            editing-id (close-edit-comment-form* {:id editing-id}))))

(defmutation close-any-edit-comment-form [_params]
  (action [{:keys [state]}]
          (swap! state close-any-edit-comment-form*)))

(defn open-edit-comment-form*
  [state {:keys [id]}]
  (log/info "Opening edit comment form for" id)
  (let [edit-comment-form-class (comp/registry-key->class
                                  :violit.ui.comments.edit-comment-form/EditCommentForm)
        edit-comment-form-ident (comment/comment-path id)]
    (-> state
        close-any-edit-comment-form*
        (fs/add-form-config* edit-comment-form-class edit-comment-form-ident)
        (fs/mark-complete* edit-comment-form-ident)
        (assoc-in (comment/comment-path id :ui/edit-comment-form) edit-comment-form-ident))))

(defmutation open-edit-comment-form [params]
  (action [{:keys [state]}]
          (swap! state open-edit-comment-form* params)))

(defn update-comment*
  [state {:keys [id] :as params}]
  (log/info "Updating comment" params)
  (let [comment-edit-history-class (comp/registry-key->class
                                     :violit.ui.comments.comment-edit-history/CommentEditHistory)
        cursor (get-in state (comment/comment-path id ::comment/cursor))
        article-header-ident (get-in state (comment/comment-path id :ui/article-header))
        updated-at (t/now)]
    (-> state
        (update-in (comment/comment-path id) #(-> %
                                                  (dissoc :ui/edit-comment-form)
                                                  (assoc ::comment/updated-at updated-at)))
        (update-in article-header-ident
                   merge {::article-header/updated-at      updated-at
                          ::article-header/edit-history-rt [comment-edit-history-class cursor]})
        (fs/entity->pristine* (comment/comment-path id)))))

(defmutation update-comment [params]
  (action [{:keys [state]}]
          (swap! state update-comment* params))
  (remote [_env] true))

(defn upvote-comment*
  [state {:keys [id]}]
  (log/info "Upvoting comment" id)
  (let [vote-status (get-in state (comment/comment-path id ::comment/vote-status))
        article-header-ident (get-in state (comment/comment-path id :ui/article-header))
        article-header-score-path (conj article-header-ident ::article-header/score)
        article-footer-ident (get-in state (comment/comment-path id :ui/article-footer))
        article-footer-vote-status-path (conj article-footer-ident ::article-footer/vote-status)]
    (case vote-status
      :upvoted (-> state
                   (update-in article-header-score-path dec)
                   (update-in (comment/comment-path id ::comment/score) dec)
                   (assoc-in article-footer-vote-status-path :not-voted)
                   (assoc-in (comment/comment-path id ::comment/vote-status) :not-voted))
      :downvoted (-> state
                     (update-in article-header-score-path + 2)
                     (update-in (comment/comment-path id ::comment/score) + 2)
                     (assoc-in article-footer-vote-status-path :upvoted)
                     (assoc-in (comment/comment-path id ::comment/vote-status) :upvoted))
      :not-voted (-> state
                     (update-in article-header-score-path inc)
                     (update-in (comment/comment-path id ::comment/score) inc)
                     (assoc-in article-footer-vote-status-path :upvoted)
                     (assoc-in (comment/comment-path id ::comment/vote-status) :upvoted)))))

(defmutation upvote-comment [params]
  (action [{:keys [state]}]
          (swap! state upvote-comment* params))
  (remote [_env] true))


(defn downvote-comment*
  [state {:keys [id]}]
  (log/info "Downvoting comment" id)
  (let [vote-status (get-in state (comment/comment-path id ::comment/vote-status))
        article-header-ident (get-in state (comment/comment-path id :ui/article-header))
        article-header-score-path (conj article-header-ident ::article-header/score)
        article-footer-ident (get-in state (comment/comment-path id :ui/article-footer))
        article-footer-vote-status-path (conj article-footer-ident ::article-footer/vote-status)]
    (case vote-status
      :upvoted (-> state
                   (update-in article-header-score-path - 2)
                   (update-in (comment/comment-path id ::comment/score) - 2)
                   (assoc-in article-footer-vote-status-path :downvoted)
                   (assoc-in (comment/comment-path id ::comment/vote-status) :downvoted))
      :downvoted (-> state
                     (update-in article-header-score-path inc)
                     (update-in (comment/comment-path id ::comment/score) inc)
                     (assoc-in article-footer-vote-status-path :not-voted)
                     (assoc-in (comment/comment-path id ::comment/vote-status) :not-voted))
      :not-voted (-> state
                     (update-in article-header-score-path dec)
                     (update-in (comment/comment-path id ::comment/score) dec)
                     (assoc-in article-footer-vote-status-path :downvoted)
                     (assoc-in (comment/comment-path id ::comment/vote-status) :downvoted)))))

(defmutation downvote-comment [params]
  (action [{:keys [state]}]
          (swap! state downvote-comment* params))
  (remote [_env] true))
