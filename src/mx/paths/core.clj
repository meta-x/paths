(ns mx.paths.core
  (:require [mx.paths.utils :refer [combine]])
  )

(def PATH_DELIMITER_KEEP #"((?<=/)|(?=/))")
(def PATH_DELIMITER_DISCARD #"/")

(defn tokenize-path [path]
  (clojure.string/split path PATH_DELIMITER_DISCARD))

(defn- last-token? [tokens]
  (= (count tokens) 1))

(defn create-branch [tree tokens actions]
  (if-let [t (first tokens)]
    (let [r-t (get tree t {})]
      (if (last-token? tokens)
        (->> (merge r-t actions)
             (assoc tree t))
        (assoc tree t {:subroutes (create-branch tree (rest tokens) actions)})))
    tree))

(defn- get-next-route [[path actions]]
  (if (nil? path)
    nil
    [path actions]))

(defn create-tree [routes]
  (loop [r routes
         tree {}]
    (if-let [[path actions] (get-next-route r)]
      (do
        (println path)
        (let [path-tokens (tokenize-path path)
              new-branch (create-branch {} path-tokens actions)
              new-tree (combine tree new-branch)]
          (println new-tree)
          (println "----------------------------------")
          (recur (drop 2 r) new-tree)))
      tree))
  )


(defn route [request routes]
  ; route the request to the correct handler
  (let [path (:uri request)
        routes (:routes route)]

    ))



(defn router [route-def]
  "takes a route definition, ''compiles it'' and returns a ring handler function
  that will route requests to the correct endpoint handler"
  (fn [request]
    ; todo: (route request routes)
    )
  )

