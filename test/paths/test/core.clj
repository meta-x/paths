(ns paths.test.core
  (:require [clojure.test :refer [deftest testing]]
            [ring.mock.request :refer [request]]
            [mx.paths.core :as paths]
    )
  )



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

(def routes-def
  {
    :name
    :protocol
    :domain
    :routes [
      "/" {:get index$get}
      ["/user" {:post user$post}
       ["/:id" {:get user$get :delete user$delete :put user$put}]
       ["/account" {:post account$post}
        ["/:id" {:get account$get :put account$put :delete account$delete}]
       ]
      ]
    ]
  })


(def req1 ; GET /
  {
    :ssl-client-cert nil,
    :remote-addr "127.0.0.1",
    :params {},
    :headers {
      host "localhost:3000",
      user-agent "curl/7.35.0",
      accept "*/*"
    },
    :server-port "3000",
    :content-length nil,
    :form-params {},
    :query-params {},
    :content-type nil,
    :character-encoding nil,
    :uri "/",
    :server-name localhost,
    :query-string nil,
    :body "this is the request body",
    :scheme :http,
    :request-method :get
  })
(def req2 ; GET /user
  {
    :ssl-client-cert nil,
    :remote-addr "127.0.0.1",
    :params {},
    :headers {
      host "localhost:3000",
      user-agent "curl/7.35.0",
      accept "*/*"
    },
    :server-port "3000",
    :content-length nil,
    :form-params {},
    :query-params {},
    :content-type nil,
    :character-encoding nil,
    :uri "/user"
    :server-name localhost,
    :query-string nil,
    :body "this is the request body",
    :scheme :http,
    :request-method :get
  })
(def req3 ; GET /user/5/account
  {
    :ssl-client-cert nil,
    :remote-addr "127.0.0.1",
    :params {},
    :headers {
      host "localhost:3000",
      user-agent "curl/7.35.0",
      accept "*/*"
    },
    :server-port "3000",
    :content-length nil,
    :form-params {},
    :query-params {},
    :content-type nil,
    :character-encoding nil,
    :uri "/user/5/account"
    :server-name localhost,
    :query-string nil,
    :body "this is the request body",
    :scheme :http,
    :request-method :get
  })
(def req4 ; GET /user/5/account/1
  {
    :ssl-client-cert nil,
    :remote-addr "127.0.0.1",
    :params {},
    :headers {
      host "localhost:3000",
      user-agent "curl/7.35.0",
      accept "*/*"
    },
    :server-port "3000",
    :content-length nil,
    :form-params {},
    :query-params {},
    :content-type nil,
    :character-encoding nil,
    :uri "/user/5/account/1"
    :server-name localhost,
    :query-string nil,
    :body "this is the request body",
    :scheme :http,
    :request-method :get
  })
(def req5 ; DELETE /user/5/account/1
  {
    :ssl-client-cert nil,
    :remote-addr "127.0.0.1",
    :params {},
    :headers {
      host "localhost:3000",
      user-agent "curl/7.35.0",
      accept "*/*"
    },
    :server-port "3000",
    :content-length nil,
    :form-params {},
    :query-params {},
    :content-type nil,
    :character-encoding nil,
    :uri "/user/5/account/1",
    :server-name localhost,
    :query-string nil,
    :body "this is the request body",
    :scheme :http,
    :request-method :delete
  })



;(paths/defroutes routes-def)

; (paths/route req1 routes-def)
; (paths/route req2 routes-def)
; (paths/route req3 routes-def)
; (paths/route req4 routes-def)
; (paths/route req5 routes-def)










