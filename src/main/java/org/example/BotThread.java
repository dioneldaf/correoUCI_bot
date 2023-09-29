package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BotThread extends TelegramLongPollingBot implements Runnable {
    private final ArrayList<BotUser> botUsers;
    private final Update update;

    public BotThread(String botToken, ArrayList<BotUser> botUsers, Update update) {
        super(botToken);
        this.botUsers = botUsers;
        this.update = update;
    }

    @Override
    public void run() {
        onUpdateReceived(update);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                String message = update.getMessage().getText();
                if (message.startsWith("/start")) start(update);
                else if (message.startsWith("/register")) register(update);
                else if (message.startsWith("/emails")) emails(update);
            }
            deleteMessage(update.getMessage().getFrom().getId(), update.getMessage().getMessageId());
            return;
        }
        if (update.hasCallbackQuery()) {
            buttonPressed(update);
        }
    }

    private void start(Update update) {
        BotUser user = new BotUser(update.getMessage().getFrom());
        if (!botUsers.contains(user)) {
            botUsers.add(user);
            sendMessage("Hola :)", user.getTelegramId());
            sendMessage("Introduzca los datos de su cuenta con el siguiente comando (sin comillas):\n" +
                            "/register \"nombre de usuario\" \"contraseña\"", user.getTelegramId());
        } else {
            sendMessage("Hola otra vez ;)", user.getTelegramId());
            sendMessage("Si desea cambiar" +
                    " los datos de su cuenta use el siguiente comando (sin comillas):\n" +
                    "/register \"nombre de usuario\" \"contraseña\"", user.getTelegramId());
        }
    }

    private void register(Update update) {
        BotUser user = new BotUser(update.getMessage().getFrom());
        BotUser userInList = getUserInList(user);
        if (userInList == null) {
            sendMessage("Ocurrió un error desconocido :(\n" +
                    "Por favor, contacta a los desarrolladores", user.getTelegramId());
            return;
        }
        String[] texts = update.getMessage().getText().split(" ", 3);
        if (texts.length != 3) {
            sendMessage("Parámetros inválidos", userInList.getTelegramId());
            return;
        }
        userInList.setUsername(texts[1]);
        userInList.setPassword(texts[2]);
        sendMessage("""
                Datos registrados exitosamente!!
                Para ver la información de tus correos usa el siguiente comando (sin comillas):
                /emails "C"
                
                Sustituye "C" por la cantidad de correos que deseas obtener (cronológicamente) o
                escribe "All" en el lugar de "C" para obtener todos los correos.
                """, userInList.getTelegramId());
    }

    private void emails(Update update) {
        BotUser user = new BotUser(update.getMessage().getFrom());
        BotUser userInList = getUserInList(user);
        if (userInList == null) {
            sendMessage("Al parecer no te has registrado aún", user.getTelegramId());
            return;
        }
        if (userInList.getUsername() == null || userInList.getPassword() == null) {
            sendMessage("Usted aun no ha registrado sus datos de " +
                    "inicio de sesión", userInList.getTelegramId());
            sendMessage("Introduzca los datos de su cuenta con el siguiente comando (sin comillas): " +
                    "/register \"nombre de usuario\" \"contraseña\"", user.getTelegramId());
            return;
        }
        String[] texts = update.getMessage().getText().split(" ", 2);
        if (texts.length != 2) {
            sendMessage("Parámetros inválidos", userInList.getTelegramId());
            return;
        }
        int C;
        try {
            C = Integer.parseInt(texts[1]);
            if (C <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            if (!texts[1].toLowerCase().contains("all")) {
                sendMessage("Parámetros inválidos", userInList.getTelegramId());
                return;
            }
            C = -1;
        }
        Message messageWait;
        try {
            messageWait = sendMessage("Cargando"
                    + (C == -1 ? " todos los" : (" los últimos " + C))
                    + " mensajes... Esto puede tardar 1 minuto :)", user.getTelegramId());
            for (Email email : userInList.getEmails()) {
                if (email.getTelegramID() == null) continue;
                deleteMessage(user.getTelegramId(), email.getTelegramID());
            }
            userInList.refreshEmails();
        } catch (Exception e) {
            sendMessage("Ocurrió el siguiente error al acceder a los mensajes:\n" +
                    Arrays.toString(e.getStackTrace()), userInList.getTelegramId());
            e.printStackTrace();
            return;
        }
        //noinspection DataFlowIssue
        deleteMessage(userInList.getTelegramId(), messageWait.getMessageId());
        for (int i = C == -1 ? userInList.getEmails().size() - 1 : C - 1; i >= 0; i--) {
            Email email = userInList.getEmails().get(i);
            Message message = sendMessageWithButton(email.toString(), update.getMessage().getChatId(),
                    "📦  Ver todo el texto", email.getID());
            if (message == null) {
                return;
            }
            Integer telegramID = message.getMessageId();
            email.setTelegramID(telegramID);
        }
    }

    private void buttonPressed(Update update) {
        String ID = update.getCallbackQuery().getData();
        BotUser user = new BotUser(update.getCallbackQuery().getFrom());
        BotUser userInList = getUserInList(user);
        Email email = null;
        for (Email emailInList : userInList.getEmails()) {
            if (emailInList.getID().equals(ID)) {
                email = emailInList;
            }
        }
        if (email == null) {
            sendMessage("No pudimos cargar el mensaje :(", userInList.getTelegramId());
            return;
        }
        String url = userInList.getUserType();
        String emailUrl = email.getUrl();
        String username = userInList.getUsername();
        String password = userInList.getPassword();
        String text;
        try {
            text = WebWork.getCompleteText(url, emailUrl, username, password);
        } catch (Exception e) {
            sendMessage("Ocurrió el siguiente error al acceder al mensaje:\n" +
                    Arrays.toString(e.getStackTrace()), userInList.getTelegramId());
            e.printStackTrace();
            return;
        }
        String allText = email.completeToString(text);
        EditMessageText message = new EditMessageText();
        message.setChatId(userInList.getTelegramId());
        message.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        message.setText(allText);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /*
     *
     *
     * AUX METHODS
     *
     *
     */

    private Message sendMessage(String text, Long chatId) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(chatId);
        try {
            return execute(message);
        } catch (TelegramApiException ignored) {}
        return null;
    }

    private Message sendMessageWithButton(String text, Long chatId,
                                          @SuppressWarnings("SameParameterValue") String buttonName, String callBack) {
        SendMessage message = new SendMessage();
        message.setText(text);
        message.setChatId(chatId);
        try {
            return execute(addButtons(message, buttonName, callBack));
        } catch (TelegramApiException ignored) {}
        return null;
    }

    private void deleteMessage(Long chatID, Integer messageID) {
        DeleteMessage delete = new DeleteMessage();
        delete.setChatId(chatID);
        delete.setMessageId(messageID);
        try {
            execute(delete);
        } catch (TelegramApiException ignored) {}
    }

    private BotUser getUserInList(BotUser user) {
        BotUser userInList = null;
        for (BotUser botUser : botUsers) {
            if (botUser.equals(user)) {
                userInList = botUser;
                break;
            }
        }
        return userInList;
    }

    private SendMessage addButtons(SendMessage message, String buttonName, String callBack) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(buttonName);
        button.setCallbackData(callBack);
        rowInline.add(button);
        rowsInline.add(rowInline);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(inlineKeyboardMarkup);
        return message;
    }

    @Override
    public String getBotUsername() {
        return "correoUCI_bot";
    }
}
