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
(ns violit.lib.routing
  (:require
    [clojure.string :as str]
    [com.fulcrologic.fulcro.components :as comp]
    [com.fulcrologic.fulcro.routing.dynamic-routing :as dr]
    [oops.core :refer [oget]]
    [taoensso.timbre :as log]))

(defn path->route
  [path]
  (let [route (drop 1 (str/split path #"/"))]
    (if (seq route)
      (vec route)
      [""])))

(defn current-route []
  (let [path (oget js/document :location :pathname)]
    (path->route path)))

(defn route->path
  [route]
  (str "/" (str/join "/" route)))

(defn- mk-history-state
  [app prev-route]
  (when (dr/resolve-target app prev-route)
    (clj->js {:prev-route prev-route})))

(defn install-route-history!
  [app {:keys [ignore-targets]}]
  (let [pop-state-listener (fn [evt]
                             (log/debug "Pop state event emitted" evt)
                             (let [route (current-route)]
                               (dr/change-route! app route)))
        route (current-route)
        ignore-routes (into #{} (map #(apply dr/path-to %)) ignore-targets)
        ignore? (contains? ignore-routes route)
        state (when-not ignore? (mk-history-state app route))]
    (log/info "Installing route history" {:current-route route
                                          :state         state})
    (.replaceState js/history state "")
    (dr/initialize! app)
    (dr/change-route! app route)
    (.addEventListener js/window "popstate" pop-state-listener)))

(defn- normalize-route-from-targets
  [{:keys [route route-targets]}]
  (when-not (or route route-targets)
    (log/error "Route or route target must be specified"))
  (or route (apply dr/path-to route-targets)))

(defn push-route!
  [app-or-comp {:keys [save-route?] :or {save-route? true} :as opts}]
  (let [app (comp/any->app app-or-comp)
        prev-route (current-route)
        next-route (normalize-route-from-targets opts)
        state (if save-route?
                (mk-history-state app next-route)
                (oget js/history :state))]
    (when-not (= prev-route next-route)
      (log/info "Pushing route" next-route "with state" state)
      (dr/change-route! app-or-comp next-route)
      (.pushState js/history state "" (route->path next-route)))))

(defn replace-route!
  [app-or-comp {:keys [save-route?] :as opts}]
  (let [app (comp/any->app app-or-comp)
        prev-route (current-route)
        next-route (normalize-route-from-targets opts)
        state (when save-route?
                (mk-history-state app prev-route))]
    (when-not (= prev-route next-route)
      (log/info "Replacing route" next-route "with state" state)
      (dr/change-route! app-or-comp next-route)
      (.replaceState js/history state "" (route->path next-route)))))

(defn return-to-saved-route!
  [app-or-comp default-route]
  (let [route (-> (oget js/history :state)
                  js->clj
                  (get "prev-route")
                  (or default-route))]
    (replace-route! app-or-comp {:route route})))
