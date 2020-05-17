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
(ns violit.client
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [taoensso.timbre :as log]
    [violit.application :refer [app]]
    [violit.lib.routing :as routing]
    [violit.ui.account.signup-form :as signup-form]
    [violit.ui.auth :as auth]
    [violit.ui.auth.login-form :as login-form]
    [violit.ui.root :as root]))

(defn ^:export init
  []
  (auth/start-session! app)
  (app/mount! app root/Root "app")
  (routing/install-route-history! app {:ignore-targets [[login-form/LoginForm]
                                                        [signup-form/SignupForm]]})
  (log/info "Loaded"))

(defn ^:export refresh
  []
  (app/mount! app root/Root "app")
  (log/info "Hot reload"))
