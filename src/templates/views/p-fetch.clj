(ns parallel-fetch
  (:import (java.io InputStream InputStreamReader BufferedReader)
           (java.net URL HttpURLConnection)))

(defn get-url [url]
  (let [conn (.openConnection (URL. url))]
    (.setRequestMethod conn "GET")
    (.connect conn)
    (with-open [stream (BufferedReader. 
                       (InputStreamReader. (.getInputStream conn)))]
      (.toString (reduce #(.append %1 %2) 
                          (StringBuffer.) (line-seq stream))))))

(defn get-urls [urls]
  (let [agents (doall (map #(agent %) urls))]
    (doseq [agent agents] (send-off agent get-url))
    (apply await-for 5000 agents)
    (doall (map #(deref %) agents))))

(prn (get-urls '("http://lethain.com" "http://willarson.com")))
