(ns paths.test.matcher
  (:require [clojure.test :refer [deftest testing is]]
            [mx.paths.tree :refer [create-routes-tree]]
            [mx.paths.matcher :refer :all]))

(def routes [
  "/has/:route/params/:here" {:get :r1}
])
(def routes-tree (create-routes-tree routes))

(deftest matcher
  (is (= (match routes-tree {:uri "/has/one/params/two" :request-method :get})
         [:r1 {:here "two", :route "one"}]))
  (is (= (match routes-tree {:uri "/has/params/two" :request-method :get})
         [nil {:route "params"}]))
  (is (= (match routes-tree {:uri "/ha/params/two" :request-method :get})
         [nil {}])))
