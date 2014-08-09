(ns paths.test.middleware
  (:require [clojure.test :refer [deftest testing is]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [mx.paths.middleware :refer [wrap-route-params]]
            [mx.paths.tree :refer [create-routes-tree]]))

(def routes [
  "/has/:route/params/:here" {}
])
(def rt (create-routes-tree routes))
(def app
  (->
    (fn [req] req)
    (wrap-route-params rt)
    (wrap-keyword-params)
    (wrap-params)))

; (wrap-route-params handler routes-tree)
(deftest middleware
  (let [arg-route "one"
        arg-here "two"
        uri (str "/has/" arg-route "/params/" arg-here)
        req (app {:uri uri :request-method :get})
        params (:params req)]
    (is (= (:route params) arg-route))
    (is (= (:here params) arg-here))
  ))
