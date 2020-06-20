(ns db
  (:use [clojure.java.jdbc :as d]
        [environ.core :refer [env]]))

(defn top-query [] (d/query (env :database-url "postgres://localhost:5432/docs")["select name, id as kingdom_id from kingdom ORDER BY kingdom.name ASC"]))

(defn kingdom-query [id] (d/query (env :database-url "postgres://localhost:5432/docs")["select kingdom.id as kingdom_id, kingdom.name as title, clan.name as name, clan.id as clan_id from kingdom, clan where clan.kingdom_id = kingdom.id AND kingdom_id = ?" id]))

(defn clan-query [id] (d/query (env :database-url "postgres://localhost:5432/docs")["select kingdom.id as kingdom_id, clan.id as clan_id, clan.name as title, family.name as name, family.id as family_id from kingdom, clan, family where family.clan_id = clan.id AND kingdom.id = clan.kingdom_id AND clan_id = ?" id]))

(defn family-query [id] (d/query (env :database-url "postgres://localhost:5432/docs")["select kingdom.id as kingdom_id, clan.id as clan_id, family.id as family_id, family.name as title, item.name as name, item.id as item_id from kingdom, clan, family, item where item.family_id = family.id and family.clan_id = clan.id AND kingdom.id = clan.kingdom_id AND family_id = ?" id]))

(defn item-query [id] (d/query (env :database-url "postgres://localhost:5432/docs")["select item.name as title, example from item, examples where item.id = examples.item_id and item.id = ?" id]))

(defn scrape-query [] (d/query (env :database-url "postgres://localhost:5432/docs") ["select id as item_id, url from item GROUP BY id, url"]))

(defn insert-all-examples [ex-vector]
  "Inserts a sequence of vector pairs"
  (d/insert-multi! (env :database-url "postgres://localhost:5432/docs")
                    :examples
                    [:item_id :example]
                    ex-vector))

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
