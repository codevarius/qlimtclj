package org.qwep.core.enums;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

public enum ActivationFunctions {

    SIGMOID(Clojure.var("lib.clj.latest.source.activation", "sigmoid")),
    TANH(Clojure.var("lib.clj.latest.source.activation", "tanh")),
    LINEAR(Clojure.var("lib.clj.latest.source.activation", "linear"));

    private final IFn func;

    ActivationFunctions(IFn func) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("lib.clj.latest.source.activation"));
        this.func = func;
    }

    public IFn get() {
        return this.func;
    }
}
