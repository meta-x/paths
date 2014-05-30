(defproject paths_example "0.1"
  :description "An example of how to use paths."
  :license {
    :name "The MIT License"
    :url "http://opensource.org/licenses/MIT"
  }
  :dependencies [
    [org.clojure/clojure "1.6.0"]
    [paths "0.1.0-beta2"]
    [ring/ring-core "1.3.0-RC1"]
    [ring/ring-jetty-adapter "1.3.0-RC1"]
  ]
  :profiles {
    :dev {
      :plugins [
        [lein-ring "0.8.10"]
        [ring/ring-devel "1.2.2"]
      ]
    }
  }
  :ring {:handler paths-example.core/app}
)
