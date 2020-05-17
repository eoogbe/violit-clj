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
(ns violit.model.thread
  (:require
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [taoensso.timbre :as log]
    [tick.alpha.api :as t]
    [violit.logger :as logger]
    [violit.lib.routing :as routing]
    [violit.schema.board :as board]
    [violit.schema.comment :as comment]
    [violit.schema.thread :as thread]
    [violit.ui.article.article-footer :as article-footer]
    [violit.ui.article.article-header :as article-header]
    [violit.ui.comments.comment :as ui.comment]
    [violit.ui.core.alert :as alert]
    [violit.ui.core.loading-indicator :as loading-indicator]))

(defn thread-form-path
  ([] [:component/id ::ThreadForm])
  ([field] [:component/id ::ThreadForm field]))

(defsc CommentPage [_this _props]
  {:query [::thread/slug
           ::thread/next-comment-cursor
           {::thread/comments (comp/get-query ui.comment/Comment)}]
   :ident [::thread/comment-page ::thread/slug]})

(defn integrate-comment-page*
  [state {:keys [slug targeting]}]
  (let [{::thread/keys [comments next-comment-cursor]} (get-in state [::thread/comment-page slug])
        update-comments (if (= targeting :append)
                          #(-> % (concat comments) vec)
                          (constantly comments))]
    (-> state
        (update-in (thread/thread-path slug ::thread/comments) update-comments)
        (assoc-in (thread/thread-path slug ::thread/next-comment-cursor) next-comment-cursor)
        (loading-indicator/set-visible* {:visible? false}))))

(defmutation integrate-comment-page [params]
  (action [{:keys [state]}]
          (swap! state integrate-comment-page* params)))

