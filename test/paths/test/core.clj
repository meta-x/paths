(ns paths.test.core
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [mx.paths.core :as paths]))



(deftest something
  (testing "something something"
    (is (= 1 1))
    (is (= 2 2))
    ))


; handler runs
; get
; post
; put
; delete

; params
; query
; form
; wildcard
; request object
