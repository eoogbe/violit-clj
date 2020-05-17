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
(ns violit.ui.article.article-footer
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom.icons :as icons]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [goog.object :as gobj]
    [violit.ui.article.arrows :as arrows]
    [violit.ui.core.click-outside :as click-outside]
    [violit.ui.core.dropdown-menu :as dropdown-menu]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]))

(defsc ArticleFooter [this
                      {::keys [id vote-status] :ui/keys [open?]}
                      {:keys [on-upvote on-downvote on-open-edit-form on-delete]}]
  {:query                [::id ::vote-status :ui/open?]
   :ident                ::id
   :initial-state        {::id          :param/id
                          ::vote-status :param/vote-status}
   :initLocalState       (fn [this _props]
                           {:save-overflow-ref #(gobj/set this "overflow-el" %)})
   :componentDidMount    (fn [this]
                           (let [overflow-el (gobj/get this "overflow-el")
                                 close-menu #(m/set-value! this :ui/open? false)
                                 cleanup (click-outside/click-outside overflow-el close-menu)]
                             (comp/update-state! this assoc :cleanup cleanup)))
   :componentWillUnmount (fn [this]
                           (let [{:keys [cleanup]} (comp/get-state this)]
                             (cleanup)))
   :css                  [[:.article-footer {:display        "flex"
                                             :flex-direction "column"}]
                          [:.vote-button (merge styles/button-tertiary
                                                {:margin-left theme/spacing-md})
                           [:&:disabled styles/button-disabled]]
                          [:.downvote-button {:margin-right theme/spacing-lg}]
                          [:.open-edit-form-button styles/button-tertiary]
                          [:.upvoted-color {:color theme/primary-color}]
                          [:.downvoted-color {:color theme/error-color}]
                          [:.not-voted-color {:color theme/gray500}]
                          [:.overflow-dropdown {:display     "flex"
                                                :align-items "center"
                                                :align-self  "flex-end"
                                                :position    "relative"}]
                          [:.overflow-toggle {:font-size  "1rem"
                                              :padding    theme/spacing-sm
                                              :border     "none"
                                              :background "none"
                                              :color      theme/gray700
                                              :cursor     "pointer"}]
                          [:.no-overflow-toggle {:width "1.5em"}]]}
  (let [{:keys [save-overflow-ref]} (comp/get-state this)
        menu-id (str "overflow-menu-" id)]
    (dom/footer
      :.article-footer
      (dom/nav
        :.overflow-dropdown
        {:ref save-overflow-ref}
        (dom/button
          :.vote-button
          {:classes  [(if (= vote-status :upvoted) :.upvoted-color :.not-voted-color)]
           :disabled (not on-upvote)
           :onClick  on-upvote
           :type     "button"}
          (arrows/ui-arrow-up {:id    (str id "-upvote")
                               :title "Upvote"}))
        (dom/button
          :.vote-button.downvote-button
          {:classes  [(if (= vote-status :downvoted) :.downvoted-color :.not-voted-color)]
           :disabled (not on-downvote)
           :onClick  on-downvote
           :type     "button"}
          (arrows/ui-arrow-down {:id    (str id "-downvote")
                                 :title "Downvote"}))
        (when on-open-edit-form
          (dom/button
            :.open-edit-form-button
            {:onClick on-open-edit-form
             :type    "button"}
            (icons/ui-icon {:icon  :edit
                            :title "Edit"})))
        (if on-delete
          (dom/button
            :.overflow-toggle
            {:aria-controls menu-id
             :aria-expanded (str open?)
             :aria-haspopup "true"
             :onClick       #(m/toggle! this :ui/open?)
             :type          "button"}
            (icons/ui-icon {:icon :more-vert}))
          (dom/div :.no-overflow-toggle))
        (dropdown-menu/ui-dropdown-menu
          {:id        menu-id
           :items     [{:icon     :delete
                        :on-click on-delete
                        :text     "Delete"}]
           :open?     open?
           :placement "auto"}
          {:close #(m/set-value! this :ui/open? false)})))))

(def ui-article-footer (comp/computed-factory ArticleFooter))
