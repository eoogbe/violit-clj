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
(ns violit.ui.comments.edit-comment-form
  (:require
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [violit.model.comment :as model]
    [violit.schema.comment :as comment]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]))

(defsc EditCommentForm [this {::comment/keys [id text] :as props}]
  {:query         [::comment/id ::comment/text fs/form-config-join]
   :ident         ::comment/id
   :form-fields   #{::comment/text}
   :initial-state {::comment/id   :param/id
                   ::comment/text :param/text}
   :css           [[:.form styles/form]
                   [:.field-list styles/list-reset]
                   [:.input styles/input]
                   [:.button-panel (merge styles/list-reset {:display         "flex"
                                                             :justify-content "flex-end"})]
                   [:.submit-button styles/button-submit
                    [:&:disabled styles/button-disabled]]
                   [:.cancel-button (merge styles/button-secondary
                                           {:margin-left theme/spacing-md})]]}
  (let [disabled? (not (fs/valid-spec? props))
        handle-change (fn [evt]
                        (m/set-string! this ::comment/text :event evt)
                        (comp/transact! this [(fs/mark-complete! {:field ::comment/text})]))
        handle-escape (fn [evt]
                        (when (evt/escape? evt)
                          (comp/transact! this [(model/close-edit-comment-form {:id id})])))
        handle-update-comment (fn [evt]
                                (evt/prevent-default! evt)
                                (comp/transact! this [(model/update-comment
                                                        {:id   id
                                                         :diff (fs/dirty-fields props true)})]))]
    (dom/form
      :.form
      {:autoComplete "off"
       :onKeyDown    handle-escape
       :onSubmit     handle-update-comment}
      (dom/ul
        :.field-list
        (dom/li
          (dom/textarea :.input
                        {:aria-required "true"
                         :autoFocus     true
                         :onChange      handle-change
                         :placeholder   "Edit comment"
                         :rows          5
                         :value         text})))
      (dom/ul
        :.button-panel
        (dom/li
          (dom/button
            :.submit-button
            {:disabled disabled? :type "submit"}
            "Save"))
        (dom/li
          (dom/button
            :.cancel-button
            {:onClick #(comp/transact! this [(model/close-edit-comment-form {:id id})])
             :type    "button"}
            "Cancel"))))))

(def ui-edit-comment-form (comp/factory EditCommentForm))
