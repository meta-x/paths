(ns mx.paths.handlers
  (:require [ring.util.response :refer [resource-response file-response]]
            [ring.middleware.content-type :refer [content-type-response]]
            [ring.middleware.not-modified :refer [not-modified-response]]
            [ring.middleware.head :refer [head-response]]))

;;; resource and file handling

(defn- handle-resource
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

(defn- handle-file
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

(defn- handle-404
  "The default 404 response."
  [request]
  {:status 404 :body "Ooops..."})

(def fourzerofour-handler
  "Quick helper to be used in 404 route definition.
  Add this :404 fourzerofour-handler to your `paths` definition."
  {:any #'handle-404})
