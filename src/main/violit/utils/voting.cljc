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
(ns violit.utils.voting)

(defn ci-lower-bound
  [upvoters downvoters]
  (let [pos (count upvoters)
        neg (count downvoters)
        n (+ pos neg)]
    (if (= n 0)
      0
      (/
        (-
          (/ (+ pos 1.9208) n)
          (/
            (*
              1.96
              (Math/sqrt (+
                           (/ (* pos neg) n)
                           0.9604)))
            n))
        (+ 1 (/ 3.8416 n))))))
