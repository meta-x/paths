(ns mx.paths.core
  (:require [mx.paths.utils :refer [combine]])
  )

(def PATH_DELIMITER_KEEP #"((?<=/)|(?=/))")
(def PATH_DELIMITER_DISCARD #"/")

(defn- tokenize-path [path]
  (->
    (clojure.string/lower-case path)
    (clojure.string/split PATH_DELIMITER_KEEP)))

(defn- last-token? [tokens]
  (= (count tokens) 1))

(defn- create-branch [tree tokens actions]
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
  (loop [r routes
         tree {}]
    (if-let [[path actions] (get-next-route r)]
      (let [path-tokens (tokenize-path path)
            new-branch (create-branch {} path-tokens actions)
            new-tree (combine tree new-branch)]
        (recur (drop 2 r) new-tree))
      tree)))

(defn- get-node [tree token]
  (if-let [node (get tree token)]
    node
    (get tree (first (filter #(.startsWith % ":") (keys tree)))) ; TODO: clean this a bit
  ))

(defn- find-path [tree tokens]
  (let [t (first tokens)]
    (if (last-token? tokens)
      (get-node tree t) ; returns the leaf nodes (aka the actions map)
      (find-path (:subroutes (get-node tree t)) (rest tokens)))
    ))

;;; determine which handler to call

(defn- route [request routes-tree]
  ; route the request to the correct handler and returns it
  (let [path (:uri request)
        method (:request-method request)
        path-tokens (tokenize-path path)]
    (-> (find-path routes-tree path-tokens)
        (get method))
    ))

;;; determine handler parameters and automatically map them when calling

(defn- get-arglist [handler]
  (->
    (meta handler)
    (:arglists)
    (first)))

(defn- destruct-arglist [param-names req-params]
  "Given a list of `param-names`, return a list with the corresponding values from `req-params`."
  (map #(get req-params (keyword %1)) param-names))

(defn- handle [handler request]
  (let [param-names (get-arglist handler)]
    ; NOTE1: maybe this should validate against some user defined metadata eg ^{:http-request true}
    ; NOTE2: also, maybe support sending metadata AND params? too much hassle...
    (if (and (= (count param-names) 1)
             (some #{(symbol "request")} param-names))
      (apply handler [request])
      (let [req-params (:params request)
            req-vals (destruct-arglist param-names req-params)]
        (apply handler req-vals)))
    ))

(defn- default-404-handler [request]
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

; TODO:
; - how to pass the route parameter to the handler functions?
; automatically assoc into :params in "route" function
; adding route parameter to :params solves everything - binding will be done by the "dispatch" code
;
; - prune the tree from the delimiter (PATH_DELIMITER_KEEP keeps "/")
; implement my own tokenizer code? ugh
