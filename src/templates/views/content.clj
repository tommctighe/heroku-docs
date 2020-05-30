(ns templates.views.content
  (:use [hiccup.form]
        [hiccup.element :only (link-to)]
        [clojure.pprint :as pp]
        [clojure.java.jdbc :as db]
        [environ.core :refer [env]]
        [clojure.string :as s]))

(defn attr-str [s]
  (.toLowerCase (replace (replace s " " "-") "," "")))

;;; Lots of progress, that's cool!
;;; TO-DO: re-factor the section functions:
;;; (defn make-section [section-name parent-id] ...
;;; Will if-let work for the second let, with :or {defaults}?

(defn top-query [] (db/query (env :database-url "postgres://localhost:5432/docs")["select name, id as kingdom_id from kingdom ORDER BY kingdom.name ASC"]))

(defn kingdom-query [id] (db/query (env :database-url "postgres://localhost:5432/docs")["select kingdom.id as kingdom_id, kingdom.name as title, clan.name as name, clan.id as clan_id from kingdom, clan where clan.kingdom_id = kingdom.id AND kingdom_id = ?" id]))

(defn clan-query [id] (db/query (env :database-url "postgres://localhost:5432/docs")["select kingdom.id as kingdom_id, clan.id as clan_id, clan.name as title, family.name as name, family.id as family_id from kingdom, clan, family where family.clan_id = clan.id AND kingdom.id = clan.kingdom_id AND clan_id = ?" id]))

(defn family-query [id] (db/query (env :database-url "postgres://localhost:5432/docs")["select kingdom.id as kingdom_id, clan.id as clan_id, family.id as family_id, family.name as title, item.name as name, item.id as item_id from kingdom, clan, family, item where item.family_id = family.id and family.clan_id = clan.id AND kingdom.id = clan.kingdom_id AND family_id = ?" id]))

(defn item-query [id] (db/query (env :database-url "postgres://localhost:5432/docs")["select kingdom.id as kingdom_id, clan.id as clan_id, item.name as title, family.id as family_id from kingdom, clan, family, item where family.clan_id = clan.id AND kingdom.id = clan.kingdom_id AND item.id = ?" id]))

(defn get-query
  ([] (top-query))
  ([name id]
   (name {
     :kingdom (kingdom-query id)
     :clan (clan-query id)
     :family (family-query id)
     :item (item-query id)})))

(defn make-section [rows]
  (let [title (get (first rows) :title "Clojure")
        s-id (attr-str title)]
    [:section {:id s-id}
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

(defn make-item-section [rows]
  (let [title (:title (first rows))
        s-id (attr-str title)]
    [:div {:id s-id}
     [:h2 title]
     [:p "A little rap about this section..."]
     [:p "Docstring"]
     [:p "Arglist"]
     [:p "Examples!"]]))

(defn which-section [table fk]
  "Given a table and a foreign key, create a section"
  (let [f (Integer/parseInt fk)]
    (if (= :item table)
      (make-item-section (get-query table f))
      (make-section (get-query table f)))))

(defn index [params]
  (for [[table fk] params]
    (which-section table fk)))

(defn not-found []
  [:div
   [:h1 {:class "info-warning"} "Page Not Found"]
   [:p "that page doesn't exist."]
   (link-to {class "btn btn-primary"} "/" "Home")])



;;; Graveyard
;;; 
;;; 
;;; RIP!
(defn kingdom-section [k-id]
  (let [clans (db/query (env :database-url "postgres://localhost:5432/docs")["select kingdom.name as title, clan.name as name, clan.id as id from kingdom, clan where clan.kingdom_id = kingdom.id AND kingdom_id = ?" k-id])
        title (:title (first clans))
        t-attr (attr-str title)]
    [:div {:id title :class "container no-scrollbar"}
     [:h2 title]
     [:p "A little rap about this section..."]
     [:ul {:class "hs full"}
      (for [kv clans]
        (let [id (:id kv)
              c (:name kv)
              c-attr (attr-str c)]
          [:li {:class "item"} [:a {:href (str "/?kingdom=" k-id "&clan=" id "#" c-attr)} c]]))]]))

(defn clan-section [c-id]
  (let [families (db/query (env :database-url "postgres://localhost:5432/docs")
                        ["select kingdom.id as kingdom_id, clan.name as title, family.name as name, family.id as id from kingdom, clan, family where family.clan_id = clan.id AND kingdom.id = clan.kingdom_id AND clan_id = ?" c-id])
        title (:title (first families))
        t-attr (attr-str title)]
    [:div {:id t-attr :class "container no-scrollbar"}
     [:h2 title]
     [:p "A little rap about this section..."]
     [:ul {:class "hs full"}
      (for [kv families]
          (let [id (:id kv)
                f (:name kv)
                k (:kingdom_id kv)
                f-attr (attr-str f)]
            [:li {:class "item"} [:a {:href (str "/?kingdom=" k  "&clan=" c-id "&family=" id "#" f-attr)} f]]))]]))

(defn family-section [fk]
  [:div
   [:h2 "yap..."]
   [:p {:class "item-heading"} "yup..."]
   [:p "Docstring"]
   [:p "Arglist"]])

(defn top-section []
  (let [rows (get-query)]
    [:div {:id "clojure" :class ""}
     [:h1 "Clojure"]
     [:p "A more pleasant way to do anything Java can do."]
     [:div {:class "button-box"}
      (for [kv rows]
        (let [name (:name kv)
              anchor (attr-str name)
              id (:id kv)
              href-str  (str "/?kingdom=" id "#" anchor)]
          [:a {:href href-str}
           [:div {:class "button in-list"} name]]))]]))

(defn to-map [rows]
  (reduce #(assoc %1 (:title %2) (conj (%1 (:title %2))(hash-map(:id %2)(:name %2)))) {} rows))
