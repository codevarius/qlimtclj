(ns lib.cpu-kernel
  (:require
    [clojure.java.io :as io]
    [lib.clj.latest.source.dataset-reader :refer :all]
    [lib.clj.latest.source.activation :refer :all]
    [lib.clj.latest.source.fullycon :refer :all]
    [lib.clj.latest.source.network :refer :all]
    [lib.clj.latest.source.protocols :refer :all]
    [lib.clj.latest.source.winit :refer :all]
    [uncomplicate.commons.core
     :refer [with-release]]
    (uncomplicate.neanderthal
      [core :refer [ge col transfer!]]
      [native :refer [native-float]])))

(import 'clojure.lang.IFn)

(defn gen-net [afn names sizes]
  (loop [n names
         s sizes
         ret []]
    (if (< (count n) 1)
      ret
      (recur (rest n)
             (rest s)
             (conj ret (fully-connected (first s) afn (first n)))))))

(defn net-parameters [batch-size
                      input-size
                      output-size
                      train-iterations
                      layer-names
                      layer-sizes
                      act-fn
                      factory
                      lr]
  {
   :batch-size       batch-size
   :input-size       input-size
   :output-size      output-size
   :train-iterations train-iterations
   :layer-names      layer-names
   :layer-sizes      layer-sizes
   :act-fn           act-fn
   :factory          factory
   :lr               lr
   }
  )

(defn cpu-factory []
  native-float)

(defn convert2array [v]
  (loop [v v
         out []]
    (if (empty? v)
      out
      (recur (rest v)
             (conj out (first v))))))

(defn query-net [net-params x]
  (do
    (println "query net via CPU")
    (with-release [factory (cpu-factory)
                   cu-x (ge factory (:input-size net-params) 1)]
                  (let [answer (do (transfer! x cu-x)
                                   (col ((load! (inference-network
                                                  factory
                                                  (:input-size net-params)
                                                  (gen-net (:act-fn net-params)
                                                           (:layer-names net-params)
                                                           (:layer-sizes net-params)))) cu-x) 0))]
                    (convert2array answer)))))

(defn gym [net-params]
  (with-release [factory (cpu-factory)
                 d (read-dataset factory
                                 (:batch-size net-params)
                                 (:input-size net-params)
                                 (:output-size net-params)
                                 (:lr net-params))
                 cu-x-train (ge factory (:input-size net-params) (:batch-size net-params))
                 cu-y-train (ge factory (:output-size net-params) (:batch-size net-params))
                 inference ((if (.exists (io/file (str (first (:layer-names net-params)) "-weights.csv")))
                              (do (println "will load existing weights data") load!)
                              (do (println "will init weights data") init!))
                            (inference-network
                              factory
                              (:input-size net-params)
                              (gen-net ((:act-fn net-params))
                                       (:layer-names net-params)
                                       (:layer-sizes net-params))))
                 training (training-network inference cu-x-train)]
                (transfer! (:x-train d) cu-x-train)
                (transfer! (:y-train d) cu-y-train)
                (do (sgd training cu-y-train quadratic-cost! (:train-iterations net-params) (:learning-rates d)))))

(defn train-net [net-params]
  (do
    (println "will use CPU engine")
    (gym net-params)))