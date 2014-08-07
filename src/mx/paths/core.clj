(ns mx.paths.core
  (:require [mx.paths.tree :refer [create-routes-tree]]
            [mx.paths.dispatcher :refer [dispatch]]
            [mx.paths.matcher :refer [match]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [mx.paths.middleware :refer [wrap-route-params]]))

(defmulti route map?)
(defmethod route true [routes-tree]
  ; takes a previously compiled route-tree and returns a ring handler function
  ; that will route requests to the correct endpoint handler.
  (fn [request]
    (dispatch routes-tree request)))
(defmethod route false [routes-def]
  ; takes a route definition, ''compiles it'' and returns a ring handler function
  ; that will route requests to the correct endpoint handler.
  (let [tree (create-routes-tree routes-def)]
    (fn [request]
      (dispatch tree request))))

(defmulti query map?)
(defmethod query true [routes-tree]
  ; query returns a function that let's you query the routes tree.
  ; this version takes a precompiled routes-tree as argument.
  (fn [request]
    (match request routes-tree)))
(defmethod query false [routes-def]
  ; query returns a function that let's you query the routes tree.
  ; this version takes the routes definition as argument.
  (fn [request]
    (match request (create-routes-tree routes-def))))


(defn pathsize
  "Helper... to be used when there's no middleware dependencies on the params middlewares"
  [routes]
  (->
    routes  ; doesn't matter if it's a tree or the definition
    (route) ; the multimethod will find the correct one
    (wrap-route-params)
    (wrap-keyword-params)
    (wrap-params)
    ))

(defn wrap-middleware
  "Helper ... to be used when there's dependencies on the params middlewares"
  [app routes-tree]
  (->
    app
    (wrap-route-params routes-tree)
    (wrap-keyword-params)
    (wrap-params)))
