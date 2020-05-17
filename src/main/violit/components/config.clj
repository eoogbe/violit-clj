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
(ns violit.components.config
  (:require
    [com.fulcrologic.fulcro.server.config :as fserver]
    [mount.core :refer [defstate args]]
    [taoensso.timbre :as log]))

(defn configure-logging!
  [{:taoensso.timbre/keys [config]}]
  (log/merge-config! config)
  (log/debug "Configured Timbre with" config))

(defstate config
  :start (let [{:keys [config] :or {config "config/dev.edn"}} (args)
               loaded-config (fserver/load-config! {:config-path config})]
           (configure-logging! loaded-config)
           (log/info "Loading config" config)
           loaded-config))
