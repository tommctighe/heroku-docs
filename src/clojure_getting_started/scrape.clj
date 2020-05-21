(ns clojure-getting-started.web
  (:import [org.jsoup Jsoup])
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clojure.java.jdbc :as db]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [clojure.data.xml :refer :all]
            [clojure.pprint :as p]
            [camel-snake-kebab.core :as csk]))

(def sample (env :sample "sample-string-a-ma-jig"))

(map second (re-seq #":body\s(.*?)\s:\w+?\s" "<a><![CDATA[\nfoo :body 1 :editors  \"(reduce + [1 2 3 4 5])  ;;=> 15\\n(reduce + [])           ;;=> 0\\n(reduce + [1])  :editors        ;;=> 1\\n(reduce + [1 2])        ;;=> 3\\n(reduce + 1 [])  :body 2 :editors       ;;=> 1\\n(reduce + 1 [2 3])      ;;=> 6\"  bar\n]]><![CDATA[\nbaz\n]]></a>"))
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

(nth '(["cat" "dog"]) 0)

(defn link-count [url]
  (let [conn (Jsoup/connect url)
        page (.get conn)
        text (.data (first (.select page "script")))]
    text))

(defn get-rows []
  (db/query (env :database-url "postgres://localhost:5432/docs")
                                  ["select name, url from core limit 30"]))

(defn slurpy [url]
  (let [html (slurp url)
        example-strings (map second (re-seq #":body\s\\\"(.*?)\\\"," html))]
    example-strings))

(defn mark-it-up [strings]
  (for [string strings]
    (format "<br>%s" string))
  )

(defn make-html [rows]
  (for [{:keys [name url]} rows
        example (mark-it-up (slurpy url))]
    (str "<p>" name ": " example "</p>")))

(defn scrape []
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (let [rows (get-rows)
               html (make-html rows)]
           html)})

(defn record [input]
  (db/insert! (env :database-url "postgres://localhost:5432/docs")
              :sayings {:content input}))

(defroutes app
  (GET "/scrape" []
       (scrape))
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
