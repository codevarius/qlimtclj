package org.qwep.telegram;


import com.google.common.collect.Lists;
import org.qwep.core.impl.NetWizardUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.stream.DoubleStream;

import static org.qwep.core.impl.NetWizardUtils.decodeAnswer;
import static org.qwep.telegram.TelegramBotInitializer.*;

public class TelegramBot extends TelegramLongPollingBot {

    private final String botUsername = "qwep-price-miner";
    private final String botToken = "";

    private void startBot(long chatId, String userName) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("hello " + userName);

        try {
            execute(message);
            System.out.println("bot reply sent");
        } catch (TelegramApiException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.getMessage().getText().equals("/start")) {
            startBot(update.getMessage().getChatId(), update.getMessage().getFrom().getFirstName());
        } else {
            Optional<Message> tgMessage = Optional.ofNullable(update.getMessage());
            String messageText;
            if (tgMessage.isEmpty()) return;
            if (tgMessage.get().getText() != null) {
                messageText = tgMessage.get().getText();
            } else if (tgMessage.get().getCaption() != null) {
                messageText = tgMessage.get().getCaption();
                update.getMessage().setText(messageText);
            } else {
                messageText = "";
            }
            if (messageText.isEmpty()) return;
            TelegramCommand telegramCommand = new TelegramCommand(messageText);
            if (Objects.requireNonNull(telegramCommand.getType()) == TelegramCommandType.KEY) {
                String code = telegramCommand.getArgument();
                int[] question = NetWizardUtils.prepareQuestion(code, getInputSize());
                double[] encodedAnswer = getNetWizard().queryNet(getNetParams().get(), question);
                String decodedAnswer = decodeAnswer(
                        DoubleStream.of(encodedAnswer).boxed()
                                .map(d -> d < (DoubleStream.of(encodedAnswer)
                                        .max().getAsDouble() - 0.00001) ? 0 : 1)
                                .mapToInt(Math::toIntExact)
                                .toArray(), TelegramBotInitializer.getyDictPath());
                Lists.partition(Arrays.asList(decodedAnswer.split("\n")), 100).forEach(block -> {
                    try {
                        sendMessage(update, String.join("\n", block));
                    } catch (TelegramApiException e) {
                        System.out.println("error sending answer block");
                    }
                });
            }
        }
    }

    public void sendMessage(Update update, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setParseMode("HTML");
        message.setChatId(String.valueOf(update.getMessage().getChatId()));
        if (update.getMessage().getMessageId() != null) {
            message.setReplyToMessageId(update.getMessage().getMessageId());
        }
        message.setText(text);
        execute(message);
    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }
}
