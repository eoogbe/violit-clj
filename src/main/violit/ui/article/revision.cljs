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
(ns violit.ui.article.revision
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    ["diff" :as diff]
    [tick.alpha.api :as t]
    [violit.ui.core.theme :as theme]))

(defn ui-old-change
  [idx {:keys [added removed value]}]
  (when-not added
    (dom/span
      {:classes [(when removed :.removed)]
       :key     idx}
      value)))

(defn ui-new-change
  [idx {:keys [added removed value]}]
  (when-not removed
    (dom/span
      {:classes [(when added :.added)]
       :key     idx}
      value)))

(defsc Revision [_this {:revision/keys [updated-at old-text new-text]}]
  {:query         [:revision/updated-at :revision/old-text :revision/new-text]
   :ident         :revision/updated-at
   :initial-state {:revision/updated-at :param/updated-at
                   :revision/old-text   :param/old-text
                   :revision/new-text   :param/new-text}
   :css           [[:.revision {:margin [[theme/spacing-lg 0]]}]
                   [:.header {:margin-bottom theme/spacing-sm
                              :color         theme/gray700}]
                   [:.diff {:display "flex"}]
                   [:.old-changes {:width        "calc(50% - 0.5em)"
                                   :margin-right theme/spacing-lg}]
                   [:.new-changes {:width "calc(50% - 0.5em)"}]
                   [:.removed {:background-color theme/error-color-light
                               :color            theme/error-color-darkest
                               :text-decoration  "line-through"}]
                   [:.added {:background-color theme/success-color-light
                             :color            theme/success-color-dark}]]}
  (let [changes (js->clj (diff/diffWords (or old-text "") (or new-text "")) :keywordize-keys true)]
    (dom/li
      :.revision
      (dom/article
        (dom/header
          :.header
          "Edited "
          (dom/time
            {:dateTime (t/format :iso-instant updated-at)}
            (.toLocaleString (t/inst updated-at))))
        (dom/div
          :.diff
          (dom/section
            :.old-changes
            (map-indexed ui-old-change changes))
          (dom/section
            :.new-changes
            (map-indexed ui-new-change changes)))))))

(def ui-revision (comp/factory Revision {:keyfn :revision/updated-at}))
