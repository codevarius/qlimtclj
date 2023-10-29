(ns lib.clj.latest.source.activation
  (:require
    [lib.clj.latest.source.protocols :refer :all]
    [lib.clj.latest.source.protocols :refer :all]
    [uncomplicate.commons.core
     :refer [Releaseable let-release release]]
    (uncomplicate.neanderthal
      [core :refer [copy! entry! raw scal!]]
      [vect-math :refer [cosh! inv! linear-frac! mul! sqr! tanh!]])))

(deftype SigmoidActivation [work]
  Releaseable
  (release [_]
    (release work))
  Activation
  (activ [_ z a!]
    (linear-frac! 0.5 (tanh! (scal! 0.5 (copy! z a!))) 0.5))
  (prime [this z!]
    (linear-frac! 0.5 (tanh! (scal! 0.5 z!)) 0.5)
    (mul! z! (linear-frac! -1.0 z! 1.0 work))))

(deftype TanhActivation []
  Activation
  (activ [_ z a!]
    (tanh! z a!))
  (prime [this z!]
    (sqr! (inv! (cosh! z!)))))

(deftype LinearActivation []
  Activation
  (activ [_ z a!]
    (copy! z a!))
  (prime [this z!]
    (entry! z! 1)))

(defn sigmoid
  ([]
   (fn [z]
     (let-release [work (raw z)]
                  (->SigmoidActivation work))))
  ([z!]
   (linear-frac! 0.5 (tanh! (scal! 0.5 z!)) 0.5)))

(defn tanh
  ([]
   (fn [_] (->TanhActivation)))
  ([z!]
   (tanh! z!)))

(defn linear
  ([]
   (fn [_] (->LinearActivation)))
  ([z!]
   z!))
