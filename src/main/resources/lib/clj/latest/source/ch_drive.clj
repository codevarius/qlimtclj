(ns lib.clj.latest.source.ch-drive
  (:require
    [clojure.walk :as cw]
    [clojure.data.csv :as csv]
    [clojure.java.io :as io]
    [clojure.data.csv :as csv]
    [clojure.string :as str]
    [next.jdbc :as jdbc])
  (:import (java.util.regex Pattern)))

(def db-opts {:dbtype             "clickhouse"
              :classname          "com.clickhouse.jdbc.ClickHouseDriver"
              :dbname             "default"
              :host               "192.168.0.111"
              :port               30070
              :user               "default"
              :password           "Qnf84nD02jxhay"
              :connection-timeout 300000
              })

(def ds (jdbc/get-datasource db-opts))

(def clean-train-data-batch-query
  "select distinct replaceRegexpAll(ixval, '\\s\\s+', ' ') as xval,
                   replaceRegexpAll(iyval, '\\s\\s+', ' ') as yval
   from (select trimBoth((replaceRegexpAll(l_partname, '[^A-Za-zА-Яа-я]|\\s\\s+|\\t+', ' '))) as ixval,
                concat(c_name, ' ', l_articlename)                                            as iyval
         from logs
         where b_date > now() - interval 1 day
         and length(ixval) > 0
         and length(iyval) > 0) limit %d offset %d")

(def clean-randomba-query
  "select distinct replaceRegexpAll(xval, '\\s\\s+', ' ') as xval,
                   replaceRegexpAll(yval, '\\s\\s+', ' ') as yval
   from (select trimBoth((replaceRegexpAll(xval, '[^A-Za-zА-Яа-я]|\\s\\s+|\\t+', ' '))) as xval,
                yval
         from words_from_name_dataset_2
         where length(xval) > 0
         and length(yval) > 0) limit 1 offset %d")

(def dataset-count-query
  "select count() as cnt from logs where b_date > now() - interval 1 day")

(defn execute-query
  ([query]
   (with-open [conn (jdbc/get-connection ds)]
     (let [rows (jdbc/execute! conn [query])]
       rows)))
  ([query arg1]
   (with-open [conn (jdbc/get-connection ds)]
     (let [q (format query arg1)
           rows (jdbc/execute! conn [q])]
       rows)))
  ([query arg1 arg2]
   (with-open [conn (jdbc/get-connection ds)]
     (let [q (format query arg1 arg2)
           rows (jdbc/execute! conn [q])]
       rows))))

(defn fetch-dataset-size
  ([]
   (second (first (first (execute-query dataset-count-query)))))
  ([key]
   (if (= key :limited) 150000 (throw (IllegalArgumentException. "wrong key passed")))))

(defn x-vocabularate [xs input-size]
  (let [dict (map #(first %) (csv/read-csv (slurp "x-dict.csv")))
        tokens (str/split xs #" ")]
    (map
      (fn [input]
        (if (some #{input} tokens)
          1
          0))
      (take input-size dict))))

(defn y-vocabularate [ys output-size]
  (let [dict (map #(first %) (csv/read-csv (slurp "y-dict.csv")))]
    (map
      (fn [dict-item]
        (if (re-find (Pattern/compile ys) dict-item)
          1
          0))
      (take output-size dict))))

(defn update-dict [dict-path values tokenaze]
  (let [dict (map #(first %) (csv/read-csv (slurp dict-path)))
        updated-dict (distinct (concat
                                 (vec dict)
                                 (vec (filter
                                        #(> (count %) 3)
                                        (let [dd (let [kfds (cw/keywordize-keys
                                                              (frequencies
                                                                (if tokenaze (flatten (map #(str/split % #"\s")
                                                                                           (map #(str/lower-case (second (first %))) values)))
                                                                             (rest (map #(second (second %)) values)))))]
                                                   (into (sorted-map-by (fn [key1 key2] (compare (key2 kfds) (key1 kfds)))) kfds))]
                                          (loop [d dd
                                                 cnt 0
                                                 out (list)]
                                            (if (>= cnt (* 0.8 (reduce + (vals dd))))
                                              out
                                              (recur (rest d)
                                                     (+ cnt (second (first d)))
                                                     (conj out (name (first (first d))))))))))))
        out (map #(list %) (sort updated-dict))
        ]
    (with-open [writer (io/writer (str dict-path))]
      (csv/write-csv writer out :separator \n))
    (count out))
  )

(defn get-train-data-batch [batch-size input-size output-size]
  (let [random-offset (Math/round (rand (- (fetch-dataset-size :limited) batch-size)))
        raw-ds (execute-query clean-train-data-batch-query batch-size random-offset)]
    (map (fn [item] {:x (vec (x-vocabularate (second (first item)) input-size))
                     :y (vec (y-vocabularate (second (second item)) output-size))
                     }) raw-ds)))

(defn fill-dicts [batch-size]
  (let [random-offset (Math/round (rand (- (fetch-dataset-size :limited) batch-size)))
        raw-ds (execute-query clean-train-data-batch-query batch-size random-offset)
        x-cnt (update-dict "x-dict.csv" raw-ds true)
        y-cnt (update-dict "y-dict.csv" raw-ds false)]
    (do (println "dict sizes are: x-dict=" x-cnt " y-dict=" y-cnt))))