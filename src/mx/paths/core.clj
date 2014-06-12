(ns mx.paths.core
  (:require [mx.paths.utils :refer [combine]]
            [ring.util.response :refer [resource-response file-response]]
            [ring.middleware.content-type :refer [content-type-response]]
            [ring.middleware.not-modified :refer [not-modified-response]]
            [ring.middleware.head :refer [head-response]]))

(declare
  create-routes-tree route
  router-with-def router-with-tree
  bind-query-routes-def bind-query-routes-tree
  resource-handler file-handler)

; TODO:
; - prune the tree from the delimiter (PATH-DELIMITER-KEEP keeps "/") - implement my own tokenizer code? ugh
; - should really implement the feature where I can send the request object with other parameters
; - check https://github.com/ztellman/automat for faster tokenization

(def PATH-DELIMITER-KEEP #"((?<=/)|(?=/))")
(def PATH-DELIMITER-DISCARD #"/")

(def request-obj "request")

(defn- tokenize-path
  "Given /this/is/a/path, returns [/ this / is / a / path].
  TODO: should actually return [/ this is a path]."
  [path]
  (->
    path
    (clojure.string/lower-case)
    (clojure.string/split PATH-DELIMITER-KEEP)))

(defn- last-token?
  [tokens]
  (= (count tokens) 1))

(defn- create-branch
  "Iterates through `tokens` and creates a tree-like structure attaching `actions`
  to the leaf node."
  [tree tokens actions]
  (let [t (first tokens)]
    (let [r-t (get tree t {})]
      (if (last-token? tokens)
        (->>
          actions
          (merge r-t)
          (assoc tree t))
        (assoc tree t {:subroutes (create-branch tree (rest tokens) actions)})))))

(defn create-routes-tree
  "Given a routes definition, returns a routes tree that is used by the routing
  function to send requests to the correct handler."
  [routes-def] ; public visibility
  (loop [[path actions :as r] routes-def
         tree {}]
    (if path
      (let [path-tokens (tokenize-path path)
            new-branch (create-branch {} path-tokens actions)
            new-tree (combine tree new-branch)]
        (recur (drop 2 r) new-tree))
      tree)))

;;; helpers for finding the handler for a given path

(defn- is-wildcard-or-route-param
  [n]
  (.startsWith n ":"))

(defn- get-wc-rp-node
  "Helper for tree navigation. Returns the wildcard node or nil."
  [tree]
  (->
    is-wildcard-or-route-param
    (filter (keys tree))
    (first)))

(defn- wc->kw
  "Helper that converts a :wildcard into a :keyword."
  [wc]
  (->
    wc
    (subs 1)
    (keyword)))

(defn- get-node
  "Helper for tree navigation - understands the route-param/wilcard `:` token."
  [tree token route-params]
  (if-let [node (get tree token)] ; try to find the token in the current level
    [token node route-params] ; if found, returns [tree-token node route-params]
    (if-let [wc-rp (get-wc-rp-node tree)] ; if not found, try to match with a wildcard/route-param token
      (let [wc-rp-kw (wc->kw wc-rp)]
        [wc-rp-kw (get tree wc-rp) (assoc route-params wc-rp-kw token)])
      [nil nil route-params])))

(defn- find-path
  [tree tokens route-params]
  (let [pt (first tokens) ; path-token
        [tt n rp :as token-node-rp] (get-node tree pt route-params)] ; tree-token, node, route-params
    (cond
      (last-token? tokens) token-node-rp ; last token, return [token leaf-node route-params]
      (= tt :*) [tt n (assoc route-params :* tokens)] ; wildcard token, fix route-params value
      :else (find-path (:subroutes n) (rest tokens) rp)))) ; otherwise keep navigating the tree

;;; determine which handler to call

(defn route
  "Route the request to the correct handler and returns it."
  [request routes-tree] ; public visibility
  (let [path (:uri request)
        method (:request-method request)
        path-tokens (tokenize-path path)
        [token node route-params] (find-path routes-tree path-tokens {})]
      [(or (get node method) (get node :any)) route-params])) ; returns [handler route-params]

;;; determine handler parameters and automagically map them when calling

(defn- get-arglist
  "Helper for retrieving the argument list from a function (var)."
  [handler]
  (->
    handler
    (meta)
    (:arglists)
    (first)))

(defn- get-param-name
  [param]
  (or (:name (meta param))
      (keyword param)))

(defn- destruct-arglist
  "Given a list of `param-names`, return a list with the corresponding values from `req-params`."
  [param-names req-params]
  (map #(get req-params (get-param-name %1)) param-names))

(defn- handle
  "Helper that given a `handler` and a `request`, matches the request :params to the
  arguments of the handler. Sends nil for the args that do not exist in the request :params."
  [handler request]
  (let [param-names (get-arglist handler)
        num-params (count param-names)]
    ; NOTE1: should this validate against some user defined metadata eg ^{:http-request true}?
    ; NOTE2: also, maybe support sending(?) metadata AND params? or is that too much hassle...?
    (cond
      (= num-params 0) ; no args handler
        (handler)
      (and (= num-params 1) (some #{(symbol request-obj)} param-names)) ; handler expects the http request
        (apply handler [request])
      :else ; handler defined a list of arguments
        (let [req-params (:params request)
              req-vals (destruct-arglist param-names req-params)]
          (apply handler req-vals)))))

(defn- default-404-handler
  "The default 404 response."
  [request]
  {:status 404 :body "Ooops..."})

(defn- dispatch-request
  [routes-tree request handler-404]
  (let [[h rp] (route request routes-tree)]
    (if (nil? h)
      ; no handler found, 404
      (handler-404 request)
      ; handler found, apply handler with args
      (->> ; TODO: if wrap-route-params is used, this is not needed
        rp ; route params
        (merge (get request :params {})) ; merge request params with route params
        (assoc request :params) ; put it back into the request
        (handle h))))) ; call `handle` to execute the handler

;;; resource and file handling

(defn handle-resource
  "The default handler for resource requests."
  [request]
  (->
    request
    (:uri)
    (resource-response)
    (content-type-response request)
    (not-modified-response request)
    (head-response request)))

(def resource-handler
  "Quick helper to be used in resource route definition.
  Add this \"/my-resource-path/:*\" resource-handler to your `paths` definition."
  {:any #'handle-resource})

(defn handle-file
  "The default handler for static file requests."
  [request]
  (->
    request
    (:uri)
    (file-response {:root "resources/public"})
    (content-type-response request)
    (not-modified-response request)
    (head-response request)))

(def file-handler
  "Quick helper to be used in static file route definition.
  Add this \"/my-file-path/:*\" file-handler to your `paths` definition."
  {:any #'handle-file})

;;; router handler

(defn router-with-def
  "Takes a route definition, ''compiles it'' and returns a ring handler function
  that will route requests to the correct endpoint handler."
  ([routes-def]
    (router-with-def routes-def default-404-handler))
  ([routes-def handler-404]
    (let [tree (create-routes-tree routes-def)]
      (fn [request]
        (dispatch-request tree request handler-404)))))

(defn router-with-tree
  "Takes a previously compiled route-tree and returns a ring handler function
  that will route requests to the correct endpoint handler."
  ([routes-tree]
    (router-with-tree routes-tree default-404-handler))
  ([routes-tree handler-404]
    (fn [request]
      (dispatch-request routes-tree request handler-404))))

(defn bind-query-routes-tree
  "bind-query returns a function that let's you query the routes tree.
  This version takes a precompiled routes-tree as argument."
  [routes-tree]
  (fn [request]
    (route request routes-tree)))

(defn bind-query-routes-def
  "bind-query returns a function that let's you query the routes tree.
  This version takes the routes definition as argument."
  [routes-def]
  (fn [request]
    (route request (create-routes-tree routes-def))))
