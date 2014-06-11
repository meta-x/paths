(ns mx.paths.middleware
  (:require [mx.paths.core :refer [route]]))

;;; optional middleware for route params

(defn wrap-route-params
  "Middleware that puts the route parameters into the request's :params map."
  [handler routes-tree]
  (fn [request]
    (let [[_ route-params] (route request routes-tree)]
      (->>
        route-params
        (merge (get request :params {})) ; merge request params with route params
        (assoc request :params))))) ; put it back into the request
