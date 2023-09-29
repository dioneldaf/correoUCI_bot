package org.example;

import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;

public class BotUser {
    private final String userType;
    private final Long telegramId;
    private String username;
    private String password;
    private ArrayList<Email> emails;

    public BotUser(User user) {
        this.userType = Const.STUDENTS_URL;
        this.telegramId = user.getId();
        this.emails = new ArrayList<>();
        this.username = null;
        this.password = null;
    }

    public String getUserType() {
        return userType;
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

    public void refreshEmails() throws Exception {
        emails.clear();
        int i = 0;
        for (HtmlTableRow tableRow : WebWork.getHtmlMessages(WebWork.initSession(userType, username, password))) {
            emails.add(new Email(tableRow, String.valueOf(i)));
            i++;
        }
    }

    @Override
    public boolean equals(Object o) {
        BotUser botUser = (BotUser) o;
        return telegramId.equals(botUser.getTelegramId());
    }
}
