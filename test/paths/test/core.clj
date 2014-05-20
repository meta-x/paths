(ns paths.test.core
  (:require [clojure.test :refer [deftest testing]]
            [mx.paths.core :as paths]
    )
  )
;            [ring.mock.request :refer [request]]

; TODO: implement

(defn index$get [request]
  {
    :status 200
    :body "GET /"
  })

(defn user$post [request]
  {
    :status 200
    :body "POST /user"
  })
(defn user$get [request]
  {
    :status 200
    :body "GET /user/:id"
  })
(defn user$put [request]
  {
    :status 200
    :body "PUT /user/:id"
  })
(defn user$delete [request]
  {
    :status 200
    :body "DELETE /user/:id"
  })

(defn account$post [request]
  {
    :status 200
    :body "DELETE /user/:id/account"
  })
(defn account$get [request]
  {
    :status 200
    :body "GET /user/:id/account/:id"
  })
(defn account$put [request]
  {
    :status 200
    :body "PUT /user/:id/account/:id"
  })
(defn account$delete [request]
  {
    :status 200
    :body "DELETE /user/:id/account/:id"
  })

(def routes
  [
    "/" {:get index$get}
    "/user" {:post user$post}
    "/user/:id" {:get user$get :delete user$delete :put user$put}
    "/user/:id/account" {:post account$post}
    "/user/:id/account/:id" {:get account$get :put account$put :delete account$delete}
  ])

(def req1 {:uri "/" :request-method :get}) ; GET /
(def req2 {:uri "/user" :request-method :get}) ; GET /user
(def req3 {:uri "/user/5/account" :request-method :post}) ; GET /user/5/account
(def req4 {:uri "/user/5/account/1" :request-method :get}) ; GET /user/5/account/1
(def req5 {:uri "/user/5/account/1" :request-method :delete}) ; DELETE /user/5/account/1

(def r (paths/router routes))
; (r req1)
; (r req2)
; (r req3)
; (r req4)
; (r req5)

; (use 'paths.test.core :reload-all)



; (use 'mx.paths.core :reload-all)
; (def routes-tree (create-tree routes2))
; (route {:request-method :get :uri "/user/:id/account/:id"} routes-tree)
