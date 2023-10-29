package org.qwep.telegram;

public class TelegramCommand {
    private TelegramCommandType type;
    private String argument;

    TelegramCommand(String message) {
        type = TelegramCommandType.KEY;
        argument = message;
    }

    public TelegramCommandType getType() {
        return type;
    }

    public String getArgument() {
        return argument;
    }


}
