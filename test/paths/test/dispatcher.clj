(ns paths.test.dispatcher
  (:require [clojure.test :refer [deftest testing is]]
            [mx.paths.tree :refer [create-routes-tree]]
            [mx.paths.dispatcher :refer :all]))

(defn h1 []
  {:body :h1})
(defn h2 [arg]
  {:body {:h2 arg}})
(defn h3 []
  {:body :h3})
(defn four [request]
  {:body :404})

(def routes [
  "/" {:get #'h1}
  "/hello" {:get #'h2}
  "/hello/world" {:get #'h3}
  :404 {:any #'four}
])

(deftest dispatcher
  (let [routes-tree (create-routes-tree routes)]
    (is (= (dispatch routes-tree {:uri "/" :request-method :get}) {:body :h1}))
    (is (= (dispatch routes-tree {:uri "/hello" :request-method :get :params {:arg "arg-h2"}}) {:body {:h2 "arg-h2"}}))
    (is (= (dispatch routes-tree {:uri "/hello/world" :request-method :get}) {:body :h3}))
    (is (= (dispatch routes-tree {:uri "/abc" :request-method :get}) {:body :404}))))
