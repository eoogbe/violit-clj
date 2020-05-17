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
(ns violit.ui.comments.new-comment-form
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

(defsc NewCommentForm [this
                       {::comment/keys [id text] :as props}
                       {:keys [thread-slug on-create]}]
  {:query         [::comment/id ::comment/text fs/form-config-join]
   :ident         ::comment/id
   :form-fields   #{::comment/text}
   :initial-state {}
   :pre-merge     (fn [{:keys [current-normalized data-tree]}]
                    (let [defaults {::comment/id   (random-uuid)
                                    ::comment/text ""}]
                      (merge
                        defaults
                        current-normalized
                        (fs/add-form-config NewCommentForm data-tree))))
   :css           [[:.form (merge styles/form {:margin [[theme/spacing-lg 0 theme/spacing-md]]})]
                   [:.field-list styles/list-reset]
                   [:.input styles/input]
                   [:.submit-button styles/button-submit
                    [:&:disabled styles/button-disabled]]]}
  (let [disabled? (not (fs/valid-spec? props))
        handle-change (fn [evt]
                        (m/set-string! this ::comment/text :event evt)
                        (comp/transact! this [(fs/mark-complete! {:field ::comment/text})]))
        handle-create-comment (fn [evt]
                                (evt/prevent-default! evt)
                                (on-create)
                                (comp/transact! this [(model/create-comment
                                                        {:diff        (fs/dirty-fields props false)
                                                         :id          id
                                                         :thread-slug thread-slug})]))]
    (dom/form
      :.form
      {:autoComplete "off"
       :onSubmit     handle-create-comment}
      (dom/ul
        :.field-list
        (dom/li
          (dom/textarea :.input
                        {:onChange    handle-change
                         :placeholder "Write a comment"
                         :rows        5
                         :value       text})))
      (dom/button
        :.submit-button
        {:disabled disabled? :type "submit"}
        "Comment"))))

(def ui-new-comment-form (comp/computed-factory NewCommentForm))
