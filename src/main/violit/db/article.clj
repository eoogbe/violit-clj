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
(ns violit.db.article
  (:require
    [crux.api :as crux]
    [tick.alpha.api :as t]))

(defn get-revisions
  [conn id {:keys [unique-key deleted-at-key updated-at-key]}]
  (let [history (for [{:crux.db/keys [valid-time]} (crux/history conn id)
                      :let [entity (crux/entity (crux/db conn valid-time) id)]]
                  (assoc entity updated-at-key (t/instant valid-time)))
        deleted? (some deleted-at-key history)
        history-with-unique-key (transduce
                                  (comp
                                    (partition-by unique-key)
                                    (map first))
                                  conj
                                  (reverse history))]
    (when (and (not deleted?) (> (count history-with-unique-key) 1))
      (->> history-with-unique-key
           (partition 2 1)
           (map (fn [[old new]]
                  {:revision/updated-at (get new updated-at-key)
                   :revision/old-text   (get old unique-key)
                   :revision/new-text   (get new unique-key)}))
           reverse))))
