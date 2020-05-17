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
(ns violit.ui.root
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [violit.model.session :as session]
    [violit.ui.app.app-bar :as app-bar]
    [violit.ui.app.main-router :as main-router]
    [violit.ui.core.alert :as alert]
    [violit.ui.core.dropdown-menu :as dropdown-menu]
    [violit.ui.core.loading-indicator :as loading-indicator]
    [violit.ui.core.main-container :as main-container]
    [violit.ui.core.markdown :as markdown]
    [violit.ui.core.not-found-page :as not-found-page]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]
    [violit.ui.dialog.dialog :as dialog]))

(defsc Root [_this {:root/keys [router app-bar alert loading-indicator]}]
  {:query         [{:root/router (comp/get-query main-router/MainRouter)}
                   {:root/app-bar (comp/get-query app-bar/AppBar)}
                   {:root/alert (comp/get-query alert/Alert)}
                   {:root/loading-indicator (comp/get-query loading-indicator/LoadingIndicator)}
                   {:ui/current-credentials (comp/get-query session/Credentials)}]
   :initial-state {:root/router            {}
                   :root/app-bar           {}
                   :root/alert             {}
                   :root/loading-indicator {:visible? true}
                   :ui/current-credentials {}}
   :css           [[:* {:box-sizing "border-box"}]
                   [:body styles/body]
                   [:code {:font-family "'Roboto Mono', monospace"}]
                   [:.app {:display          "flex"
                           :flex-direction   "column"
                           :align-items      "center"
                           :min-height       "100vh"
                           :background-color theme/gray200}]]
   :css-include   [dialog/Dialog
                   dropdown-menu/DropdownMenu
                   main-container/MainContainer
                   markdown/Markdown
                   not-found-page/NotFoundPage]}
  (dom/div
    :.app
    (app-bar/ui-app-bar app-bar)
    (alert/ui-alert alert)
    (loading-indicator/ui-loading-indicator loading-indicator)
    (main-router/ui-main-router router)
    (inj/style-element {:component Root})))
