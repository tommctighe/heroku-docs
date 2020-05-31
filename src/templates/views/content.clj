(ns templates.views.content
  (:use [hiccup.form]
        [hiccup.element :only (link-to)]
        [clojure.pprint :as pp]
        [clojure.java.jdbc :as db]
        [environ.core :refer [env]]
        [clojure.string :as s]
        [clojure.pprint :as pprint]))

(defn print-code [o]
  (binding [pprint/*print-right-margin* 100
            pprint/*print-miser-width* 60]
    (pprint/with-pprint-dispatch pprint/code-dispatch
      (pprint/pprint o))))

(print-code "(defn go [] Wow! (slug)")

(defn attr-str [s]
  (.toLowerCase (replace (replace s " " "-") "," "")))

(defn top-query [] (db/query (env :database-url "postgres://localhost:5432/docs")["select name, id as kingdom_id from kingdom ORDER BY kingdom.name ASC"]))

(defn kingdom-query [id] (db/query (env :database-url "postgres://localhost:5432/docs")["select kingdom.id as kingdom_id, kingdom.name as title, clan.name as name, clan.id as clan_id from kingdom, clan where clan.kingdom_id = kingdom.id AND kingdom_id = ?" id]))

(defn clan-query [id] (db/query (env :database-url "postgres://localhost:5432/docs")["select kingdom.id as kingdom_id, clan.id as clan_id, clan.name as title, family.name as name, family.id as family_id from kingdom, clan, family where family.clan_id = clan.id AND kingdom.id = clan.kingdom_id AND clan_id = ?" id]))

(defn family-query [id] (db/query (env :database-url "postgres://localhost:5432/docs")["select kingdom.id as kingdom_id, clan.id as clan_id, family.id as family_id, family.name as title, item.name as name, item.id as item_id from kingdom, clan, family, item where item.family_id = family.id and family.clan_id = clan.id AND kingdom.id = clan.kingdom_id AND family_id = ?" id]))

(defn item-query [id] (db/query (env :database-url "postgres://localhost:5432/docs")["select item.name as title from item where item.id = ?" id]))

(defn get-query
  ([] (top-query))
  ([name id]
   (name {
     :kingdom (kingdom-query id)
     :clan (clan-query id)
     :family (family-query id)
     :item (item-query id)})))

(defn get-title [rows] (get (first rows) :title "Clojure"))
(defn get-section-class [title](attr-str title))

(defn make-section [rows section-name]
  (let [title (get-title rows)
        s-class (get-section-class title)]
    [:section {:id section-name :class s-class}
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

(defn make-item-section [rows section-name]
  (let [title (get-title rows)
        s-class (get-section-class title)
        s-name (meta (resolve (symbol title)))]
    [:section {:id section-name :class s-class}
     [:h2 title]
     [:div {:class "note"} [:p "A little rap about this section..."]]     
     [:p {:class "docstring"} (str (:doc s-name))]
     [:p {:class "arglists"} (str (:arglists s-name))]
     [:p "Examples!"]] ))

(defn index [params]
  "Add sections to the Index page "
  (for [[table fk] params]
    (let [f (Integer/parseInt fk)
        rows (get-query table f)]
    (if (= :item table)
      (make-item-section rows table)
      (make-section rows table)))))

(defn not-found []
  [:div
   [:h1 {:class "info-warning"} "Page Not Found"]
   [:p "that page doesn't exist."]
   (link-to {class "btn btn-primary"} "/" "Home")])
