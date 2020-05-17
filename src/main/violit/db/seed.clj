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
(ns violit.db.seed
  (:require
    [taoensso.timbre :as log]
    [tick.alpha.api :as t]
    [violit.db.account :as db.account]
    [violit.db.board :as db.board]
    [violit.db.comment :as db.comment]
    [violit.db.thread :as db.thread]
    [violit.schema.account :as account]
    [violit.schema.board :as board]
    [violit.schema.comment :as comment]
    [violit.schema.thread :as thread]
    [violit.utils.string :as str]
    [violit.utils.uuid :as uuid]))

(defn minutes-ago
  [n]
  (t/<< (t/now) (t/new-duration n :minutes)))

(def USERNAME "fred-astaire")

(def ACCOUNT
  (account/mk-account {::account/username   USERNAME
                       ::account/password   "password123"
                       ::account/created-at (minutes-ago 5)}))

(def BOARD
  {::board/name
   "dance"

   ::board/description
   (str/multiline
     "Dance is an art form that expresses itself through movement. Here, we celebrate all forms of "
     "dance.")})

(def THREADS
  [(thread/mk-thread
     {::thread/id
      (uuid/gen-uuid)

      ::thread/title
      "Social dance is like a conversation"

      ::thread/created-at
      (minutes-ago 4)

      ::thread/author
      USERNAME

      ::thread/body
      (str/multiline
        "In social dance, you communicate with your partner through movements. The lead makes a "
        "suggestion, and the follow makes an interpretation. The lead then responds to that "
        "interpretation with the next suggestion, and so on."
        ""
        "Follow is a misleading term because the follow isn't blindly following orders. Instead "
        "they are actively *interpreting* the lead's suggestions."
        ""
        "This fluid back-and-forth is what makes social dance unique!")})

   (thread/mk-thread
     {::thread/id         (uuid/gen-uuid)
      ::thread/title      "How do I learn the chicken noodle soup?"
      ::thread/created-at (minutes-ago 2)
      ::thread/author     USERNAME})

   (thread/mk-thread
     {::thread/id
      (uuid/gen-uuid)

      ::thread/title
      "DAE hate bachata?"

      ::thread/created-at
      (minutes-ago 1)

      ::thread/author
      USERNAME

      ::thread/body
      (str/multiline
        "It's like grinding but with a basic step. Doesn't help that we\ndo it in a well-lit dance "
        "hall.")})])

(def COMMENTS
  (let [thread-slug (-> THREADS first ::thread/slug)]
    [(comment/mk-comment {::comment/id          (uuid/gen-uuid)
                          ::comment/text        "I love to dance!"
                          ::comment/thread-slug thread-slug
                          ::comment/created-at  (minutes-ago 3)
                          ::comment/author      USERNAME})
     (comment/mk-comment {::comment/id          (uuid/gen-uuid)
                          ::comment/text        "Dance is the best!"
                          ::comment/thread-slug thread-slug
                          ::comment/created-at  (minutes-ago 2)
                          ::comment/author      USERNAME})
     (comment/mk-comment {::comment/id          (uuid/gen-uuid)
                          ::comment/text        "How do I get started dancing?"
                          ::comment/thread-slug thread-slug
                          ::comment/created-at  (minutes-ago 1)
                          ::comment/author      USERNAME})]))

(defn seed!
  [conn]
  (log/info "Seeding database")
  (db.account/create-account conn ACCOUNT)
  (db.board/create-board conn BOARD)
  (doseq [thread THREADS]
    (db.thread/create-thread conn thread))
  (doseq [comment COMMENTS]
    (db.comment/create-comment conn comment)))
