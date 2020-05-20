(ns clojure-getting-started.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as db]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [camel-snake-kebab.core :as csk]))

(def sample (env :sample "sample-string-a-ma-jig"))

(defn splash []
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (concat (for [kind ["camel" "snake" "kebab"]]
                   (format "make into <a href='/%s?input=%s'> %s</a> case... </a><br />"
                    kind sample kind))
                ["<hr /><ul>"]
                (for [s (db/query (env :database-url)
                                  ["select content from sayings"])]
                  (format "<li>%s</li>" (:content s)))
                ["</ul>"])})

(defn record [input]
  (db/insert! (env :database-url "postgres://localhost:5432/kebabs")
              :sayings {:content input}))

(defroutes app
  (GET "/camel" {{input :input} :params}
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (csk/->camelCase input)})
  (GET "/snake" {{input :input} :params}
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (csk/->snake_case input)})
  (GET "/kebab" {{input :input} :params}
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
