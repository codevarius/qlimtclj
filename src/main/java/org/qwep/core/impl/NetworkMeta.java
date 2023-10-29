package org.qwep.core.impl;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import clojure.lang.PersistentHashMap;
import org.qwep.core.enums.ActivationFunctions;
import org.qwep.core.enums.NetEngine;
import org.qwep.core.interfaces.INetworkMeta;

import java.util.List;

public class NetworkMeta implements INetworkMeta {

    private final int batchSize;
    private final int inputSize;
    private final int outputSize;
    private final int trainingEpochs;
    private final List<String> hiddenLayersNames;
    private final List<Integer> hiddenLayersSizes;
    private final IFn actFn;
    private final String engine;
    private final double lr;
    private final String kernelName;

    public NetworkMeta(
            int batchSize,
            int inputSize,
            int outputSize,
            int trainingEpochs,
            List<String> hiddenLayersNames,
            List<Integer> hiddenLayersSizes,
            ActivationFunctions actFn,
            NetEngine engine,
            double lr) {
        this.batchSize = batchSize;
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.trainingEpochs = trainingEpochs;
        this.hiddenLayersNames = hiddenLayersNames;
        this.hiddenLayersSizes = hiddenLayersSizes;
        this.actFn = actFn.get();
        this.engine = engine.get();
        this.lr = lr;
        this.kernelName = engine.equals(NetEngine.CUDA) ? "lib.cuda-kernel" : "lib.cpu-kernel";
    }

    @Override
    public PersistentHashMap get() {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read(kernelName));
        IFn netParameters = Clojure.var(kernelName, "net-parameters");
        return (PersistentHashMap) netParameters.invoke(
                batchSize,
                inputSize,
                outputSize,
                trainingEpochs,
                hiddenLayersNames,
                hiddenLayersSizes,
                actFn,
                engine,
                lr);
    }

}
