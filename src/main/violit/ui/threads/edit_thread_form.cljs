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
(ns violit.ui.threads.edit-thread-form
  (:require
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [violit.model.thread :as model]
    [violit.schema.thread :as thread]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]))

(defsc EditThreadForm [this {::thread/keys [slug body] :as props}]
  {:query         [::thread/slug ::thread/body fs/form-config-join]
   :ident         ::thread/slug
   :form-fields   #{::thread/body}
   :initial-state {::thread/slug :param/slug
                   ::thread/body :param/body}
   :css           [[:.form (merge styles/form {:margin [[theme/spacing-md 0]]})]
                   [:.field-list styles/list-reset]
                   [:.input styles/input]
                   [:.button-panel (merge styles/list-reset {:display         "flex"
                                                             :justify-content "flex-end"})]
                   [:.submit-button styles/button-submit
                    [:&:disabled styles/button-disabled]]
                   [:.cancel-button (merge styles/button-secondary
                                           {:margin-left theme/spacing-md})]]}
  (let [handle-change (fn [evt]
                        (m/set-string! this ::thread/body :event evt)
                        (comp/transact! this [(fs/mark-complete! {:field ::thread/text})]))
        handle-escape (fn [evt]
                        (when (evt/escape? evt)
                          (comp/transact! this [(model/close-edit-thread-form {:slug slug})])))
        handle-update-thread (fn [evt]
                               (evt/prevent-default! evt)
                               (comp/transact! this [(model/update-thread
                                                       {:slug slug
                                                        :diff (fs/dirty-fields props true)})]))]
    (dom/form
      :.form
      {:autoComplete "off"
       :onKeyDown    handle-escape
       :onSubmit     handle-update-thread}
      (dom/ul
        :.field-list
        (dom/li
          (dom/textarea :.input
                        {:autoFocus   true
                         :onChange    handle-change
                         :placeholder "Edit thread"
                         :rows        5
                         :value       (or body "")})))
      (dom/ul
        :.button-panel
        (dom/li
          (dom/button :.submit-button {:type "submit"} "Save"))
        (dom/li
          (dom/button
            :.cancel-button
            {:onClick #(comp/transact! this [(model/close-edit-thread-form {:slug slug})])
             :type    "button"}
            "Cancel"))))))

(def ui-edit-thread-form (comp/factory EditThreadForm))
