(ns templates.views.content
  (:use [hiccup.form]
        [hiccup.element :only (link-to)]
        [clojure.pprint :as pp]
        [clojure.java.jdbc :as db]
        [environ.core :refer [env]]
        [clojure.string :as s]
        [clojure.pprint :as pprint]
        [rewrite-clj.parser :as p]))

(defn attr-str [s]
  (.toLowerCase (replace (replace s " " "-") "," "")))

(defn top-query [] (db/query (env :database-url "postgres://localhost:5432/docs")["select name, id as kingdom_id from kingdom ORDER BY kingdom.name ASC"]))

(defn kingdom-query [id] (db/query (env :database-url "postgres://localhost:5432/docs")["select kingdom.id as kingdom_id, kingdom.name as title, clan.name as name, clan.id as clan_id from kingdom, clan where clan.kingdom_id = kingdom.id AND kingdom_id = ?" id]))

(defn clan-query [id] (db/query (env :database-url "postgres://localhost:5432/docs")["select kingdom.id as kingdom_id, clan.id as clan_id, clan.name as title, family.name as name, family.id as family_id from kingdom, clan, family where family.clan_id = clan.id AND kingdom.id = clan.kingdom_id AND clan_id = ?" id]))

(defn family-query [id] (db/query (env :database-url "postgres://localhost:5432/docs")["select kingdom.id as kingdom_id, clan.id as clan_id, family.id as family_id, family.name as title, item.name as name, item.id as item_id from kingdom, clan, family, item where item.family_id = family.id and family.clan_id = clan.id AND kingdom.id = clan.kingdom_id AND family_id = ?" id]))

(defn item-query [id] (db/query (env :database-url "postgres://localhost:5432/docs")["select item.name as title from item where item.id = ?" id]))

(defn scrape-query [] (db/query (env :database-url "postgres://localhost:5432/docs") ["select id as item_id, url from item limit 12"]))

(defn get-query
  ([name]
   (name {
          :top (top-query)
          :scrape (scrape-query)}))
  ([name id]
   (name {
     :kingdom (kingdom-query id)
     :clan (clan-query id)
     :family (family-query id)
     :item (item-query id)})))

(defn slurp-example-strings [url]
  (let [html (slurp url)
        example-strings (map second (re-seq #":body\s\\\"(.*?)\\\"," html))]
    example-strings))

(defn get-title [rows] (get (first rows) :title "Clojure"))
(defn get-section-id [title](attr-str title))

(defn cdata-to-string [str]
  (replace str #"\\\\n" "\n"))

(defn make-section [rows class-name]
  (let [title (get-title rows)
        s-id (get-section-id title)]
    [:section {:id s-id :class class-name}
     [:h2 title]
     [:div {:class "note"} [:p "A little rap about this section..."]]
     [:div {:class "cards"}
      (for [kv rows]
        (let [kingdom (str "/?kingdom=" (get kv :kingdom_id ""))
              clan (if (:clan_id kv) (str "&clan=" (:clan_id kv)) "")
              family (if (:family_id kv) (str "&family=" (:family_id kv)) "")
              item (if (:item_id kv) (str "&item=" (:item_id kv)) "")
              name (get kv :name "")
              anchor (str "#" (attr-str name))
              href-str (str kingdom clan family item anchor)]
          [:a {:href href-str}
           [:div {:class "card"} name]]))]]))

(defn make-item-section [rows class-name]
  (let [title (get-title rows)
        s-id (get-section-id title)
        s-name (meta (resolve (symbol title)))]
    [:section {:id s-id :class class-name}
     [:h2 title]
     [:div {:class "note"} [:p "A little rap about this section..."]]     
     [:p {:class "docstring"} (str (:doc s-name))]
     [:p {:class "arglists"} (str (:arglists s-name))]
     [:p "(for [ex examples]"]
     [:pre {:class "prettyprint lang-clj"} (cdata-to-string "code...")]]))

(defn make-scrape-section [rows]
  [:section {:id "scrape"}
     [:h2 "Scrapy!"]
     [:div {:class "note"} [:p "Scraping examlples 'til the break of day..."]]
      (for [kv rows
            example (slurp-example-strings (:url kv))]
        [:pre {:class "prettyprint lang-clj"} (cdata-to-string example)])])

(defn insert-row [item-id example]
  (db/insert! (env :database-url "postgres://localhost:5432/docs")
              :example {:item_id item-id :example example}))

(def ex-vector [[6, "example-1"] [6, "example-2"]])

(defn insert-all-examples [ex-vector]
  (class ex-vector))

(defn insert-all-examples2 [ex-vector]
  (db/insert-multi! (env :database-url "postgres://localhost:5432/docs")
                    :example
                    [:item_id :example]
                    ex-vector))

;;(insert-all-examples ex-vector)

(defn build-insert-vec [item_id example]
  [[6 "ex2"] [5 "ex6"]])

(defn insert-examples [rows]
  [:section {:id "scrape"}
     [:h2 "Scrapy!"]
   (let [total []
         examples (map #(slurp-example-strings (:url %) rows))
         vectors (map #(vector (:item_id r) %) examples)
         all (into total vectors)]
     (insert-all-examples all))])

(defn index [params]
  "Add sections to the Index page "
  (for [[table fk] params]
    (let [f (Integer/parseInt fk)
        rows (get-query table f)]
    (if (= :item table)
      (make-item-section rows table)
      (make-section rows table)))))

(defn scrape-page []
  "Add sections to the Scrape page "
  ;;(make-scrape-section (get-query :scrape))
  (insert-examples (get-query :scrape)))














(defn not-found []
  [:div
   [:h1 {:class "info-warning"} "Page Not Found"]
   [:p "that page doesn't exist."]
   (link-to {class "btn btn-primary"} "/" "Home")])

(def code3 (resolve (symbol "map")))
(defn print-code-out [code]
  (with-out-str (pprint/write code :dispatch pprint/code-dispatch)))

(defn print-code [code]
  (pprint/write code :dispatch pprint/code-dispatch))

(defn printy [code ]
  (pprint/with-pprint-dispatch
    pprint/code-dispatch   ;
                 (pprint/pprint code)))

(def code '(do (println "Hello") (println "Goodbye") (println "Hey, you left me out!")))
(print-code code)
