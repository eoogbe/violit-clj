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
(ns violit.ui.article.article-header
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as m]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [goog.object :as gobj]
    ["@popperjs/core" :refer [createPopper]]
    [violit.ui.account.profile :as profile]
    [violit.ui.core.link :as link]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]
    [violit.ui.core.time-ago :as time-ago]))

(defn create-popper
  [this]
  (let [author-el (gobj/get this "author-el")
        author-tooltip-el (gobj/get this "author-tooltip-el")
        popper-instance (createPopper author-el author-tooltip-el #js {"placement" "top-start"})]
    (gobj/set this "popper-instance" popper-instance)))

(defn destroy-popper
  [this]
  (when-let [popper-instance (gobj/get this "popper-instance")]
    (.destroy popper-instance)
    (gobj/set this "popper-instance" nil)))

(defn show-tooltip
  [this]
  (m/set-value! this :ui/show-author-tooltip? true)
  (create-popper this))

(defn hide-tooltip
  [this]
  (m/set-value! this :ui/show-author-tooltip? false)
  (destroy-popper this))

(defsc ArticleHeader [this
                      {::keys
                       [id author author-pronouns score created-at updated-at edit-history-rt]

                       :ui/keys
                       [show-author-tooltip?]}
                      {:keys [heading]}]
  {:query          [::id
                    ::author
                    ::author-pronouns
                    ::score
                    ::created-at
                    ::updated-at
                    ::edit-history-rt
                    :ui/show-author-tooltip?]
   :ident          ::id
   :initial-state  {::id              :param/id
                    ::author          :param/author
                    ::author-pronouns :param/author-pronouns
                    ::score           :param/score
                    ::created-at      :param/created-at
                    ::updated-at      :param/updated-at
                    ::edit-history-rt :param/edit-history-rt}
   :initLocalState (fn [this _props]
                     {:save-author-ref         #(gobj/set this "author-el" %)
                      :save-author-tooltip-ref #(gobj/set this "author-tooltip-el" %)})
   :css            [[:.heading styles/page-heading]
                    [:.meta (merge styles/list-reset {:font-size     "0.875rem"
                                                      :display       "flex"
                                                      :margin-bottom "0.286em"
                                                      :color         theme/gray700})
                     [:li
                      [:&:before {:content "\" • \""
                                  :margin  [[0 theme/spacing-sm]]}]
                      [:&:first-child [:&:before {:content "\"\""
                                                  :margin  0}]]]]
                    [:.author-tooltip {:padding          theme/spacing-sm
                                       :border           [["1px" "solid" theme/gray500]]
                                       :background-color theme/white
                                       :color            theme/black}]
                    [:.show-tooltip {:display "block"}]
                    [:.hide-tooltip {:display "none"}]
                    [:.meta-link {:cursor "pointer"}
                     [:&:hover {:text-decoration [["underline" theme/gray500]]}]]]}
  (let [{:keys [save-author-ref save-author-tooltip-ref]} (comp/get-state this)
        author-tooltip-id (str id "-author-tooltip")
        author-tooltip-props {:classes [:.author-tooltip
                                        (if show-author-tooltip? :.show-tooltip :.hide-tooltip)]
                              :id      author-tooltip-id
                              :ref     save-author-tooltip-ref
                              :role    "tooltip"}]
    (dom/header
      (when heading
        (dom/h2 :.heading {:itemProp "headline"} heading))
      (dom/ul
        :.meta
        (if author
          (dom/li
            (if author-pronouns
              (dom/span author-tooltip-props
                        "Pronouns: " author-pronouns)
              (dom/span author-tooltip-props
                        "No pronouns specified"))
            (link/ui-link
              :.meta-link
              {:aria-describedby author-tooltip-id
               :itemProp         "author"
               :onBlur           #(hide-tooltip this)
               :onFocus          #(show-tooltip this)
               :onMouseEnter     #(show-tooltip this)
               :onMouseLeave     #(hide-tooltip this)
               :ref              save-author-ref
               :route-targets    [profile/Profile author]}
              author))
          (dom/li {:itemProp "author"} "[deleted]"))
        (dom/li score (if (= score 1) " point" " points"))
        (dom/li
          (time-ago/ui-time-ago {:date     created-at
                                 :itemProp "dateCreated"}))
        (when updated-at
          (dom/li
            (if edit-history-rt
              (link/ui-link
                :.meta-link
                {:route-targets edit-history-rt}
                "Edited "
                (time-ago/ui-time-ago {:date     updated-at
                                       :itemProp "dateModified"}))
              (dom/span
                "Edited "
                (time-ago/ui-time-ago {:date     updated-at
                                       :itemProp "dateModified"})))))))))

(def ui-article-header (comp/computed-factory ArticleHeader))
