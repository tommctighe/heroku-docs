(ns scrape
  (:import [org.jsoup Jsoup])
  (:require
            [clojure.java.io :as io]
            [clojure.java.jdbc :as db]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [clojure.data.xml :refer :all]
            [clojure.pprint :as p]
            [templates.views.layout :as layout]))

(map second (re-seq #":body\s(.*?)\s:\w+?\s" "<a><![CDATA[\nfoo :body 1 :editors  \"(reduce + [1 2 3 4 5])  ;;=> 15\\n(reduce + [])           ;;=> 0\\n(reduce + [1])  :editors        ;;=> 1\\n(reduce + [1 2])        ;;=> 3\\n(reduce + 1 [])  :body 2 :editors       ;;=> 1\\n(reduce + 1 [2 3])      ;;=> 6\"  bar\n]]><![CDATA[\nbaz\n]]></a>"))

(def code2 ";; make an atomic list\\n(def players (atom ()))\\n;; #'user/players\\n\\n;; conjoin a keyword into that list\\n(swap! players conj :player1)\\n;;=> (:player1)\\n\\n;; conjyoin a second keyword into the list\\n(swap! players conj :player2)\\n;;=> (:player2 :player1)\\n\\n;; take a look at what is in the list\\n(deref players)\\n;;=> (:player2 :player1)")

(defn cdata-to-string [str]
  (replace str #"\\n" "\n"))

(defn get-rows []
  (db/query (env :database-url "postgres://localhost:5432/docs")
                                  ["select name, url from item limit 6"]))

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
        example (slurpy url)]
    (str name ": " example)))

(defn scrape []
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (let [rows (get-rows)
               html (make-html rows)]
           html)})





(defn link-count [url]
  (let [conn (Jsoup/connect url)
        page (.get conn)
        text (.data (first (.select page "script")))]
    text))


(defn splash [sample]
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
