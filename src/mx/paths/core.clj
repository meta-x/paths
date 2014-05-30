(ns mx.paths.core
  (:require [mx.paths.utils :refer [combine]])
  )

; TODO:
; - prune the tree from the delimiter (PATH_DELIMITER_KEEP keeps "/")
; implement my own tokenizer code? ugh


(declare create-routes-tree route router-with router-with-tree)

(def PATH_DELIMITER_KEEP #"((?<=/)|(?=/))")
(def PATH_DELIMITER_DISCARD #"/")

(defn- tokenize-path [path]
  "Given /this/is/a/path, returns [/ this / is / a / path].
  TODO: should actually return [/ this is a path]."
  (->
    (clojure.string/lower-case path)
    (clojure.string/split PATH_DELIMITER_KEEP)))

(defn- last-token? [tokens]
  (= (count tokens) 1))

(defn- create-branch [tree tokens actions]
  "Iterates through `tokens` and creates a tree-like structure attaching `actions`
  to the leaf node."
  (let [t (first tokens)]
    (let [r-t (get tree t {})]
      (if (last-token? tokens)
        (->> (merge r-t actions)
             (assoc tree t))
        (assoc tree t {:subroutes (create-branch tree (rest tokens) actions)})))
    ))

(defn- get-next-route [[path actions]]
  "Helper function to destructure the routes definition."
  (if (nil? path)
    nil
    [path actions]))

(defn create-routes-tree [routes-def] ; public visibility
  "Given a routes definition, returns a routes tree that is used by the routing
  function to send requests to the correct handler."
  (loop [r routes-def
         tree {}]
    (if-let [[path actions] (get-next-route r)]
      (let [path-tokens (tokenize-path path)
            new-branch (create-branch {} path-tokens actions)
            new-tree (combine tree new-branch)]
        (recur (drop 2 r) new-tree))
      tree)))

;;; helpers for finding the handler for a given path

(defn- is-wildcard [n]
  (.startsWith n ":"))

(defn- get-wildcard-node [tree]
  "Helper for tree navigation. Returns the wildcard node or nil."
  (-> (filter is-wildcard (keys tree))
      (first)))

(defn- wc->kw [wc]
  "Helper that converts a :wildcard into a :keyword."
  (->
    (subs wc 1)
    (keyword)))

(defn- get-node [tree token route-params]
  "Helper for tree navigation - understands the wilcard `:` token."
  (if-let [node (get tree token)] ; try to find the token in the current level
    (vector node route-params) ; if found, returns the node; if not found, try to match with a wildcard token
    (if-let [wildcard (get-wildcard-node tree)]
      (vector (get tree wildcard) (assoc route-params (wc->kw wildcard) token))
      [nil route-params])))

(defn- find-path [tree tokens route-params]
  (let [t (first tokens)]
    (if (last-token? tokens)
      (get-node tree t route-params) ; returns the leaf nodes (aka the actions map)
      (let [[n rp] (get-node tree t route-params)]
        (find-path (:subroutes n) (rest tokens) rp))
    )))

;;; determine which handler to call

(defn route [request routes-tree] ; public visibility
  "Route the request to the correct handler and returns it."
  (let [path (:uri request)
        method (:request-method request)
        path-tokens (tokenize-path path)
        [node route-params] (find-path routes-tree path-tokens {})]
      (vector (get node method) route-params) ; returns [handler route-params]
    ))

(defn bind-query-routes [routes-tree] ; usage: (def query-routes (paths/bind-query-routes routes-tree))
  (fn [request]
    (route request routes-tree)))

;;; determine handler parameters and automagically map them when calling

(defn- get-arglist [handler]
  "Helper for retrieving the argument list from a function (var)."
  (->
    (meta handler)
    (:arglists)
    (first)))

(defn- destruct-arglist [param-names req-params]
  "Given a list of `param-names`, return a list with the corresponding values from `req-params`."
  (map #(get req-params (keyword %1)) param-names))

(defn- handle [handler request]
  "Helper that given a `handler` and a `request`, matches the request :params to the
  arguments of the handler. Sends nil for the args that do not exist in the request :params."
  (let [param-names (get-arglist handler)
        num-params (count param-names)]
    ; NOTE1: should this validate against some user defined metadata eg ^{:http-request true}?
    ; NOTE2: also, maybe support sending(?) metadata AND params? or is that too much hassle...?
    (cond
      (= num-params 0) ; no args handler
        (handler)
      (and (= num-params 1) (some #{(symbol "request")} param-names)) ; handler expects the http request
        (apply handler [request])
      :else ; handler defined a list of arguments
        (let [req-params (:params request)
              req-vals (destruct-arglist param-names req-params)]
          (apply handler req-vals))
    )))

(defn- default-404-handler [request]
  "The default 404 response."
  {:status 404 :body "Ooops..."})


(defn- dispatch-request [routes-tree request handler-404]
  (let [[h rp] (route request routes-tree)]
    (if (nil? h)
      ; no handler found, 404
      (handler-404 request)
      ; handler found, apply handler with args
      (->>
        ; TODO: if wrap-route-params is used, this is not needed
        (merge (get request :params {}) rp) ; merge request params with route params
        (assoc request :params) ; put it back into the request
        (handle h) ; call `handle` to execute the handler
      ))))

;;; router handler

(defn router-with-def
  "Takes a route definition, ''compiles it'' and returns a ring handler function
  that will route requests to the correct endpoint handler."
  ([routes-def] (router-with-def routes-def default-404-handler))
  ([routes-def handler-404]
    (let [tree (create-routes-tree routes-def)]
      (fn [request]
        (dispatch-request tree request handler-404)))))

(defn router-with-tree
  ([routes-tree]
    (router-with-tree routes-tree default-404-handler))
  ([routes-tree handler-404]
    (fn [request]
      (dispatch-request routes-tree request handler-404))))


