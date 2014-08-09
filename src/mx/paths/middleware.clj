(ns mx.paths.middleware
  (:require [mx.paths.matcher :refer [match]]))

;;; optional middleware for route params

(defn wrap-route-params
  "Middleware that puts the route parameters into the request's :params map."
  [handler routes-tree]
  (fn [request]
    (let [[_ route-params] (match routes-tree request)]
      (->>
        route-params
        (merge (get request :params {})) ; merge request params with route params
        (assoc request :params) ; put it back into the request
        (handler) ; and call the next handler...
      ))))
