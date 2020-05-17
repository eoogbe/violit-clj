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
(ns user
  (:require
    [clojure.tools.namespace.repl :as tools-ns :refer [set-refresh-dirs]]
    [mount.core :as mount]
    [violit.components.http-server]))

(set-refresh-dirs "src/main" "src/dev")

(defn start []
  (mount/start-with-args {:config "config/dev.edn"})
  :ok)

(defn stop []
  (mount/stop))

(defn restart
  "Stop, reload code, and restart the server. If there is a compile error, use:
  ```
  (tools-ns/refresh)
  ```
  to recompile, and then use `start` once things are good."
  []
  (stop)
  (tools-ns/refresh :after 'user/start))

(comment
  (start)
  (stop)
  (restart)
  (tools-ns/refresh))
