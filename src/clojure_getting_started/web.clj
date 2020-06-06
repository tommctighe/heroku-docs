(ns clojure-getting-started.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as db]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [clojure.pprint :as p]
            [templates.views.layout :as layout]
            [templates.views.content :as content]
            [scrape :as scraper]))

(defroutes app
  (GET "/scrape" [] (layout/scrape-layout "Scrape"))
  (GET "/" {params :params}
        (layout/app "/" (content/index params)))
  (route/resources "/")
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
