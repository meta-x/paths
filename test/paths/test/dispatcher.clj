(ns paths.test.dispatcher
  (:require [clojure.test :refer [deftest testing is]]
            [mx.paths.dispatcher :refer :all]))

; dispatcher.clj
; (dispatch routes-tree request)
(deftest dispatcher
  )

; (defn x1 []
;   (println "slash"))
; (defn four []
;   (println "404!"))

; (def routes [
;   "/" {:get #'x1}
; ])

; (def rt (t/create-routes-tree routes))
; (d/dispatch rt {:uri "/" :request-method :get})
; (d/dispatch rt {:uri "/abc" :request-method :get})
