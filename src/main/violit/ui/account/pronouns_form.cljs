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
(ns violit.ui.account.pronouns-form
  (:require
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [violit.schema.account :as account]
    [violit.model.account :as model]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]))

(defsc PronounsForm [this {::account/keys [username pronouns] :as props}]
  {:query         [::account/username ::account/pronouns fs/form-config-join]
   :ident         ::account/username
   :form-fields   #{::account/pronouns}
   :initial-state {::account/username :param/username
                   ::account/pronouns :param/pronouns}
   :css           [[:.form (merge styles/form {:width "16em"})]
                   [:.field-list styles/list-reset]
                   [:.input styles/input]
                   [:.button-panel (merge styles/list-reset {:display         "flex"
                                                             :justify-content "flex-end"})]
                   [:.submit-button styles/button-primary]
                   [:.cancel-button (merge styles/button-secondary
                                           {:margin-left theme/spacing-md})]]}
  (let [handle-change (fn [evt]
                        (m/set-string! this ::account/pronouns :event evt)
                        (comp/transact! this [(fs/mark-complete! {:field ::account/pronouns})]))
        handle-escape (fn [evt]
                        (when (evt/escape? evt)
                          (comp/transact!
                            this [(model/close-pronouns-form {:username username})])))
        handle-update (fn [evt]
                        (evt/prevent-default! evt)
                        (comp/transact! this [(model/update-pronouns
                                                {:username username
                                                 :diff     (fs/dirty-fields props true)})]))]
    (dom/form
      :.form
      {:onKeyDown handle-escape
       :onSubmit  handle-update}
      (dom/ul
        :.field-list
        (dom/li
          (dom/input :.input
                     {:aria-required "true"
                      :autoFocus     true
                      :onChange      handle-change
                      :placeholder   "e.g. he/him, she/her, or they/them"
                      :type          "text"
                      :value         (or pronouns "")})))
      (dom/ul
        :.button-panel
        (dom/li
          (dom/button :.submit-button {:type "submit"} "Save"))
        (dom/li
          (dom/button
            :.cancel-button
            {:onClick #(comp/transact! this [(model/close-pronouns-form {:username username})])
             :type    "button"}
            "Cancel"))))))

(def ui-pronouns-form (comp/factory PronounsForm))
