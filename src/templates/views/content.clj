(ns templates.views.content
  (:use [hiccup.form]
        [hiccup.element :only (link-to)]
        [clojure.string :as s]
        [clojure-getting-started.db]))

;; Utilities
(defn attr-str [s]
  (.toLowerCase (replace (replace s " " "-") "," "")))
(defn get-title [rows] (get (first rows) :title "Clojure"))
(defn get-section-id [title](attr-str title))
(defn cdata-to-string [str]
  (replace str #"\\\\n" "\n"))

(defn make-section [rows class-name]
  "Create <section>, format links to child sections"
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
  "Create <section> that explains a function"
  (let [title (get-title rows)
        s-id (get-section-id title)
        s-name (meta (resolve (symbol title)))]
    [:section {:id s-id :class class-name}
     [:h2 title]
     [:div {:class "note"} [:p "A little rap about this section..."]]     
     [:p {:class "docstring"} (str (:doc s-name))]
     [:p {:class "arglists"} (str (:arglists s-name))]
     (for [r rows]
       [:pre {:class "prettyprint lang-clj"} (cdata-to-string (:example r))])
     ]))

(defn index [params]
  "Add sections to the Index page, given table name & foreign key"
  (for [[table fk] params]
    (let [f (Integer/parseInt fk)
        rows (db/get-query table f)]
      (case table
        :item (make-item-section rows table)
        :clan (if (= 1 (count rows)) ;; Skip clan section if it has only one family
                          (if (not (contains? params :family))
                            (make-section ;; Display the family section instead
                             (db/get-query :family (:family_id (first rows))) :family))
                  (make-section rows table))
        (make-section rows table))))) ;; default for :kingdom & :family tables
