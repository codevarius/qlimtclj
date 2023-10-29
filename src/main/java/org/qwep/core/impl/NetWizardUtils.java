package org.qwep.core.impl;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import com.google.common.primitives.Ints;
import org.qwep.core.enums.NetEngine;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NetWizardUtils {

    public static List<String> genHiddenLayerNames(String namePattern, int count) {
        return IntStream.range(1, ++count).boxed().map(index -> namePattern + "-" + index).toList();
    }

    @Deprecated
    public static String encodeText2BinaryString(String input, NetEngine engine) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("lib.clj.old.source.mnist-reader"));
        IFn encode = Clojure.var("lib.clj.old.source.mnist-reader", "encode");
        return (String) encode.invoke(input);
    }

    @Deprecated
    public static String decodeBinaryString2Text(List<Double> input, NetEngine engine) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("lib.clj.old.source.mnist-reader"));
        IFn decode = Clojure.var("lib.clj.old.source.mnist-reader", "decode");
        return (String) decode.invoke(input.stream()
                .map(n -> String.valueOf(Math.round(n)))
                .collect(Collectors.joining("", "", "")));
    }

    public static int[] prepareQuestion(String input, int inputSize) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("lib.clj.latest.source.dataset-reader"));
        IFn prepareQuestion = Clojure.var("lib.clj.latest.source.dataset-reader", "prepare-question");
        return Ints.toArray((List<Long>) prepareQuestion.invoke(input, inputSize));
    }

    @Deprecated
    public static Map<String, String> fetchRandomBrandArticle(int batchSize) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("lib.clj.latest.source.dataset-reader"));
        IFn fetchRandomBa = Clojure.var("lib.clj.latest.source.dataset-reader", "fetch-randomba");
        return (Map<String, String>) fetchRandomBa.invoke(batchSize);
    }

    public static String decodeAnswer(int[] encodedAnswer, String dictPath) {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("lib.clj.latest.source.dataset-reader"));
        IFn decodeAnswer = Clojure.var("lib.clj.latest.source.dataset-reader", "decode-from-dict");
        return (String) decodeAnswer.invoke(encodedAnswer, dictPath);
    }
}
