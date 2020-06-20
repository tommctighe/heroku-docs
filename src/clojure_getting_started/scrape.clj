(ns scrape
  (:require
   [db]))

;; Don't want to insert duplicates -- could drop the examples table first, but not safe if INSERT then fails
;; Catch exceptions, agents can (await) forever
;; What if there are no examples on a page?
;; Add tests
;; Make the scrape page display pretty

(defn scrape-example-strings [{:keys [item_id url]}]
  (let [html (slurp url)
        example-strings (map second (re-seq #":body\s\\\"(.*?)\\\"," html))]
    (map #(vector item_id %) example-strings)))

(defn dispatch-agents [rows]
  "Send off agents to scrape examples"
  (let [agents (doall (map #(agent %) rows))]
    (doseq [agent agents] (send-off agent scrape-example-strings))
    (apply await agents)
    (doall (map #(deref %) agents))))

(defn get-examples [rows] 
  "For each URL, scrape its examples, package for multi-insert"
  (db/insert-all-examples (apply concat ;; because we create a seq of seqs
                              (dispatch-agents rows)))
  [:p "done!"])

(defn scrape-page []
  "Populate DB with scraped examples"
  (get-examples (db/get-query :scrape)))
