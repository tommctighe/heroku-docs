(defproject clojure-getting-started "1.0.0-SNAPSHOT"
  :description "A viewer for Clojure docs"
  :url "http://clojure-getting-started.herokuapp.com"
  :license {:name "Eclipse Public License v1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [ring/ring-jetty-adapter "1.8.1"]
                 [environ "1.2.0"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [org.postgresql/postgresql "42.2.12"]
                 [org.clojure/data.xml "0.0.8"]
                 [hiccup "1.0.5"]]
  :min-lein-version "2.0.0"
  :plugins [[lein-environ "1.2.0"]]
  :hooks [environ.leiningen.hooks]
  :uberjar-name "clojure-getting-started-standalone.jar"
  :profiles {:production {:env {:production true}}})
