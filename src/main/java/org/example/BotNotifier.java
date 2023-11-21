package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

public class BotNotifier extends TelegramLongPollingBot implements Runnable {
    private final BotUser user;

    public BotNotifier(String token, BotUser user) {
        super(token);
        this.user = user;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(user.getSleep());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Email lastEmail = user.hasNewEmail();
        if (lastEmail != null) {
            String text = "📨  Tienes un nuevo correo UCI  📨\n\n";
            text = text.concat(lastEmail.toString());
            text = text.concat("\n\n🤔  Para ver el mensaje completo escribe el siguiente comando:\n/emails 1");
            Message message = sendMessage(text, user.getTelegramId());
            if (message == null) return;
            Integer telegramID = message.getMessageId();
            lastEmail.setTelegramID(telegramID);
            user.getEmails().add(0, lastEmail);
        }
    }

    private Message sendMessage(String text, Long chatId) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(chatId);
        try {
            return execute(message);
        } catch (TelegramApiException e) {
            error(e, chatId);
        }
        return null;
    }

    private void error(Exception e, Long chatID) {
        sendMessage("😵  Ocurrió el siguiente error:\n\n" +
                e.getMessage(), chatID);
        String text = "Usuario: " + chatID;
        text = text.concat("\n\nMensaje:\n").concat(e.getMessage());
        text = text.concat("\n\nTrace:\n").concat(Arrays.toString(e.getStackTrace()));
        sendMessage(text, Const.EXC_CHANNEL_ID);
    }

    @Override
    public void onUpdateReceived(Update update) {}

    @Override
    public String getBotUsername() {
        return "correoUCI_bot";
    }
}
