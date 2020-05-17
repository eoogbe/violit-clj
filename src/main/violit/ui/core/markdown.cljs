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
(ns violit.ui.core.markdown
  (:require
    [com.fulcrologic.fulcro.algorithms.react-interop :as interop]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [oops.core :refer [oget]]
    ["react-markdown" :as BaseMarkdown]
    [violit.ui.core.link :as link]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]))

(def ui-base-markdown (interop/react-factory BaseMarkdown))

(defsc Markdown [_this props]
  {:css [[:.markdown
          [:a styles/link]
          [:blockquote {:margin       theme/spacing-md
                        :padding-left theme/spacing-md
                        :border-left  [["2px" "solid" theme/light-blue800]]}]
          [:h1 :h2 :h3 :h4 :h5 :h6 {:font-size   "1.25rem"
                                    :font-weight 400
                                    :margin      [["0.4em" 0]]}]
          [:p {:margin [[theme/spacing-md 0]]}]]]}
  (letfn [(link-renderer [props]
            (dom/a
              {:rel "ugc" :href (oget props :href)}
              (oget props :children)))]
    (dom/div
      :.markdown
      (ui-base-markdown
        (assoc props :renderers #js {:link link-renderer})))))

(def ui-markdown (comp/factory Markdown))
