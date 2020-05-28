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
            [templates.views.content :as content]))

(defn mark-it-up [strings]
  (for [{:keys [kingdom]} strings]
    (format "<p><strong>%s<strong></p>" kingdom))
  )

(defn make-html [rows]
  (for [kingdom rows]
    (str)))

(defn build-docs []
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (let [kingdoms (get-kingdoms)
               html (mark-it-up kingdoms)]
           html)})

(defn record [input]
  (db/insert! (env :database-url "postgres://localhost:5432/docs")
              :sayings {:content input}))

(defroutes app
  (GET "/docs" []
       (build-docs))
  (GET "/" {params :params}
       (layout/app "Home" (content/index params)))
  (route/resources "/")
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
