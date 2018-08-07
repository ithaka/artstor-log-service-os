(defproject artstor-log-service "0.1.0-SNAPSHOT"
  :description "log service for ui"
  :url "http://artstor.org"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :repositories [["central-proxy" "http://repository.sonatype.org/content/repositories/central/"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [prismatic/schema "1.1.3"]                
                 [metosin/compojure-api "1.2.0-alpha1"]
                 [ring/ring-mock "0.3.2"]
                 [ring "1.4.0"]]
  :plugins [[lein-modules "0.3.11"]
            [lein-ring "0.8.12"]
            [ring "1.4.0"]
            [ring/ring-mock "0.3.0"]
            [clojusc/ring-redis-session "3.1.0"]
            [hiccup "1.0.5"]
            [cheshire "5.7.0"]
            [clj-time "0.13.0"]
            [environ "1.1.0"]
            [lein-environ "1.1.0"]]
  :main ^:skip-aot clj-app-container.core
  :modules {:parent "../.."}
  :profiles {:test {:dependencies [[org.clojure/tools.nrepl "0.2.12"]
                                   [org.clojure/test.check "0.9.0"]]}
             :uberjar {:aot :all}})
