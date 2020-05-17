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
(ns violit.ui.core.not-found-page
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [violit.ui.core.link :as link]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]
    [violit.ui.core.title :as title]))

(defsc NotFoundPage [this {:keys [found?]}]
  {:componentDidMount (fn [this]
                        (let [{:keys [found?]} (comp/props this)]
                          (when-not found?
                            (title/set-title! "Page not found"))))
   :css               [[:.container {:padding theme/spacing-md}
                        [:p {:margin [[theme/spacing-md 0]]}]
                        [:a styles/link]]
                       [:.heading styles/page-heading]]}
  (if found?
    (comp/children this)
    (dom/div
      :.container
      (dom/h2 :.heading "Page not found")
      (dom/p
        (link/ui-link {:route [""]} "Return to home")))))

(def ui-not-found-page (comp/factory NotFoundPage))
