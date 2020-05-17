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
(ns violit.ui.core.styles
  (:require
    [violit.ui.core.theme :as theme]))

(def body
  {:font-size               "16px"
   :font-family             theme/body-font
   :width                   "100vw"
   :margin                  0
   :-moz-osx-font-smoothing "grayscale"
   :-webkit-font-smoothing  "antialiased"})

(def button
  {:font-size     "1rem"
   :font-weight   500
   :font-family   theme/body-font
   :min-width     "6em"
   :line-height   "1.5em"
   :padding       [[theme/spacing-sm theme/spacing-md]]
   :border-radius theme/border-radius
   :text-align    "center"
   :cursor        "pointer"})

(def button-primary
  (merge button {:border           "none"
                 :background-color theme/primary-color
                 :color            theme/white}))

(def button-secondary
  (merge button {:border           [["1px" "solid" theme/primary-color]]
                 :background-color "white"
                 :color            theme/primary-color}))

(def button-tertiary
  {:font-size      "1rem"
   :font-weight    500
   :font-family    theme/body-font
   :padding        theme/spacing-sm
   :border         "none"
   :background     "none"
   :color          theme/primary-color
   :text-transform "uppercase"
   :cursor         "pointer"})

(def button-submit
  (merge button-primary {:align-self "flex-end"}))

(def button-disabled
  {:opacity 0.5
   :cursor  "default"})

(def form
  {:display        "flex"
   :flex-direction "column"})

(def form-error
  {:margin        0
   :margin-bottom theme/spacing-md
   :color         theme/error-color-dark})

(def input
  {:font-size     "1rem"
   :font-family   theme/body-font
   :display       "block"
   :width         "100%"
   :margin-bottom theme/spacing-md
   :padding       theme/spacing-md
   :border        [["1px" "solid" theme/gray500]]
   :border-radius theme/border-radius
   :resize        "none"})

(def link
  {:font-size       "1rem"
   :padding         0
   :border          "none"
   :background      "none"
   :color           theme/light-blue800
   :outline         "none"
   :text-decoration "underline"
   :cursor          "pointer"})

(def list-reset
  {:margin     0
   :padding    0
   :list-style "none"})

(def no-list-container
  {:text-align "center"})

(def no-list-paragraph
  {:font-size  "1.25em"
   :margin     0
   :margin-top "0.625em"
   :color      theme/gray700
   :text-align "center"})

(def no-text-paragraph
  {:margin [[theme/spacing-md 0]]
   :color  theme/gray700})

(def page-heading
  {:font-size     "1.75rem"
   :font-weight   400
   :margin        0
   :margin-bottom "0.289em"
   :color         theme/black})

(def page-heading-link
  {:font-size       "1.75rem"
   :font-weight     400
   :margin-bottom   "0.289em"
   :padding         0
   :border          "none"
   :background      "none"
   :color           theme/light-blue800
   :outline         "none"
   :text-decoration "underline"
   :cursor          "pointer"})
