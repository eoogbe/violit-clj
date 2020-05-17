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
(ns violit.ui.article.arrows
  (:require
    [com.fulcrologic.fulcro-css.localized-dom :as dom]))

(defn ui-arrow-up
  [{:keys [title id]}]
  (let [arrowhead-id (str id "-arrowhead")]
    (dom/svg
      {:height  18
       :role    "img"
       :version "1.1"
       :viewBox "0 0 18 18"
       :width   18
       :xmlns   "http://www.w3.org/2000/svg"}
      (dom/title title)
      (dom/defs
        (dom/marker
          {:id           arrowhead-id
           :markerHeight 2
           :markerWidth  1
           :orient       "auto-start-reverse"
           :refX         0
           :refY         1}
          (dom/polygon {:fill   "currentColor"
                        :points "0 0, 1 1, 0 2"})))
      (dom/line {:markerStart (str "url(#" arrowhead-id ")")
                 :stroke      "currentColor"
                 :strokeWidth 8
                 :x1          9
                 :x2          9
                 :y1          10
                 :y2          18}))))

(defn ui-arrow-down
  [{:keys [title id]}]
  (let [arrowhead-id (str id "-arrowhead")]
    (dom/svg
      {:height  18
       :role    "img"
       :version "1.1"
       :viewBox "0 0 18 18"
       :width   18
       :xmlns   "http://www.w3.org/2000/svg"}
      (dom/title title)
      (dom/defs
        (dom/marker
          {:id           arrowhead-id
           :markerHeight 2
           :markerWidth  1
           :orient       "auto"
           :refX         0
           :refY         1}
          (dom/polygon {:fill   "currentColor"
                        :points "0 0, 1 1, 0 2"})))
      (dom/line {:markerEnd   (str "url(#" arrowhead-id ")")
                 :stroke      "currentColor"
                 :strokeWidth 8
                 :x1          9
                 :x2          9
                 :y1          3
                 :y2          11}))))
