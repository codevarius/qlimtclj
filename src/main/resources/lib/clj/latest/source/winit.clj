(ns lib.clj.latest.source.winit
  (:require
    [clojure.string :as str]
    [lib.clj.latest.source.protocols :refer :all]
    [uncomplicate.commons.core
     :refer [with-release]]
    [uncomplicate.fluokitten.core :refer [fmap!]]
    (uncomplicate.neanderthal
      [core :refer [dim
                    ge mrows ncols scal! transfer! vctr]]
      [math :refer [sqrt log pi sin sqrt]]
      [vect-math :refer [cosh! inv! mul! sqr! tanh!]])
    [uncomplicate.neanderthal.internal.api :refer [device native-factory]]
    [uncomplicate.neanderthal.math]))

(defn rand-uniform ^double [^double _]
  (double (rand)))

(defn rand-gaussian ^double [^double _]
  (let [u1 (rand-uniform Double/NaN)
        u2 (rand-uniform Double/NaN)]
    (double (* (sqrt (* -2.0 (log u1))) (sin (* 2.0 pi u2))))))

(defn init-weights! [w b]
  (fmap! rand-gaussian w)                                   ;(scal! (/ 1.0 (ncols w)) (fmap! rand-gaussian w))
  (fmap! rand-gaussian b))
(defn load-weights! [layer-name w b]
  (transfer! (map #(Double/parseDouble %) (str/split (slurp (str layer-name "-weights.csv")) #",|\n")) w)
  (transfer! (map #(Double/parseDouble %) (str/split (slurp (str layer-name "-bias.csv")) #",|\n")) b))

(defn init-layer! [layer!]
  (let [w (weights layer!)
        b (bias layer!)]
    (if (= :cpu (device w))
      (init-weights! w b)
      (with-release [native-w (ge (native-factory w) (mrows w) (ncols w))
                     native-b (vctr (native-factory b) (dim b))]
                    (init-weights! native-w native-b)
                    (transfer! native-w w)
                    (transfer! native-b b)))
    layer!))

(defn load-layer! [layer!]
  (let [w (weights layer!)
        b (bias layer!)]
    (if (= :cpu (device w))
      (load-weights! (layer-name layer!) w b)
      (with-release [native-w (ge (native-factory w) (mrows w) (ncols w))
                     native-b (vctr (native-factory b) (dim b))]
                    (load-weights! (layer-name layer!) native-w native-b)
                    (transfer! native-w w)
                    (transfer! native-b b)))
    layer!))

(defn init! [network!]
  (doseq [layer (layers network!)]
    (init-layer! layer))
  network!)

(defn load! [network!]
  (doseq [layer (layers network!)]
    (do
      (load-layer! layer)))
  network!)