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
(ns violit.db.board
  (:require
    [crux.api :as crux]
    [violit.logger :as logger]
    [violit.schema.board :as board]
    [violit.schema.thread :as thread]
    [violit.utils.voting :as voting]))

(defn get-board
  [conn]
  (crux/entity (crux/db conn) ::board/Board))

(defn get-board-thread-page
  [conn {:keys [before after]}]
  (let [q (crux/q (crux/db conn)
                  {:find     '[id cursor]
                   :where    '[[t ::thread/id id]
                               [t ::thread/cursor cursor]
                               [t ::thread/created-at created-at]
                               [(<= cursor after)]
                               [(tick.alpha.api/>= created-at before)]]
                   :order-by '[[cursor :desc]]
                   :args     [{'before before 'after after}]})]
    (->> q
         (map (fn [[id]]
                (let [{::thread/keys [upvoters downvoters] :as thread}
                      (crux/entity (crux/db conn) id)

                      ci-lower-bound (voting/ci-lower-bound upvoters downvoters)]
                  (assoc thread ::thread/ci-lower-bound ci-lower-bound))))
         (filter (fn [{::thread/keys [deleted-at]}] (not deleted-at)))
         (sort-by (juxt ::thread/ci-lower-bound ::thread/cursor))
         reverse
         (into [] (map #(select-keys % [::thread/slug ::thread/cursor]))))))

(defn get-board-thread-created-before
  [conn instant]
  (when-first [[id] (crux/q (crux/db conn)
                            {:find     '[id cursor]
                             :where    '[[t ::thread/id id]
                                         [t ::thread/cursor cursor]
                                         [t ::thread/created-at created-at]
                                         [(tick.alpha.api/< created-at instant)]]
                             :order-by '[[cursor :desc]]
                             :limit    1
                             :args     [{'instant instant}]})]
    (crux/entity (crux/db conn) id)))

(defn create-board
  [conn board]
  (logger/log-entity "Creating board" board)
  (let [new-board (assoc board :crux.db/id ::board/Board)]
    (crux/submit-tx conn [[:crux.tx/put new-board]])))
