package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;

public class Bot extends TelegramLongPollingBot {
    private ArrayList<BotUser> botUsers;
    private final String botToken;

    public Bot(String botToken) {
        super(botToken);
        this.botToken = botToken;
        try {
            this.botUsers = Helper.loadBin();
        } catch (Exception e) {
            this.botUsers = new ArrayList<>();
        }
    }

    public ArrayList<BotUser> getBotUsers() {
        return botUsers;
    }

    @Override
    public void onUpdateReceived(Update update) {
        Thread botThread = new Thread(new BotThread(botToken, botUsers, update));
        botThread.start();
    }

    public void notifier() {
        while (true) {
            for (BotUser user : botUsers) {
                Thread notifierThread = new Thread(new BotNotifier(botToken, user));
                notifierThread.start();
            }
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "correoUCI_bot";
    }
}
