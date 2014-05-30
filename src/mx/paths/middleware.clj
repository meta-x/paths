(ns mx.paths.middleware
  (:require [mx.paths.core :refer [route]]))

;;; optional middleware for route params

(defn wrap-route-params [handler routes-tree]
  "Middleware that puts the route parameters into the request's :params map."
  (fn [request]
    (let [[_ route-params] (route request routes-tree)]
      (->>
        (merge (get request :params {}) route-params) ; merge request params with route params
        (assoc request :params) ; put it back into the request
      ))))
