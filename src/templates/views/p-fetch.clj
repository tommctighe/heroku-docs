(ns parallel-fetch)

(defn get-urls [rows]
  (let [agents (doall (map #(agent %) rows))]
    (doseq [agent agents] (send-off agent scrape-example-strings))
    (apply await agents)
    (doall (map #(deref %) agents))))

(defn scrape-example-strings [{:keys [item_id url]}]
  (let [html (slurp url)
        example-strings (map second (re-seq #":body\s\\\"(.*?)\\\"," html))]
    (map #(vector item_id %) example-strings)))

(prn (apply concat (get-urls [{:item_id 1, :url "https://clojuredocs.org/clojure.core/swap!"} {:item_id 2, :url "https://clojuredocs.org/clojure.core/do"}])))
