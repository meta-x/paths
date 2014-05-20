(ns mx.paths.core
  (:require [mx.paths.utils :refer [combine]])
  )

(def PATH_DELIMITER_KEEP #"((?<=/)|(?=/))")
(def PATH_DELIMITER_DISCARD #"/")

(defn tokenize-path [path]
  (clojure.string/split path PATH_DELIMITER_KEEP))

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
      tree))
  )



(defn find-path [tree tokens]
  (let [t (first tokens)]
    (if (last-token? tokens)
      ; TODO implement logic to treat :id/:etc elements as wildcards
      ; if -not-found-, find a key that starts with wildcard element ':''
      ; and use it. in other frameworks/routing libs, this value is passed as an argument
      ; to the handler function
      (get tree t) ; returns the leaf nodes (aka the actions map)
      (find-path (:subroutes (get tree t)) (rest tokens)))
    ))

(defn route [request routes-tree]
  ; route the request to the correct handler
  (let [path (:uri request)
        method (:request-method request)
        path-tokens (tokenize-path path)]
    (-> (find-path routes-tree path-tokens)
        (get method))
    ))



(defn router [routes-def]
  "takes a route definition, ''compiles it'' and returns a ring handler function
  that will route requests to the correct endpoint handler"
  (let [tree (create-tree routes-def)]
    (fn [request]
      ; TODO: call the handler function
      ; if it's a simple defn, pass the request
      ; if it's a defhandler, unwrap arguments from :params and pass them in the correct order
      ; e.g. defined by the defhandler macro
      ; (let [handler (route request tree)]
      ;   (handler request)
      ; )
      (route request tree)
      )))
