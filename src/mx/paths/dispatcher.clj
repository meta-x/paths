(ns mx.paths.dispatcher
  (:require [mx.paths.matcher :refer [match]]
            [mx.paths.handlers :refer [fourzerofour-handler]]))

;;; find out the parameters of the handler and automagically pass them to the function

(defn- get-arglist
  "Helper for retrieving the argument list from a function (var)."
  [fnvar]
  (->
    fnvar
    (meta)
    (:arglists)
    (first)))

(defn- get-param-name
  "Returns the :name of the parameter. The name is either set as a metadata field
  (called :name) or is the param."
  [param]
  (or (:name (meta param)) param))

(defn- destruct-arglist
  "Given a list of `param-names`, return a list with the corresponding values from `req-params`."
  [param-names req-params]
  (map #(get req-params (keyword (get-param-name %1))) param-names))

(defn- handle
  "Helper that given a `handler` and a `request`, matches the request :params to the
  arguments of the handler. Sends nil for the args that do not exist in the request :params."
  [handler request]
  (let [param-names (get-arglist handler)]
    (if (empty? param-names)
      (handler) ; no args handler
      (let [req-params (:params request) ; handler defined a list of arguments
            req-params (assoc req-params :request request) ; add the request object into the request params
            req-vals (destruct-arglist param-names req-params)]
        (apply handler req-vals)))))

(defn dispatch
  [routes-tree request]
  (let [[h rp] (match request routes-tree)]
    (if (nil? h)
      ; no handler found, 404
      (let [handler-404 (:any (get routes-tree ":404" fourzerofour-handler))]
        (handler-404 request))
      ; handler found, apply handler with args
      (->> ; TODO: if wrap-route-params is used, this is not needed
        rp ; route params
        (merge (get request :params {})) ; merge request params with route params
        (assoc request :params) ; put it back into the request
        (handle h))))) ; call `handle` to execute the handler
