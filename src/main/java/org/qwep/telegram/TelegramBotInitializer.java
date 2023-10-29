package org.qwep.telegram;

import org.qwep.core.enums.ActivationFunctions;
import org.qwep.core.enums.NetEngine;
import org.qwep.core.impl.NetWizard;
import org.qwep.core.impl.NetWizardUtils;
import org.qwep.core.impl.NetworkMeta;
import org.qwep.core.interfaces.INetManager;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

public class TelegramBotInitializer {
    private final TelegramBot bot;
    static NetEngine engine = NetEngine.CPU;
    private static final int batchSize = 1000;
    private static final int inputSize = 1099;
    private static final int outputSize = 11081;
    private static final int trainingEpochs = 100;
    private static final List<Integer> netArch = List.of(64, 128, 256, 128, 64, outputSize);
    private static final double learningRate = 0.0001;
    private final String xDictPath = "x-dict.csv";
    private static final String yDictPath = "y-dict.csv";

    private static final NetworkMeta netParams = new NetworkMeta(
            batchSize,
            inputSize,
            outputSize,
            trainingEpochs,
            NetWizardUtils.genHiddenLayerNames("layer", netArch.size()),
            netArch,
            ActivationFunctions.SIGMOID,
            engine,
            learningRate);

    private static final INetManager netWizard = new NetWizard(engine);

    public TelegramBotInitializer(TelegramBot bot) {
        this.bot = bot;
    }

    public void init() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    public static NetworkMeta getNetParams() {
        return netParams;
    }

    public static INetManager getNetWizard() {
        return netWizard;
    }

    public static String getyDictPath() {
        return yDictPath;
    }

    public static NetEngine getEngine() {
        return engine;
    }

    public static int getBatchSize() {
        return batchSize;
    }

    public static int getInputSize() {
        return inputSize;
    }

    public static int getOutputSize() {
        return outputSize;
    }

}
