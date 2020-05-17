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
(ns violit.model.board
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]
    [violit.schema.board :as board]
    [violit.ui.core.loading-indicator :as loading-indicator]
    [violit.ui.threads.thread :as thread]))

(defn thread-page-path
  ([] [::board/thread-page ::ThreadPage])
  ([field] [::board/thread-page ::ThreadPage field]))

(defsc ThreadPage [_this _props]
  {:query [::board/created-threads-before
           ::board/next-thread-cursor
           {::board/threads (comp/get-query thread/Thread)}]
   :ident (fn (thread-page-path))})

(defn integrate-board-page*
  [state {:keys [targeting]}]
  (let [{::board/keys [created-threads-before next-thread-cursor threads]}
        (get-in state (thread-page-path))
        update-threads (if (= targeting :append)
                         #(-> % (concat threads) vec)
                         (constantly threads))]
    (-> state
        (update-in (board/board-path ::board/threads) update-threads)
        (assoc-in (board/board-path ::board/created-threads-before) created-threads-before)
        (assoc-in (board/board-path ::board/next-thread-cursor) next-thread-cursor)
        (loading-indicator/set-visible* {:visible? false}))))

(defmutation integrate-board-page [params]
  (action [{:keys [state]}]
          (swap! state integrate-board-page* params)))

(defn load-threads!
  [app-or-comp {:keys [before after targeting]}]
  (comp/transact! app-or-comp [(loading-indicator/set-visible {:visible? true})])
  (df/load! app-or-comp (thread-page-path) ThreadPage
            {:marker               ::thread-page
             :params               {:before before
                                    :after  after}
             :post-mutation        `integrate-board-page
             :post-mutation-params {:targeting targeting}}))
