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
    [com.wsscode.pathom.connect :as pc :refer [defresolver]]
    [violit.components.database :as db]
    [tick.alpha.api :as t]
    [violit.db.board :as db.board]
    [violit.schema.board :as board]
    [violit.schema.thread :as thread]
    [violit.utils.base64 :as base64]))

(def THREAD-PAGE-PERIOD (t/new-period 2 :days))

(defresolver board-resolver [{::db/keys [conn]} _input]
  {::pc/output [::board/name ::board/description]}
  (db.board/get-board conn))

(defresolver board-thread-page-resolver [{::db/keys [conn] :as env} _input]
  {::pc/output [::board/created-threads-before
                ::board/next-thread-cursor
                {::board/threads [::thread/slug ::thread/cursor]}]}
  (let [{:keys [before after]} (:query-params env)
        before (or before (-> (t/now) (t/<< THREAD-PAGE-PERIOD)))
        after (if after
                (base64/decode after)
                (str (t/now)))
        thread-page (db.board/get-board-thread-page
                      conn {:before before
                            :after  after})
        thread-page (mapv #(update % ::thread/cursor base64/encode) thread-page)
        next-thread (db.board/get-board-thread-created-before conn before)
        created-threads-before (some-> next-thread ::thread/created-at (t/<< THREAD-PAGE-PERIOD))
        next-cursor (some-> next-thread ::thread/cursor base64/encode)]
    {::board/created-threads-before created-threads-before
     ::board/next-thread-cursor     next-cursor
     ::board/threads                thread-page}))

(def resolvers [board-resolver board-thread-page-resolver])
