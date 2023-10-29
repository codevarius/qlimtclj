(ns lib.clj.latest.source.network
  (:require
    [lib.clj.latest.source.fullycon :refer :all]
    [lib.clj.latest.source.protocols :refer :all]
    [uncomplicate.commons.core
     :refer [Releaseable let-release release with-release]]
    (uncomplicate.neanderthal
      [core :refer [axpy! copy! dim
                    entry! ge mrows ncols nrm2
                    raw row vctr view-ge]]
      [math :refer [sqr]])))

(import 'clojure.lang.IFn)

(deftype NeuralNetworkInference [layers
                                 ^long max-width-1
                                 ^long max-width-2]
  Releaseable
  (release [_]
    (doseq [l layers] (release l)))
  NeuralNetwork
  (layers [_]
    layers)
  IFn
  (invoke [_ x ones-vctr temp-1! temp-2!]
    (let [batch (dim ones-vctr)]
      (loop [x x v1 temp-1! v2 temp-2! layers layers]
        (if layers
          (recur (let [layer (first layers)]
                   (layer x ones-vctr
                          (view-ge v1 (mrows (weights layer)) batch)))
                 v2 v1 (next layers))
          x))))
  (invoke [this x a!]
    (let [cnt (count layers)]
      (if (= 0 cnt)
        (copy! x a!)
        (with-release [ones-vctr (entry! (vctr x (ncols x)) 1.0)]
                      (if (= 1 cnt)
                        ((layers 0) x ones-vctr a!)
                        (with-release [temp-1 (vctr x (* max-width-1 (dim ones-vctr)))]
                                      (if (= 2 cnt)
                                        (this x ones-vctr temp-1 a!)
                                        (with-release [temp-2 (vctr x (* max-width-2 (dim ones-vctr)))]
                                                      (copy! (this x ones-vctr temp-1 temp-2) a!)))))))))
  (invoke [this x]
    (let-release [a (ge x (mrows (weights (peek layers))) (ncols x))]
                 (this x a))))

(defn inference-network [factory in-dim layers]
  (let [out-sizes (map #(%) layers)
        in-sizes (cons in-dim out-sizes)
        max-width-1 (apply max (take-nth 2 out-sizes))
        max-width-2 (apply max (take-nth 2 (rest out-sizes)))]
    (let-release [layers (vec (map (fn [layer-fn in-size]
                                     (layer-fn factory in-size))
                                   layers
                                   in-sizes))]
                 (->NeuralNetworkInference layers max-width-1 max-width-2))))

(deftype NeuralNetworkTraining [forward-layers backward-layers]
  Releaseable
  (release [_]
    (doseq [l forward-layers] (release l)))
  NeuralNetwork
  (layers [_]
    forward-layers)
  Transfer
  (input [_] (input (first forward-layers)))
  (output [_] (output (first backward-layers)))
  (ones [_] (ones (first backward-layers)))
  Backprop
  (forward [_]
    (doseq [layer forward-layers]
      (forward layer))
    (output (first backward-layers)))
  (backward [_ eta]
    (doseq [layer backward-layers]
      (backward layer eta))))

(defn training-network [inference input]
  (let-release [ones-vctr (entry! (raw (row input 0)) 1)]
               (let-release [backward-layers
                             (reduce (fn [bwd-layers layer]
                                       (cons (training-layer layer (first bwd-layers))
                                             bwd-layers))
                                     (list (training-layer (first (layers inference))
                                                           input ones-vctr))
                                     (rest (layers inference)))]
                            (->NeuralNetworkTraining (reverse backward-layers) backward-layers))))

(defn sgd
  ([network out cost! epochs eta]
   (dotimes [n epochs]
     (time (do
             (print (str "training epoch №" n " | "))
             (forward network)
             (cost! out (output network))
             (print (str "net error: " (cost! (output network)) " | "))
             (backward network eta))))
   (cost! (output network)))
  ([network out cost! options]
   (map (fn [[epochs eta]] (sgd network out cost! epochs eta)) options)))

(defn quadratic-cost!
  ([y-a]
   (/ (sqr (nrm2 y-a)) (* 2 (dim y-a))))
  ([y a!]
   (axpy! -1.0 y a!)))
