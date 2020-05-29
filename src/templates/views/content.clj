(ns templates.views.content
  (:use [hiccup.form]
        [hiccup.element :only (link-to)]
        [clojure.pprint :as pp]
        [clojure.java.jdbc :as db]
        [environ.core :refer [env]]
        [clojure.string :as s]))

(defn attr-str [s]
  (.toLowerCase (replace (replace s " " "-") "," "")))

(defn to-map [rows]
  (reduce #(assoc %1 (:title %2) (conj (%1 (:title %2))(hash-map(:id %2)(:name %2)))) {} rows))

;;; Lots of progress, that's cool!
;;; TO-DO: re-factor the section functions:
;;; (defn slider-section [section-name parent-id] ...
;;; Will if-let work for the second let, with :or {defaults}?


(defn top-section []
  (let [rows (db/query (env :database-url "postgres://localhost:5432/docs")
                                  ["select name, id from kingdom ORDER BY kingdom.name ASC"])]
    [:div {:id "clojure" :class "container no-scrollbar"}
     [:h1 "Clojure"]
     [:p "A more pleasant way to do anything Java can do."]
     [:ul {:class "hs full"}
      (for [kv rows]
        (let [k (:name kv)
              k-attr (attr-str k)
              id (:id kv)]          
          [:li {:class "item"}
           [:a {:href (str "/?kingdom=" id "#" k-attr)} k]]))]]))

(defn kingdom-section [k-id]
  (let [clans (db/query (env :database-url "postgres://localhost:5432/docs")
                                  ["select kingdom.name as title, clan.name as name, clan.id as id from kingdom, clan where clan.kingdom_id = kingdom.id AND kingdom_id = ?" k-id])
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

(defn item-section [fk]
  [:div
   [:h2 "yap..."]
   [:p {:class "item-heading"} "yup..."]
   [:p "Docstring"]
   [:p "Arglist"]])

(defn create-section [table fk]
  "Given a table and a foreign key, create a section"
  (let [f (Integer/parseInt fk)
        result (case table
                 :item (item-section f)
                 :family (family-section f)
                 :clan (clan-section f)
                 :kingdom (kingdom-section f))]
    result))

(defn index [params]
  (for [[table fk] params]
    (create-section table fk)))

(defn not-found []
  [:div
   [:h1 {:class "info-warning"} "Page Not Found"]
   [:p "that page doesn't exist."]
   (link-to {class "btn btn-primary"} "/" "Home")])
