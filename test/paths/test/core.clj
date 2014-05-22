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
(defn woot$get []
  {
    :status 200
    :body "GET /woot"
  })
(defn woot$post [arg1 arg2 arg3]
  (println arg1)
  (println arg2)
  (println arg3)
  {
    :status 200
    :body "POST /woot"
  })
(defn echo$get [request]
  (println request)
  {
    :status 200
    :body "GET /echo"
  })
(defn echo$post [id]
  (println id)
  {
    :status 200
    :body "POST /echo"
  })

(def routes
  [
    "/echo/:id/stuff" {:get #'echo$get :post #'echo$post}
    "/woot" {:get #'woot$get :post #'woot$post}
    "/" {:get #'index$get}
    "/user" {:post #'user$post}
    "/user/:id" {:get #'user$get :delete #'user$delete :put #'user$put}
    "/user/:id/account" {:post #'account$post}
    "/user/:id/account/:id" {:get #'account$get :put #'account$put :delete #'account$delete}
  ])

(def req1 {:uri "/" :request-method :get}) ; GET /
(def req2 {:uri "/user" :request-method :get}) ; GET /user
(def req3 {:uri "/user/5/account" :request-method :post}) ; GET /user/5/account
(def req4 {:uri "/user/5/account/1" :request-method :get}) ; GET /user/5/account/1
(def req5 {:uri "/user/5/account/1" :request-method :delete}) ; DELETE /user/5/account/1
(def req6 {:uri "/woot" :request-method :get}) ; GET /woot
(def req7 {:uri "/woot" :request-method :post :params {:arg1 "arg1" :arg2 "arg2"}}) ; POST /woot
(def req8 {:uri "/echo/5/stuff" :request-method :get}) ; GET /echo
(def req9 {:uri "/echo/124fzfsa/stuff" :request-method :post}) ; POST /echo

(defn my-404-handler [request]
  {:status 418
   :body "YOU are a teapot :)"})
(def r (paths/router routes my-404-handler))

; (r req1)
; (r req2)
; (r req3)
; (r req4)
; (r req5)
; (r req6)
; (r req7)
; (r req8)
; (r req9)

; (use 'paths.test.core :reload-all)


; (defn my-fn1 [request arg1 arg2 arg3]
;   (println arg1)
;   (println arg2)
;   (println arg3)
;   )
; ;(my-fn1 1 2 3)

; (defn my-fn2 [request]
;   (println request)
;   )

; (require '[paths.test.core :as test])
; (require '[mx.paths.core :as paths])
; (paths/handle test/my-fn1 {:params {:arg1 1 :arg2 2 :arg3 3}})



; (use 'mx.paths.core :reload-all)
; (def routes-tree (create-tree routes2))
; (route {:request-method :get :uri "/user/:id/account/:id"} routes-tree)


; (handle my-fn1 {:params {:arg1 1 :arg2 2 :arg3 3}})

; (handle my-fn2 {:headers {:something "here"} :request-method :get})



