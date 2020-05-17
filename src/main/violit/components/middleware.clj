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
(ns violit.components.middleware
  (:require
    [clojure.string :as str]
    [com.fulcrologic.fulcro.server.api-middleware :as server]
    [hiccup.page :refer [html5]]
    [mount.core :refer [defstate]]
    [ring.middleware.defaults :refer [wrap-defaults]]
    [ring.middleware.gzip :refer [wrap-gzip]]
    [ring.util.response :as resp]
    [taoensso.timbre :as log]
    [violit.components.config :as config]
    [violit.components.parser :as parser]
    [violit.utils.transit :as transit]))

(defn not-found-handler
  [_req]
  {:status  404
   :headers {"Content-Type" "text/plain"}
   :body    "404 Not Found"})

(defn wrap-api
  [handler]
  (fn [{:keys [transit-params uri] :as req}]
    (if (= uri "/api")
      (server/handle-api-request
        transit-params
        #(parser/parser {:ring/request req} %))
      (handler req))))

(defn index
  [csrf-token]
  (log/debug "Serving index.html")
  (html5
    [:html {:lang "en"}
     [:head {:lang "en"}
      [:meta {:charset "utf-8"}]
      [:title "Violit"]
      [:meta {:content "width=device-width, initial-scale=1" :name "viewport"}]
      [:meta {:content "An internet forum for discussion" :name "description"}]
      [:link {:href "https://fonts.googleapis.com/css2?family=Montserrat&family=Roboto:wght@400;500&family=Roboto+Mono&display=swap" :rel "stylesheet"}]
      [:script (str "var fulcro_network_csrf_token = '" csrf-token "';")]]

     [:body
      [:noscript "You need to enable JavaScript to run this app."]
      [:div#app]
      [:script {:src "/js/main/main.js"}]]]))

(defn wrap-html-routes [handler]
  (fn [{:keys [anti-forgery-token uri] :as req}]
    (if (or (str/starts-with? uri "/api")
            (str/starts-with? uri "/js"))
      (handler req)
      (-> (resp/response (index anti-forgery-token))
          (resp/content-type "text/html")))))

(defstate middleware
  :start (let [{:keys [ring.middleware/defaults-config]} config/config]
           (-> not-found-handler
               wrap-api
               (server/wrap-transit-params {:opts {:handlers transit/read-handlers}})
               (server/wrap-transit-response {:opts {:handlers transit/write-handlers}})
               wrap-html-routes
               (wrap-defaults defaults-config)
               wrap-gzip)))
