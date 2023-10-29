package org.qwep;

import org.qwep.core.impl.NetWizard;
import org.qwep.core.interfaces.INetManager;
import org.qwep.telegram.TelegramBot;
import org.qwep.telegram.TelegramBotInitializer;

import static org.qwep.telegram.TelegramBotInitializer.*;

public class Main {
    public static void main(String[] args) {

        TelegramBot bot = new TelegramBot();
        TelegramBotInitializer botInitializer = new TelegramBotInitializer(bot);
        botInitializer.init();
        System.out.println("bot initialized");

        INetManager netWizard = new NetWizard(getEngine());
        int i = 0;

        while (true) {
            try {
                Thread.sleep(100);
                System.out.println("push new batch №" + i++);
//                netWizard.fillDictionary(getBatchSize());
                netWizard.trainNet(TelegramBotInitializer.getNetParams().get());
            } catch (Exception e) {
                System.out.println("error:" + e.getLocalizedMessage());
            }
        }
    }
}