(ns lib.clj.latest.source.fullycon
  (:require
    [clojure.data.csv :as csv]
    [clojure.java.io :as io]
    [lib.clj.latest.source.protocols :refer :all]
    [uncomplicate.commons.core
     :refer [Releaseable let-release release]]
    (uncomplicate.neanderthal
      [core :refer [axpby! dim ge mm! mrows mv! raw
                    rk! trans vctr view-ge view-vctr zero]]
      [native :refer [native-float]]
      [vect-math :refer [cosh! inv! mul! sqr! tanh!]])
    ))

(import 'clojure.lang.IFn)

(deftype FullyConnectedInference [w b activ-fn nam]
  Releaseable
  (release [_]
    (release w)
    (release b)
    (release activ-fn))
  Parameters
  (weights [_] w)
  (bias [_] b)
  (layer-name [_] nam)
  ActivationProvider
  (activation-fn [_] activ-fn)
  IFn
  (invoke [_ x ones a]
    (activ-fn (rk! -1.0 b ones (mm! 1.0 w x 0.0 a)))))

(defn fully-connected
  ([factory in-dim out-dim activ nam]
   (let-release [w (ge factory out-dim in-dim)
                 bias (vctr factory out-dim)]
                (->FullyConnectedInference w bias activ nam))
   )
  ([out-dim activ nam]
   (fn
     ([factory in-dim]
      (fully-connected factory in-dim out-dim activ nam))
     ([]
      out-dim))
   )
  )

(defmacro vctr2matrix [v]
  `(ge native-float (dim ~v) 1 ~v))

(deftype FullyConnectedTraining [v w b a-1 z a ones activ-fn first? nam]
  Releaseable
  (release [_]
    (release v)
    (release w)
    (release b)
    (release a-1)
    (release z)
    (release a)
    (release ones)
    (release activ-fn))
  Parameters
  (weights [_] w)
  (bias [_] b)
  (layer-name [_] nam)
  Transfer
  (input [_] a-1)
  (output [_] a)
  (ones [_] ones)
  Backprop
  (forward [_]
    (activ activ-fn (rk! -1.0 b ones (mm! 1.0 w a-1 0.0 z)) a))
  (backward [_ [eta lambda mu]]
    (let [eta-avg (- (/ (double eta) (dim ones)))]
      (mul! (prime activ-fn z) a)
      (mm! eta-avg z (trans a-1) mu v)
      (when-not first? (mm! 1.0 (trans w) z 0.0 a-1))
      (mv! eta-avg z ones 1.0 b)
      (with-open [writer (io/writer (str nam "-bias.csv"))] (csv/write-csv writer (vctr2matrix b)))
      (axpby! 1.0 v (inc (* eta-avg (double lambda))) w)
      (with-open [writer (io/writer (str nam "-weights.csv"))] (csv/write-csv writer (ge native-float w)))
      )))

(defn training-layer
  ([inference-layer input ones-vctr first?]
   (let-release [w (view-ge (weights inference-layer))
                 v (zero w)                                 ;; Has to be initialized to zero
                 b (view-vctr (bias inference-layer))
                 a-1 (view-ge input)
                 z (ge w (mrows w) (dim ones-vctr))
                 a (raw z)
                 o (view-vctr ones-vctr)]
                (->FullyConnectedTraining v w b a-1 z a o ((activation-fn inference-layer) z) first? (layer-name inference-layer))))
  ([inference-layer input ones-vctr]
   (training-layer inference-layer input ones-vctr true))
  ([inference-layer previous-backprop]
   (training-layer inference-layer
                   (output previous-backprop)
                   (ones previous-backprop)
                   false)))