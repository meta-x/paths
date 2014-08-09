(ns paths.test.integrated
  (:require [clojure.test :refer [deftest testing is]]
            [mx.paths.core :refer :all]
            [mx.paths.tree :refer :all]
            [mx.paths.matcher :refer :all]
            [mx.paths.dispatcher :refer :all]
            [mx.paths.handlers :refer :all]))

(deftest integrated
  )


; no args handlers
(defn na-get [] :na-get)
(defn na-post [] :na-post)
(defn na-put [] :na-put)
(defn na-delete [] :na-delete)

; "normal" params handlers
(defn wnp-get [arg1 arg2] {:wnp-get {:arg1 arg1 :arg2 arg2}})
(defn wnp-post [arg1 arg2] {:wnp-post {:arg1 arg1 :arg2 arg2}})
(defn wnp-put [arg1 arg2] {:wnp-put {:arg1 arg1 :arg2 arg2}})
(defn wnp-delete [arg1 arg2] {:wnp-delete {:arg1 arg1 :arg2 arg2}})

; route params handlers
(defn wrp-get [arg1 arg2] {:wrp-get {:arg1 arg1 :arg2 arg2}})
(defn wrp-post [arg1 arg2] {:wrp-post {:arg1 arg1 :arg2 arg2}})
(defn wrp-put [arg1 arg2] {:wrp-put {:arg1 arg1 :arg2 arg2}})
(defn wrp-delete [arg1 arg2] {:wrp-delete {:arg1 arg1 :arg2 arg2}})

; request object param handlers
(defn wrop-get [request] {:wrop-get {:arg request}})
(defn wrop-post [request] {:wrop-post {:arg request}})
(defn wrop-put [request] {:wrop-put {:arg request}})
(defn wrop-delete [request] {:wrop-delete {:arg request}})

; wildcard route handlers
(defn wwr-get [arg] {:wwr-get {:arg arg}})
(defn wwr-post [arg] {:wwr-post {:arg arg}})
(defn wwr-put [arg] {:wwr-put {:arg arg}})
(defn wwr-delete [arg] {:wwr-delete {:arg arg}})

(def routes [
  "/no/args" {:get #'na-get :post #'na-post :put #'na-put :delete #'na-delete}
  "/with/normal/params" {:get #'wnp-get :post #'wnp-post :put #'wnp-put :delete #'wnp-delete}
  "/with/route/params/:arg1/:arg2" {:get #'wrp-get :post #'wrp-post :put #'wrp-put :delete #'wrp-delete}
  "/with/request/object/param" {:get #'wrop-get :post #'wrop-post :put #'wrop-put :delete #'wrop-delete}
  "/with/i-am-a-route-param/wildcard/:*" {:get #'wwr-get :post #'wwr-post :put #'wwr-put :delete #'wwr-delete}
])

; TODO: test plain (doesn't add new keys to the request)
; TODO: test with pathsize
; TODO: test with wrap-middleware

(def app
  (route routes))

(deftest routes-and-handlers
  (testing "no-args"
    (let [request {:uri "/no/args"}
          get-req (assoc request :request-method :get)
          put-req (assoc request :request-method :put)
          post-req (assoc request :request-method :post)
          del-req (assoc request :request-method :delete)]
      (is (= (na-get) (app get-req)) "GET")
      (is (= (na-post) (app post-req)) "POST")
      (is (= (na-put) (app put-req)) "PUT")
      (is (= (na-delete) (app del-req)) "DELETE")
    ))
  (testing "with-normal-params"
    ; TODO: these args should be in the correct place (get params in query string, form params in body, etc)
    (let [arg1 "thisisargument1-normal-params"
          arg2 "thisisargument2-normal-params"
          request {:uri (str "/with/normal/params") :params {:arg1 arg1 :arg2 arg2}}
          get-req (assoc request :request-method :get)
          put-req (assoc request :request-method :put)
          post-req (assoc request :request-method :post)
          del-req (assoc request :request-method :delete)]
      (is (= (wnp-get arg1 arg2) (app get-req)) "GET")
      (is (= (wnp-post arg1 arg2) (app post-req)) "POST")
      (is (= (wnp-put arg1 arg2) (app put-req)) "PUT")
      (is (= (wnp-delete arg1 arg2) (app del-req)) "DELETE")
    ))
  (testing "with-request-obj-param"
    (let [request {:uri "/with/request/object/param" :something :else :and :more :params {}}
          get-req (assoc request :request-method :get)
          put-req (assoc request :request-method :put)
          post-req (assoc request :request-method :post)
          del-req (assoc request :request-method :delete)]
      (is (= (wrop-get get-req) (app get-req)) "GET")
      (is (= (wrop-post post-req) (app post-req)) "POST")
      (is (= (wrop-put put-req) (app put-req)) "PUT")
      (is (= (wrop-delete del-req) (app del-req)) "DELETE")
    ))
  (testing "with-route-params"
    (let [arg1 "thisisargument1-route-params"
          arg2 "thisisargument2-route-params"
          request {:uri (str "/with/route/params/" arg1 "/" arg2)}
          get-req (assoc request :request-method :get)
          put-req (assoc request :request-method :put)
          post-req (assoc request :request-method :post)
          del-req (assoc request :request-method :delete)]
      (is (= (wrp-get arg1 arg2) (app get-req)) "GET")
      (is (= (wrp-post arg1 arg2) (app post-req)) "POST")
      (is (= (wrp-put arg1 arg2) (app put-req)) "PUT")
      (is (= (wrp-delete arg1 arg2) (app del-req)) "DELETE")
    ))
  (testing "with-wildcard-route"
    (let [route-param "i-am-a-route-param"
          normal-param "i am a query string param or maybe a form param"
          request {:uri (str "/with/" route-param "/wildcard/something/something/something/route/that/matches/anything") :params {:arg normal-param}}
          get-req (assoc request :request-method :get)
          put-req (assoc request :request-method :put)
          post-req (assoc request :request-method :post)
          del-req (assoc request :request-method :delete)]
      (is (= (wwr-get normal-param) (app get-req)) "GET")
      (is (= (wwr-post normal-param) (app post-req)) "POST")
      (is (= (wwr-put normal-param) (app put-req)) "PUT")
      (is (= (wwr-delete normal-param) (app del-req)) "DELETE")
    ))
  ; TODO: test the cases where the parameters are missing
)
