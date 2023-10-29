package org.qwep.core.interfaces;

import clojure.lang.PersistentHashMap;
import org.qwep.core.enums.ActivationFunctions;

import java.util.List;

public interface INetManager {

    double trainNet(PersistentHashMap netMeta);

    double[] queryNet(PersistentHashMap netMeta, int[] x);

    Object fillDictionary(int batchSize);
}
