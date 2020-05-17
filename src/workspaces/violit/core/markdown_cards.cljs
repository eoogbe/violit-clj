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
(ns violit.core.markdown-cards
  (:require
    [com.fulcrologic.fulcro.components :refer [defsc]]
    [com.fulcrologic.fulcro-css.css-injection :as inj]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [nubank.workspaces.card-types.fulcro3 :as ct.fulcro]
    [nubank.workspaces.core :as ws]
    [violit.ui.core.markdown :as markdown]
    [violit.ui.core.styles :as styles]
    [violit.utils.string :as str]))

(def SOURCE
  (str/multiline
    "# Heading"
    "## Heading 2"
    ""
    "The sentence of the first paragraph."
    ""
    "The sentence of the second paragraph."
    ""
    "**bold**  "
    "*italic*  "
    "[link](http://example.com)  "
    "`inline code`  "
    "~~strikethrough~~"
    ""
    "> blockquote"
    ""
    "----------"
    ""
    "* list item 1"
    "* list item 2"
    "  * nested list item"
    "* list item 3"
    ""
    "1. ordered list item 1"
    "2. ordered list item 2"
    "3. ordered list item 3"))

(defsc Root [_this _props]
  {:css         [[:* {:box-sizing "border-box"}]
                 [:.root styles/body]
                 [:code {:font-family "'Roboto Mono', monospace"}]]
   :css-include [markdown/Markdown]}
  (dom/div
    :.root
    (markdown/ui-markdown {:source SOURCE})
    (inj/style-element {:component Root})))

(ws/defcard markdown
            (ct.fulcro/fulcro-card {::ct.fulcro/root       Root
                                    ::ct.fulcro/wrap-root? false}))
