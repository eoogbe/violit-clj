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
(ns violit.components.parser
  (:require
    [mount.core :refer [defstate]]
    [com.wsscode.common.async-clj :refer [let-chan]]
    [com.wsscode.pathom.core :as p]
    [com.wsscode.pathom.connect :as pc]
    [edn-query-language.core :as eql]
    [taoensso.timbre :as log]
    [violit.components.database :as db]
    [violit.logger :as logger]
    [violit.model.account :as account]
    [violit.model.board :as board]
    [violit.model.comment :as comment]
    [violit.model.session :as session]
    [violit.model.thread :as thread]))

(def registry
  [account/resolvers board/resolvers comment/resolvers session/resolvers thread/resolvers])

(defn process-error
  [_env err]
  (let [msg (p/error-str err)]
    (log/error msg)
    msg))

(defn log-requests!
  [{:keys [tx] :as req}]
  (logger/log-entity "transaction:" tx)
  req)

(defn log-responses!
  [resp]
  (logger/log-entity "response:" resp)
  resp)

(def query-params-to-env-plugin
  {::p/wrap-parser
   (fn [parser]
     (fn [env tx]
       (let [children (-> tx eql/query->ast :children)
             query-params (reduce
                            (fn [qps {:keys [type params]}]
                              (cond-> qps
                                      (and (not= :call type) (seq params)) (merge params)))
                            {}
                            children)
             env (assoc env :query-params query-params)]
         (parser env tx))))})

(defstate parser
  :start (p/parser
           {::p/env     {::p/reader               [p/map-reader
                                                   pc/reader2
                                                   pc/open-ident-reader
                                                   p/env-placeholder-reader]
                         ::p/placeholder-prefixes #{">"}
                         ::p/process-error        process-error
                         ::db/conn                db/conn}
            ::p/mutate  pc/mutate
            ::p/plugins [(pc/connect-plugin {::pc/register registry})
                         (p/pre-process-parser-plugin log-requests!)
                         p/elide-special-outputs-plugin
                         (p/post-process-parser-plugin log-responses!)
                         query-params-to-env-plugin
                         p/error-handler-plugin
                         p/trace-plugin]}))
