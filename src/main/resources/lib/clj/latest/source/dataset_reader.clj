(ns lib.clj.latest.source.dataset-reader
  (:require
    [clojure.data.csv :as csv]
    [clojure.string :as str]
    [lib.clj.latest.source.ch-drive :as chdrive]
    (uncomplicate.neanderthal
      [core :refer [ge]]
      [native :refer [native-float]])))

(defn prepare-dataset [batch-size input-size output-size]
  (chdrive/get-train-data-batch batch-size input-size output-size))

(defn prepare-question [xs input-size]
  (vec (chdrive/x-vocabularate (str/lower-case xs) input-size)))

(defn fetch-randomba [batch-size]
  (let [rs (chdrive/execute-query
             chdrive/clean-randomba-query
             (Math/round (rand (- (chdrive/fetch-dataset-size) batch-size))))]
    {:x (second (first (first rs)))
     :y (second (second (first rs)))}))

(defn read-dataset [factory batch-size input-size output-size lr]
  (let [ds (prepare-dataset batch-size input-size output-size)]
    {
     :x-train        (ge factory input-size batch-size (flatten (map #(:x %) ds)))
     :y-train        (ge factory output-size batch-size (flatten (map #(:y %) ds)))
     :learning-rates (vec (repeat output-size lr))
     }))

(defn decode-from-dict [encoded, dict-path]
  (let [dict (map #(first %) (csv/read-csv (slurp dict-path)))]
    (str/join "\n" (filter #(not (= "" %)) (mapv (fn [a b] (if (= a 1) b "")) encoded dict)))))
