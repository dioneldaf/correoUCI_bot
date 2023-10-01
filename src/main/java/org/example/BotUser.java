package org.example;

import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class BotUser implements Serializable {
    private String userType;
    private final Long telegramId;
    private String username;
    private String password;
    private ArrayList<Email> emails;
    private Message lastNormalMessage;
    private LocalDateTime lastEmail;

    public BotUser(User user) {
        this.telegramId = user.getId();
        this.emails = new ArrayList<>();
        this.userType = null;
        this.username = null;
        this.password = null;
        this.lastNormalMessage = null;
        this.lastEmail = null;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Long getTelegramId() {
        return telegramId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<Email> getEmails() {
        return emails;
    }

    public void setEmails(ArrayList<Email> emails) {
        this.emails = emails;
    }

    public Message getLastNormalMessage() {
        return lastNormalMessage;
    }

    public void setLastNormalMessage(Message lastNormalMessage) {
        this.lastNormalMessage = lastNormalMessage;
    }

    public void refreshEmails() throws Exception {
        emails.clear();
        int i = 0;
        for (HtmlTableRow tableRow : WebWork.getHtmlMessages(WebWork.initSession(userType, username, password))) {
            emails.add(new Email(tableRow, String.valueOf(i)));
            i++;
        }
    }

    private boolean hasNewEmail() throws Exception {
        refreshEmails();
        if (lastEmail == null) {
            if (!emails.isEmpty()) {
                lastEmail = emails.get(Const.FIRST_ELEMENT).getDate();
            }
            return false;
        }
        if (lastEmail.isAfter(emails.get(Const.FIRST_ELEMENT).getDate())) {
            lastEmail = emails.get(Const.FIRST_ELEMENT).getDate();
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BotUser botUser)) return false;
        return telegramId.equals(botUser.getTelegramId());
    }
}
