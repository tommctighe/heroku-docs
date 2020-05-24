(ns templates.views.content
  (:use [hiccup.form]
        [hiccup.element :only (link-to)]
        [clojure.pprint :as pp]))

(defn get-kingdom []
  [:div {:class "container no-scrollbar"}
   [:h1 "Clojure"]
   [:p "A more pleasant way to do anything Java can."]
   [:ul {:id "clojure" :class "hs full"}
    [:li {:class "item"}
     [:a {:href "#characters"} "Characters"][:a {:href "#keywords"} "Keywords"][:a {:href "#symbols"} "Symbols"]]
    [:li {:class "item"}
     [:a {:href "#characters"} "Characters"][:a {:href "#keywords"} "Keywords"][:a {:href "#symbols"} "Symbols"]]
    [:li {:class "item"}
     [:a {:href "#characters"} "Characters"][:a {:href "#keywords"} "Keywords"][:a {:href "#symbols"} "Symbols"]]
    [:li {:class "item"}
     [:a {:href "#characters"} "Characters"][:a {:href "#keywords"} "Keywords"][:a {:href "#symbols"} "Symbols"]]
    ]])

(defn create-section [col val]
  ;; use cond or case here!
  
  (let [result (case col
                 :clan  "clan select..."
                 :family "FAMILY select..."
                 :name "NAME select...")]
    result))

(defn index [params]
  (for [[column value] (seq params)]
    (create-section column  value)))

(defn not-found []
  [:div
   [:h1 {:class "info-warning"} "Page Not Found"]
   [:p "that page doesn't exist."]
   (link-to {class "btn btn-primary"} "/" "Home")])
