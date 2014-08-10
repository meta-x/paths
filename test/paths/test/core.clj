(ns paths.test.core
  (:require [clojure.test :refer [deftest testing is]]
            [mx.paths.tree :refer [create-routes-tree]]
            [mx.paths.core :refer :all]))

(defn h1 [arg]
  {:body {:h1 arg}})

(def routes [
  "/hello/world" {:get #'h1}
  "/has/:route/params/:here" {:get :r1}
])
(def routes-tree
  (create-routes-tree routes))

(deftest core
  (testing "route"
    (is (= ((route routes) {:uri "/hello/world" :params {:arg "h1"} :request-method :get}) {:body {:h1 "h1"}}))
    (is (= ((route routes-tree) {:uri "/hello/world" :params {:arg "h1"} :request-method :get}) {:body {:h1 "h1"}})))

  (testing "query"
    (let [q-rd (query routes)
          q-rt (query routes-tree)]
      (is (= (q-rd {:uri "/has/one/params/two" :request-method :get})
             [:r1 {:here "two", :route "one"}]))
      (is (= (q-rd {:uri "/has/params/two" :request-method :get})
             [nil {:route "params"}]))
      (is (= (q-rd {:uri "/ha/params/two" :request-method :get})
             [nil {}]))
      (is (= (q-rt {:uri "/has/one/params/two" :request-method :get})
             [:r1 {:here "two", :route "one"}]))
      (is (= (q-rt {:uri "/has/params/two" :request-method :get})
             [nil {:route "params"}]))
      (is (= (q-rt {:uri "/ha/params/two" :request-method :get})
             [nil {}]))))

  (testing "pathsize"
    (let [app-rd (pathsize routes)
          app-rt (pathsize routes-tree)]
      (is (= (app-rd {:uri "/hello/world" :params {:arg "h1"} :request-method :get}) {:body {:h1 "h1"}}))
      (is (= (app-rt {:uri "/hello/world" :params {:arg "h1"} :request-method :get}) {:body {:h1 "h1"}}))
    ))

  (testing "wrap-middleware"
    (let [app (-> routes-tree (route))
          app (wrap-middleware app routes-tree)]
      (is (= (app {:uri "/hello/world" :params {:arg "h1"} :request-method :get}) {:body {:h1 "h1"}}))))
)
