(ns mx.paths.core
  (:require [mx.paths.utils :refer [combine]])
  )

(def PATH_DELIMITER_KEEP #"((?<=/)|(?=/))")
(def PATH_DELIMITER_DISCARD #"/")

(defn tokenize-path [path]
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

(defn create-tree [routes]
  (loop [r routes
         tree {}]
    (if-let [[path actions] (get-next-route r)]
      (let [path-tokens (tokenize-path path)
            new-branch (create-branch {} path-tokens actions)
            new-tree (combine tree new-branch)]
        (recur (drop 2 r) new-tree))
      tree)))

(defn get-node [tree token]
  (if-let [node (get tree token)]
    node
    (get tree (first (filter #(.startsWith % ":") (keys tree)))) ; TODO: clean this a bit
  ))

(defn find-path [tree tokens]
  (let [t (first tokens)]
    (if (last-token? tokens)
      (get-node tree t) ; returns the leaf nodes (aka the actions map)
      (find-path (:subroutes (get-node tree t)) (rest tokens)))
    ))

(defn route [request routes-tree]
  ; route the request to the correct handler
  (let [path (:uri request)
        method (:request-method request)
        path-tokens (tokenize-path path)]
    (-> (find-path routes-tree path-tokens)
        (get method))
    ))

(defn default-404-handler [request]
  {:status 404 :body "Ooops..."})

(defn router
  "Takes a route definition, ''compiles it'' and returns a ring handler function
  that will route requests to the correct endpoint handler."
  ([routes-def] (router routes-def default-404-handler))
  ([routes-def handler-404]
    (let [tree (create-tree routes-def)]
      (fn [request]
        (if-let [h (route request tree)]
          (apply h [request])
          (handler-404 request)
          )))))

; TODO:
; - defhandler/defn x [request]
; defhandler macro accepts any number of arguments that will be bound by name from :params
; defn takes a single "request" argument
; router middleware must know the "type" of the handler function so it can tell which dispatch to do,
; i.e. (handler request) or (do (get-handler-param-names handler) (handler (unwrap-params (:params request))))
; - how to pass the route parameter to the handler functions?
; automatically assoc into :params in "route" function
; adding route parameter to :params solves everything - binding will be done by the "dispatch" code
; - prune the tree from the delimiter (PATH_DELIMITER_KEEP keeps "/")
