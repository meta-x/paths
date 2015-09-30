(defproject paths "0.1.1"
  :description "(Enhanced) routing library for Clojure/Ring."
  :url "https://github.com/meta-x/paths"
  :license {
    :name "The MIT License"
    :url "http://opensource.org/licenses/MIT"
  }
  :dependencies [
    [org.clojure/clojure "1.6.0"]
    [ring/ring-core "1.3.0"]
  ]
  :deploy-repositories [
    ["clojars" {:sign-releases false}]
  ]
)
