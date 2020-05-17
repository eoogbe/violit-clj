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
(ns violit.ui.threads.new-thread-form
  (:require
    [clojure.string :as str]
    [com.fulcrologic.fulcro.algorithms.form-state :as fs]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom.events :as evt]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [com.fulcrologic.fulcro.ui-state-machines :as uism]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [violit.model.session :as session]
    [violit.model.thread :as model]
    [violit.schema.thread :as thread]
    [violit.ui.auth.login-form :as login-form]
    [violit.ui.core.main-container :as main-container]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.title :as title]))

(defn validate-title
  [{::thread/keys [title]}]
  (when (str/blank? title)
    "Must not be blank."))

(defsc NewThreadForm [this {::thread/keys [id title body] :ui/keys [loaded? debug?] :as props}]
  {:query             [::thread/id
                       ::thread/title
                       ::thread/body
                       :ui/thread-result
                       :ui/loaded?
                       :ui/debug?
                       fs/form-config-join
                       (uism/asm-ident ::session/sessionsm)]
   :ident             (fn [] (model/thread-form-path))
   :form-fields       #{::thread/title ::thread/body}
   :initial-state     {:ui/debug? :param/debug?}
   :pre-merge         (fn [{:keys [current-normalized data-tree]}]
                        (let [defaults {::thread/id    (random-uuid)
                                        ::thread/title ""
                                        ::thread/body  ""}]
                          (merge
                            defaults
                            current-normalized
                            (fs/add-form-config NewThreadForm data-tree))))
   :route-segment     ["new-thread"]
   :will-enter        (fn [app _params]
                        (let [ident (comp/get-ident NewThreadForm {})]
                          (merge/merge-component! app NewThreadForm {::thread/title ""
                                                                     ::thread/body  ""
                                                                     :ui/loaded?    false})
                          (comp/transact! app [(fs/clear-complete! {:entity-ident ident})])
                          (dr/route-immediate ident)))
   :componentDidMount (fn [this]
                        (let [{:ui/keys [debug?]} (comp/props this)]
                          (when-not debug?
                            (session/redirect-if-unauthenticated this login-form/LoginForm))
                          (title/set-title! "New thread")))
   :css               [[:.form styles/form]
                       [:.heading styles/page-heading]
                       [:.field-list styles/list-reset]
                       [:.input styles/input]
                       [:.form-error styles/form-error]
                       [:.submit-button styles/button-submit
                        [:&:disabled styles/button-disabled]]]}
  (let [title-error (and
                      (fs/checked? props ::thread/title)
                      (validate-title props))
        disabled? (not (fs/valid-spec? props ::thread/title))

        mk-handle-change (fn [field]
                           (fn [evt]
                             (m/set-string! this field :event evt)
                             (comp/transact! this [(fs/mark-complete! {:field field})])))

        handle-create-thread (fn [evt]
                               (evt/prevent-default! evt)
                               (comp/transact! this
                                               [(model/create-thread
                                                  {:id   id
                                                   :diff (fs/dirty-fields props false)})]))]
    (when (or loaded? debug?)
      (main-container/ui-main-container
        {}
        (dom/form
          :.form
          {:autoComplete "off"
           :onSubmit     handle-create-thread}
          (dom/h2 :.heading "New thread")
          (dom/ul
            :.field-list
            (dom/li
              (dom/label {:htmlFor "thread-title"} "Title")
              (dom/input
                :.input#thread-title
                {:aria-describedby (when title-error "thread-title-error")
                 :aria-required    "true"
                 :autoFocus        true
                 :onBlur           #(comp/transact! this [(fs/mark-complete! {:field ::thread/title})])
                 :onChange         (mk-handle-change ::thread/title)
                 :type             "text"
                 :value            title})
              (when title-error
                (dom/p :.form-error#thread-title-error title-error)))
            (dom/li
              (dom/label {:htmlFor "thread-body"} "Body")
              (dom/textarea
                :.input#thread-body
                {:rows     5
                 :onBlur   #(comp/transact! this [(fs/mark-complete! {:field ::thread/title})])
                 :onChange (mk-handle-change ::thread/body)
                 :value    body})))
          (dom/button
            :.submit-button
            {:disabled disabled? :type "submit"}
            "Post"))))))

(def ui-new-thread-form (comp/factory NewThreadForm))