(defn load-comments!
  [app-or-comp {:keys [slug after targeting]}]
  (let [comment-page-ident (comp/get-ident CommentPage {::thread/slug slug})]
    (comp/transact! app-or-comp [(loading-indicator/set-visible {:visible? true})])
    (df/load! app-or-comp comment-page-ident CommentPage
              {:marker               ::comment-page
               :params               {:after after}
               :post-mutation        `integrate-comment-page
               :post-mutation-params {:slug      slug
                                      :targeting targeting}})))

(defn clear-new-comment-form*
  [state {:keys [slug]}]
  (let [new-comment-form-class (comp/registry-key->class
                                 :violit.ui.comments.new-comment-form/NewCommentForm)
        new-comment-form-path (thread/thread-path slug :ui/new-comment-form)
        new-comment-form-ident (get-in state new-comment-form-path)]
    (if new-comment-form-ident
      (let [comment-form (get-in state new-comment-form-ident)]
        (-> state
            (merge/merge-component new-comment-form-class
                                   (merge comment-form {::comment/text ""}))
            (fs/clear-complete* new-comment-form-ident)))
      (merge/merge-component state new-comment-form-class {}
                             :replace new-comment-form-path))))

(defmutation clear-new-comment-form [params]
  (action [{:keys [state]}]
          (swap! state clear-new-comment-form* params)))

(defn create-thread*
  [state]
  (let [thread-form-class (comp/registry-key->class
                            :violit.ui.threads.new-thread-form/NewThreadForm)]
    (-> state
        (loading-indicator/set-visible* true)
        (merge/merge-component thread-form-class {}))))

(defmutation create-thread [params]
  (action [{:keys [state]}]
          (logger/log-entity "Creating thread for" params)
          (swap! state #(-> %
                            (loading-indicator/set-visible* {:visible? true})
                            create-thread*)))
  (ok-action [{:keys [state app]}]
             (let [[_ slug] (get-in @state (thread-form-path :ui/thread-result))
                   thread-class (comp/registry-key->class :violit.ui.threads.thread/Thread)]
               (routing/replace-route! app {:route-targets [thread-class slug]})))
  (remote [env]
          (let [thread-class (comp/registry-key->class :violit.ui.threads.thread/Thread)]
            (-> env
                (m/returning thread-class)
                (m/with-target (thread-form-path :ui/thread-result))))))

(defn delete-thread*
  [state {:keys [slug]}]
  (log/info "Deleting thread" slug)
  (-> state
      alert/hide-alert*
      (assoc-in (thread/thread-path slug ::thread/deleted-at) (t/now))
      (merge/remove-ident* (thread/thread-path slug) (board/board-path ::board/threads))))

(defmutation delete-thread [params]
  (action [{:keys [state]}]
          (swap! state delete-thread* params))
  (remote [_env] true))

(defmutation start-delete-thread [{:keys [slug]}]
  (action [{:keys [state app]}]
          (log/info "Start deleting thread" slug)
          (let [timeout-id (.setTimeout js/window
                                        #(comp/transact! app [(delete-thread {:slug slug})]) 5000)]
            (swap! state alert/show-alert* {:message         "Undo delete thread"
                                            :hide-timeout-id timeout-id}))))

(defn close-edit-thread-form*
  [state {:keys [slug]}]
  (log/info "Closing edit thread form for" slug)
  (-> state
      (update-in (thread/thread-path slug) dissoc :ui/edit-thread-form)
      (fs/pristine->entity* (thread/thread-path slug))))

(defmutation close-edit-thread-form [params]
  (action [{:keys [state]}]
          (swap! state close-edit-thread-form* params)))

(defn open-edit-thread-form*
  [state {:keys [slug]}]
  (log/info "Opening edit thread form for" slug)
  (let [edit-thread-form-class
        (comp/registry-key->class :violit.ui.threads.edit-thread-form/EditThreadForm)

        edit-thread-form-ident (thread/thread-path slug)]
    (-> state
        (fs/add-form-config* edit-thread-form-class edit-thread-form-ident)
        (fs/mark-complete* edit-thread-form-ident)
        (assoc-in (thread/thread-path slug :ui/edit-thread-form) edit-thread-form-ident))))

(defmutation open-edit-thread-form [params]
  (action [{:keys [state]}]
          (swap! state open-edit-thread-form* params)))

(defn update-thread*
  [state {:keys [slug diff] :as params}]
  (log/info "Updating thread" params)
  (let [thread-edit-history-class (comp/registry-key->class
                                    :violit.ui.threads.thread-edit-history/ThreadEditHistory)
        cursor (get-in state (thread/thread-path slug ::thread/cursor))
        article-header-ident (get-in state (thread/thread-path slug :ui/article-header))
        updated-at (t/now)
        new-body (-> diff first second ::thread/body :after thread/mk-body)]
    (-> state
        (update-in article-header-ident
                   merge {::article-header/updated-at      updated-at
                          ::article-header/edit-history-rt [thread-edit-history-class cursor]})
        (fs/entity->pristine* (thread/thread-path slug))
        (update-in (thread/thread-path slug) #(-> %
                                                  (dissoc :ui/edit-thread-form)
                                                  (assoc ::thread/updated-at updated-at
                                                         ::thread/body new-body))))))

(defmutation update-thread [params]
  (action [{:keys [state]}]
          (swap! state update-thread* params))
  (remote [_env] true))

(defn upvote-thread*
  [state {:keys [slug]}]
  (log/info "Upvoting thread" slug)
  (let [vote-status (get-in state (thread/thread-path slug ::thread/vote-status))
        article-header-ident (get-in state (thread/thread-path slug :ui/article-header))
        article-header-score-path (conj article-header-ident ::article-header/score)
        article-footer-ident (get-in state (thread/thread-path slug :ui/article-footer))
        article-footer-vote-status-path (conj article-footer-ident ::article-footer/vote-status)]
    (case vote-status
      :upvoted (-> state
                   (update-in article-header-score-path dec)
                   (update-in (thread/thread-path slug ::thread/score) dec)
                   (assoc-in article-footer-vote-status-path :not-voted)
                   (assoc-in (thread/thread-path slug ::thread/vote-status) :not-voted))
      :downvoted (-> state
                     (update-in article-header-score-path + 2)
                     (update-in (thread/thread-path slug ::thread/score) + 2)
                     (assoc-in article-footer-vote-status-path :upvoted)
                     (assoc-in (thread/thread-path slug ::thread/vote-status) :upvoted))
      :not-voted (-> state
                     (update-in article-header-score-path inc)
                     (update-in (thread/thread-path slug ::thread/score) inc)
                     (assoc-in article-footer-vote-status-path :upvoted)
                     (assoc-in (thread/thread-path slug ::thread/vote-status) :upvoted)))))

(defmutation upvote-thread [params]
  (action [{:keys [state]}]
          (swap! state upvote-thread* params))
  (remote [_env] true))

(defn downvote-thread*
  [state {:keys [slug]}]
  (log/info "Downvoting thread" slug)
  (let [vote-status (get-in state (thread/thread-path slug ::thread/vote-status))
        article-header-ident (get-in state (thread/thread-path slug :ui/article-header))
        article-header-score-path (conj article-header-ident ::article-header/score)
        article-footer-ident (get-in state (thread/thread-path slug :ui/article-footer))
        article-footer-vote-status-path (conj article-footer-ident ::article-footer/vote-status)]
    (case vote-status
      :upvoted (-> state
                   (update-in article-header-score-path - 2)
                   (update-in (thread/thread-path slug ::thread/score) - 2)
                   (assoc-in article-footer-vote-status-path :downvoted)
                   (assoc-in (thread/thread-path slug ::thread/vote-status) :downvoted))
      :downvoted (-> state
                     (update-in article-header-score-path inc)
                     (update-in (thread/thread-path slug ::thread/score) inc)
                     (assoc-in article-footer-vote-status-path :not-voted)
                     (assoc-in (thread/thread-path slug ::thread/vote-status) :not-voted))
      :not-voted (-> state
                     (update-in article-header-score-path dec)
                     (update-in (thread/thread-path slug ::thread/score) dec)
                     (assoc-in article-footer-vote-status-path :downvoted)
                     (assoc-in (thread/thread-path slug ::thread/vote-status) :downvoted)))))

(defmutation downvote-thread [params]
  (action [{:keys [state]}]
          (swap! state downvote-thread* params))
  (remote [_env] true))
