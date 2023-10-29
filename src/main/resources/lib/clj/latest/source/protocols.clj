(ns lib.clj.latest.source.protocols)

(defprotocol Parameters
  (weights [this])
  (bias [this])
  (layer-name [this]))

(defprotocol ActivationProvider
  (activation-fn [this]))

(defprotocol Backprop
  (forward [this])
  (backward [this eta]))

(defprotocol Transfer
  (input [this])
  (output [this])
  (ones [this]))

(defprotocol Activation
  (activ [_ z a!])
  (prime [_ z!]))

(defprotocol NeuralNetwork
  (layers [this]))