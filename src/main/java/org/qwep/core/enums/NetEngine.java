package org.qwep.core.enums;

public enum NetEngine {
    CUDA("cuda"),
    CPU("cpu");

    private final String engine;

    NetEngine(String engine) {
        this.engine = engine;
    }

    public String get() {
        return this.engine;
    }


}
