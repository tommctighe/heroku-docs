(ns clojure-getting-started.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as db]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [clojure.pprint :as p]
            [hiccup-templates.views.layout :as layout]
            [hiccup-templates.views.contents :as contents]
            [camel-snake-kebab.core :as csk]))

(def sample (env :sample "sample-string-a-ma-jig"))

(defn splash []
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (concat (for [kind ["camel" "snake" "kebab"]]
                   (format "make into <a href='/%s?input=%s'> %s</a> case... </a><br />"
                    kind sample kind))
                ["<hr /><ul>"]
                (for [s (db/query (env :database-url "postgres://localhost:5432/docs")
                                  ["select content from sayings"])]
                  (format "<li>%s</li>" (:content s)))
                ["</ul>"])})

(defn get-kingdoms []
  (db/query (env :database-url "postgres://localhost:5432/docs")
                                  ["select distinct kingdom from core order by kingdom asc limit 30"]))

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
  (GET "/camel" {{input :input} :params}
       (record (csk/->camelCase input))
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (csk/->camelCase input)})
  (GET "/snake" {{input :input} :params}
       (record (csk/->snake_case input))
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (csk/->snake_case input)})
  (GET "/kebab" {{input :input} :params}
       (record (csk/->kebab-case input))
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (csk/->kebab-case input)})
  (GET "/" []
       (splash))
  (ANY "*" []
       (route/not-found (slurp (io/resource "404.html")))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (site #'app) {:port port :join? false})))

;; For interactive development:
;; (.stop server)
;; (def server (-main))
