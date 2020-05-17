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
(ns violit.ui.core.dropdown-menu
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom.icons :as icons]
    [com.fulcrologic.fulcro-css.localized-dom :as dom]
    [goog.object :as gobj]
    [oops.core :refer [oget oset!+]]
    [violit.ui.core.styles :as styles]
    [violit.ui.core.theme :as theme]))

(defn bounding-client-rect
  [el]
  (let [rect (.getBoundingClientRect el)]
    {:bottom (oget rect :bottom)
     :height (oget rect :height)
     :left   (oget rect :left)
     :right  (oget rect :right)
     :top    (oget rect :top)
     :width  (oget rect :width)
     :x      (oget rect :x)
     :y      (oget rect :y)}))

(defn get-menu-vertical
  [{:keys [menu-rect scroll-parent-rect view-height scroll-top placement]}]
  (let [view-space-below (- view-height (:top menu-rect))
        scroll-space-below (- (:height scroll-parent-rect) scroll-top (:top menu-rect))
        scroll-down (+ (- (:bottom menu-rect) view-height) scroll-top)]
    (cond
      (<= (:height menu-rect) view-space-below) {:direction :top}
      (<= (:height menu-rect) scroll-space-below) {:direction   :top
                                                   :scroll-down scroll-down}
      (= placement "auto") {:direction :bottom}
      :else {:direction   :top
             :scroll-down scroll-down})))

(defn place-menu
  [menu-el placement]
  (let [scroll-parent (oget js/document :documentElement)
        {:keys [direction scroll-down]} (get-menu-vertical
                                          {:placement          placement
                                           :view-height        (oget js/window :innerHeight)
                                           :scroll-top         (oget js/window :pageYOffset)
                                           :scroll-parent-rect (bounding-client-rect scroll-parent)
                                           :menu-rect          (bounding-client-rect menu-el)})]
    (oset!+ menu-el :style direction "calc(100% - 1px)")
    (when scroll-down
      (.scrollTo scroll-parent #js {"top"      scroll-down
                                    "behavior" "smooth"}))))

(def menu-box-shadow
  "0 0 0 1px hsla(0, 0%, 0%, 0.1), 0 4px 11px hsla(0, 0%, 0%, 0.1)")

(defn ui-menu-item
  [{:keys [close icon on-click text]}]
  (dom/li
    {:key  text
     :role "menuitem"}
    (dom/button
      :.menu-item-button
      {:onClick (fn []
                  (close)
                  (on-click))
       :type    "button"}
      (icons/ui-icon {:icon icon})
      text)))

(defsc DropdownMenu [this {:keys [id items open?]} {:keys [close]}]
  {:initLocalState    (fn [this _props]
                        {:save-menu-ref #(gobj/set this "menu-el" %)})
   :componentDidMount (fn [this]
                        (let [menu-el (gobj/get this "menu-el")
                              {:keys [placement] :or {placement "bottom"}} (comp/props this)]
                          (place-menu menu-el placement)))
   :css               [[:.dropdown-menu (merge styles/list-reset
                                               {:display          "none"
                                                :position         "absolute"
                                                :top              "calc(100% - 1px)"
                                                :right            0
                                                :width            "7.5em"
                                                :border           [["1px" "solid" theme/gray200]]
                                                :background-color theme/white
                                                :box-shadow       menu-box-shadow
                                                :z-index          1})]
                       [:.menu-item-button {:font-size        "1rem"
                                            :font-family      theme/body-font
                                            :display          "flex"
                                            :justify-content  "space-between"
                                            :width            "100%"
                                            :padding          theme/spacing-md
                                            :border           "none"
                                            :background-color "transparent"
                                            :color            theme/black
                                            :cursor           "pointer"}
                        [:&:hover {:background-color theme/gray200}]]
                       [:.open {:display "block"}]]}
  (let [{:keys [save-menu-ref]} (comp/get-state this)]
    (dom/ul
      :.dropdown-menu
      {:classes [(when open? :.open)]
       :id      id
       :ref     save-menu-ref
       :role    "menu"}
      (map #(-> % (assoc :close close) ui-menu-item) items))))

(def ui-dropdown-menu (comp/computed-factory DropdownMenu))
