package org.qwep.core.impl;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.PersistentHashMap;
import com.google.common.primitives.Doubles;
import org.qwep.core.enums.NetEngine;
import org.qwep.core.interfaces.INetManager;

import java.util.List;

public class NetWizard implements INetManager {

    private final String kernelName;

    public NetWizard(NetEngine engine) {
        this.kernelName = engine.equals(NetEngine.CUDA) ? "lib.cuda-kernel" : "lib.cpu-kernel";
    }

    @Override
    public double trainNet(PersistentHashMap netParams) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read(kernelName));
        IFn trainNet = Clojure.var(kernelName, "train-net");
        return (double) trainNet.invoke(netParams);
    }

    @Override
    public double[] queryNet(PersistentHashMap netMeta, int[] x) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read(kernelName));
        IFn queryNet = Clojure.var(kernelName, "query-net");
        return Doubles.toArray(((List<Double>) queryNet.invoke(netMeta, x)));
    }

    @Override
    public Object fillDictionary(int batchSize) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("lib.clj.latest.source.ch-drive"));
        IFn fillDicts = Clojure.var("lib.clj.latest.source.ch-drive", "fill-dicts");
        return fillDicts.invoke(batchSize);
    }

}
