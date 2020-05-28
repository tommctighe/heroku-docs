(ns templates.views.content
  (:use [hiccup.form]
        [hiccup.element :only (link-to)]
        [clojure.pprint :as pp]
        [clojure.java.jdbc :as db]
        [environ.core :refer [env]]
        [clojure.string :as s]))

(defn attr-str [s]
  (.toLowerCase (replace s " " "-")))

(defn kingdom-section []
  (let [rows (db/query (env :database-url "postgres://localhost:5432/docs")
                                  ["select distinct kingdom from core order by kingdom asc"])]
    [:div {:id "clojure" :class "container no-scrollbar"}
     [:h1 "Clojure"]
     [:p "A more pleasant way to do anything Java can do."]
     [:ul {:class "hs full"}
      (for [kv rows]
        (let [kingdoms (s/split (:kingdom kv) #", ")]          
          [:li {:class "item"}
           (for [k kingdoms]
             (let [k-attr (attr-str k)]
                 [:a {:href (str "/?clan=" k "#" k-attr)} k])
             )
           ]
          ))]]))

(defn clan-section [c-name]
  (let [families (db/query (env :database-url "postgres://localhost:5432/docs")
                                  ["select distinct family from core where clan = ?" c-name])
        c-attr (attr-str c-name)]
    [:div {:id c-attr :class "container no-scrollbar"}
     [:h2 c-name]
     [:p "A little rap about this section..."]
     [:ul {:class "hs full"}
      (for [kv families]
        (let [f (:family kv)
              f-attr (attr-str f)]
          [:li {:class "item"} [:a {:href (str "/?clan=" c-name "&family=" f "#" f-attr)} f]]))]]))

(defn family-section [f-name]
  (let [names (db/query (env :database-url "postgres://localhost:5432/docs")
                                  ["select distinct name from core where family = ?" f-name])
        f-attr (attr-str f-name)]
    [:div {:id f-attr :class "container no-scrollbar"}
     [:h2 f-name]
     [:ul
      (for [n names];need session for c-name? some family names in multiple clans!
          [:li {:class "item"} [:a {:href (str "/?clan=" c-name "&family=" f "#" f-attr)} f]])]]))

(defn name-section [name]
  [:div
   [:h2 name]
   [:p {:class "name-heading"} name]
   [:p "Docstring"]
   [:p "Arglist"]])

(defn create-section [col val]
  (let [result (case col
                 :clan (clan-section val)
                 :family (family-section val)
                 :name (name-section val))]
    result))

(defn index [params]
  (for [[column val] (sort (seq params))]
    (create-section column val)))

(defn not-found []
  [:div
   [:h1 {:class "info-warning"} "Page Not Found"]
   [:p "that page doesn't exist."]
   (link-to {class "btn btn-primary"} "/" "Home")])
