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
(ns violit.components.database
  (:require
    [clojure.java.io :as io]
    [clojure.pprint :as pprint]
    [crux.api :as crux]
    [mount.core :refer [defstate]]
    [taoensso.timbre :as log]
    [violit.components.config :as config]
    [violit.db.seed :as seed]))

(defstate conn
  :start (let [{:crux.api/keys [config]} config/config
               node (crux/start-node {:crux.node/topology '[crux.standalone/topology]
                                      :crux.kv/db-dir     (-> config :db-dir io/file str)})]
           (log/info "Starting database server with config"
                     (with-out-str (pprint/pprint config)))
           (seed/seed! node)
           node)
  :stop (.close conn))
