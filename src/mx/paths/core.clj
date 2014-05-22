(ns mx.paths.core
  (:require [mx.paths.utils :refer [combine]])
  )

; TODO:
; - how to pass the route parameter to the handler functions?
; automatically assoc into :params in "route" function
; adding route parameter to :params solves everything - binding will be done by the "dispatch" code
;
; - prune the tree from the delimiter (PATH_DELIMITER_KEEP keeps "/")
; implement my own tokenizer code? ugh

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

(defn- create-tree [routes]
  "Given a routes definition, returns a routes tree that is used by the routing
  function to send requests to the correct handler."
  (loop [r routes
         tree {}]
    (if-let [[path actions] (get-next-route r)]
      (let [path-tokens (tokenize-path path)
            new-branch (create-branch {} path-tokens actions)
            new-tree (combine tree new-branch)]
        (recur (drop 2 r) new-tree))
      tree)))

(defn- get-wildcard-node [tree]
  "Helper for tree navigation. Returns the wildcard node or nil."
  (-> (filter #(.startsWith % ":") (keys tree)))
      (first))

(defn- get-node [tree token]
  "Helper for tree navigation - understands the wilcard `:` token."
  (if-let [node (get tree token)] ; try to find the token in the current level
    node ; if found, returns the node; if not found, try to match with a wildcard token
    (get tree (get-wildcard-node tree))
  ))

(defn- find-path [tree tokens]
  (let [t (first tokens)]
    (if (last-token? tokens)
      (get-node tree t) ; returns the leaf nodes (aka the actions map)
      (find-path (:subroutes (get-node tree t)) (rest tokens)))
    ))

;;; determine which handler to call

(defn- route [request routes-tree]
  "Route the request to the correct handler and returns it"
  (let [path (:uri request)
        method (:request-method request)
        path-tokens (tokenize-path path)]
    (-> (find-path routes-tree path-tokens)
        (get method))
    ))

;;; determine handler parameters and automatically map them when calling

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

(defn router
  "Takes a route definition, ''compiles it'' and returns a ring handler function
  that will route requests to the correct endpoint handler."
  ([routes-def] (router routes-def default-404-handler))
  ([routes-def handler-404]
    (let [tree (create-tree routes-def)]
      (fn [request]
        (if-let [h (route request tree)] ; the handler in the routes definition has to be a var!!
          (handle h request)
          (handler-404 request)
          )))))
