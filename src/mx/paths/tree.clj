(ns mx.paths.tree
  (:require [mx.paths.utils :refer [combine]]))

(def PATH-DELIMITER-KEEP #"((?<=/)|(?=/))")

(defn tokenize-path
  "Given \"/this/is/a/path\", returns [/ this / is / a / path]."
  [path]
  (->
    path
    (clojure.string/lower-case)
    (clojure.string/split PATH-DELIMITER-KEEP)))

(defn last-token?
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
  [routes-def]
  (loop [[path actions :as r] routes-def
         tree {}]
    (if path
      (let [path-tokens (tokenize-path path)
            new-branch (create-branch {} path-tokens actions)
            new-tree (combine tree new-branch)]
        (recur (drop 2 r) new-tree))
      tree)))
