(ns paths-example.core
  (:require [mx.paths.core :refer [router-with-def]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :refer [response]])
  )

(defn index []
  (response "<div>hi</div>
            <div>you should be running this with an eye on the <a href='https://github.com/meta-x/paths/blob/master/examples/paths_example/src/paths_example/core.clj'>example's source code</a>.</div>
             <a href='/basic'>handler that takes the request object as argument</a><br/>
             <a href='/with/args?p1=1&amp;p2=2'>handler that takes 2 different arguments</a><br/>
             <a href='/with/no/args'>handler that takes no arguments</a><br/>
             <a href='/this/accepts/anything/yeah'>wildcard example</a><br/>
             <a href='/this/is/an/endpoint?what=1'>GET</a><br/>
             <span>curl -X POST http://localhost:3000/this/is/an/endpoint?param=2</span><br/>
             <span>curl -X DELETE http://localhost:3000/this/is/an/endpoint?param=3</span><br/>
             <span>curl -X PUT http://localhost:3000/this/is/an/endpoint?param=4</span><br/>
            "))
(defn basic-handler [request]
  (println "--- basic-handler")
  (response (str request)))
(defn handler-with-args [p1 p2]
  (println "--- handler-with-args")
  (println p1)
  (println p2)
  (response (str p1 "\n" p2)))
(defn handler-with-no-args []
  (println "--- handler-with-no-args")
  (response "no args"))
(defn handler-wildcard [wildcards]
  (println "--- handler-wildcard")
  (response wildcards))
(defn handler-get [^{:name :what}param]
  (println "--- handler-get")
  (response (str "you-got-me" "\n" param)))
(defn handler-post [param]
  (println "--- handler-post")
  (response (str "you-posted-me" "\n" param)))
(defn handler-delete [param]
  (println "--- handler-delete")
  (response (str "you-deleted-me" "\n" param)))
(defn handler-put [param]
  (println "--- handler-put")
  (response (str "you-put-me" "\n" param)))


(def routes [
  "/" {:get #'index}
  "/basic" {:get #'basic-handler}
  "/with/args" {:get #'handler-with-args}
  "/with/no/args" {:get #'handler-with-no-args}
  "/this/accepts/:wildcards/yeah" {:get #'handler-wildcard}
  "/this/is/an/endpoint" {:get #'handler-get :delete #'handler-delete :post #'handler-post :put #'handler-put}
  ])


(def app
  (->
    (router-with-def routes)
    (wrap-keyword-params)
    (wrap-params)
    ))
