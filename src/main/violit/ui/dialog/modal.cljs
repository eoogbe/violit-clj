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
(ns violit.ui.dialog.modal
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [goog.dom :as gdom]
    [goog.object :as gobj]
    [oops.core :refer [oget oset!]]
    ["react-dom" :as react-dom])
  (:import
    [goog.dom TagName]))

(def modal-root (oget js/document :body))

(defsc Modal [this _props]
  {:initLocalState       (fn [this _props]
                           (let [modal-el (gdom/createElement TagName.DIV)]
                             (oset! modal-el :id "modal-root")
                             (gobj/set this "modal-el" modal-el))
                           {})
   :componentDidMount    (fn [this]
                           (let [modal-el (gobj/get this "modal-el")]
                             (gdom/appendChild modal-root modal-el)))
   :componentWillUnmount (fn [this]
                           (let [modal-el (gobj/get this "modal-el")]
                             (gdom/removeNode modal-el)))}
  (let [modal-el (gobj/get this "modal-el")]
    (react-dom/createPortal (comp/children this) modal-el)))

(def ui-modal (comp/factory Modal))
