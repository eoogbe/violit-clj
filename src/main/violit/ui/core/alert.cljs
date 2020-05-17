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
(ns violit.ui.core.alert
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [taoensso.timbre :as log]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]))

(defn alert-path
  ([] [:component/id ::Alert])
  ([field] [:component/id ::Alert field]))

(defn show-alert*
  [state {:keys [message hide-timeout-id]}]
  (log/info "Showing alert")
  (update-in state (alert-path) merge {:ui/message         message
                                       :ui/hide-timeout-id hide-timeout-id}))

(defmutation show-alert [params]
  (action [{:keys [state]}]
          (swap! state show-alert* params)))

(defn hide-alert*
  [state]
  (log/info "Hiding alert")
  (let [hide-timeout-id (get-in state (alert-path :ui/hide-timeout-id))]
    (.clearTimeout js/window hide-timeout-id)
    (assoc-in state (alert-path) {})))

(defmutation hide-alert [_params]
  (action [{:keys [state]}]
          (swap! state hide-alert*)))

(defsc Alert [this {:ui/keys [message]}]
  {:query         [:ui/message]
   :ident         (fn [] (alert-path))
   :initial-state {:ui/message :param/message}
   :css           [[:.alert {:position         "fixed"
                             :min-width        "15em"
                             :padding          theme/spacing-md
                             :background-color theme/warning-color
                             :text-align       "center"}]
                   [:.hide-button styles/link]]}
  (when message
    (dom/p
      :.alert
      {:aria-live "assertive"
       :role      "alert"}
      (dom/button
        :.hide-button
        {:onClick #(comp/transact! this [(hide-alert {})])
         :type    "button"}
        message))))

(def ui-alert (comp/factory Alert))
