(ns mx.paths.matcher
  (:require [mx.paths.tree :refer [tokenize-path last-token?]]))

;;; match helpers

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

(defn match
  "Match the request to the correct handler and returns it and its route parameters."
  [routes-tree request] ; public visibility
  (let [path (:uri request)
        method (:request-method request)
        path-tokens (tokenize-path path)
        [token node route-params] (find-path routes-tree path-tokens {})]
      [(or (get node method) (get node :any)) route-params])) ; returns [route-handler route-params]
