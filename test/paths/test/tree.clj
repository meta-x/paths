(ns paths.test.tree
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [mx.paths.tree :refer :all]))

(defn test-rt [tree handler-met handler-val sr-val]
  (is (= (get tree handler-met) handler-val))
  (is (= (empty? (:subroutes tree)) sr-val))
  :ok)

(def routes1 [
  "/" {:get :fn1}
  "/hello/world1" {:get :fn21 :post :fn22}
  "/hello/world2" {:put :fn3}
  "/hello" {:post :fn4}
  "/hallo" {:post :fn5}
])

(deftest tree
  (let [rt (create-routes-tree routes1)
        rt-root (get rt "/")]
    (test-rt rt-root :get :fn1 false)
    (let [sr (:subroutes rt-root)
          rt-hallo (get sr "hallo")
          rt-hello (get sr "hello")]
      (test-rt rt-hallo :post :fn5 true)
      (test-rt rt-hello :post :fn4 false)
      (let [sr (:subroutes (get (:subroutes rt-hello) "/"))
            rt-w1 (get sr "world1")
            rt-w2 (get sr "world2")]
        (test-rt rt-w1 :get :fn21 true)
        (test-rt rt-w1 :post :fn22 true)
        (test-rt rt-w2 :put :fn3 true)
  ))))
