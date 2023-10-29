(ns lib.clj.old.mnist-reader
  (:require
    [lib.clj.latest.source.ch-drive :as chdrive]
    [clojure.data.csv :as csv]
    [clojure.pprint :as ppr]
    [clojure.string :as st]
    (uncomplicate.neanderthal
      [core :refer [ge]]
      [native :refer [native-float]])))

(defn parse-dataset [path]
  (csv/read-csv (slurp path))
  )

(defn unspace [s]
  (st/trim (st/replace s " " "")))

(defn decode [bitstring]
  (->> (partition 8 (unspace bitstring))
       (map #(char (Integer/parseInt (apply str %) 2)))
       (apply str)
       (st/trim)))

(defn encode [s]
  (st/trim (ppr/cl-format nil "~{~8,'0b ~}" (map #(int %) s))))

(defmacro str2list [expr]
  `(map #(Integer/parseInt (str %)) ~expr))

(defn prepare-question [input]
  (vec (str2list (unspace (encode input)))))

(defn strdig [sd]
  (case sd
    "0" [1 0 0 0 0 0 0 0 0 0]
    "1" [0 1 0 0 0 0 0 0 0 0]
    "2" [0 0 1 0 0 0 0 0 0 0]
    "3" [0 0 0 1 0 0 0 0 0 0]
    "4" [0 0 0 0 1 0 0 0 0 0]
    "5" [0 0 0 0 0 1 0 0 0 0]
    "6" [0 0 0 0 0 0 1 0 0 0]
    "7" [0 0 0 0 0 0 0 1 0 0]
    "8" [0 0 0 0 0 0 0 0 1 0]
    "9" [0 0 0 0 0 0 0 0 0 1]))

(defn take-mnist-example [n]
  (let [pd (nth (map (fn [r] {:x (map #(/ (Double/parseDouble %) 255) (rest r))
                              :y (first r)
                              }) (rest (parse-dataset "mnist_test.csv"))) n)]
    (do (println "got " (:y pd))
        (vec (:x pd)))))

(defn csv2map [csv]
  (shuffle (map (fn [r] {:x (map #(Double/parseDouble %) (rest r))
                         :y (map #(double %) (strdig (first r)))
                         }) (rest csv))))

(defn read-dataset [path dataset-size input-size output-size lr]
  (let [ds (csv2map (parse-dataset path))]
    {
     :x-train        (ge native-float input-size dataset-size (flatten (map #(:x %) ds)))
     :y-train        (ge native-float output-size dataset-size (flatten (map #(:y %) ds)))
     :learning-rates (vec (repeat output-size lr))
     }))
