package org.example;

import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class Email implements Serializable {
    private boolean read;
    private String sender;
    private String topic;
    private String stringDate;
    private boolean adjunct;
    private String preview;
    private String url;
    private String text;
    private final String ID;
    private Integer telegramID;
    private final LocalDateTime date;

    public Email(HtmlTableRow htmlMessage, String ID) {
        parse(htmlMessage);
        this.ID = ID;
        this.telegramID = null;
        this.text = null;
        this.date = Helper.parseDate(stringDate);
    }

    private void parse(HtmlTableRow htmlMessage) {
        List<HtmlTableCell> cells = htmlMessage.getCells();
        HtmlImage readImage = (HtmlImage) htmlMessage.getElementsByTagName("img").get(Const.FIRST_ELEMENT);
        read = readImage.asXml().contains(Const.READ_MESSAGE);
        sender = cells.get(Const.THIRD_ELEMENT).asXml().split("<td>")[1].split("<br/>")[0].strip();
        topic = htmlMessage.getElementsByTagName("span").get(Const.FIRST_ELEMENT).asNormalizedText();
        stringDate = cells.get(Const.FOURTH_ELEMENT).asNormalizedText().strip();
        adjunct = cells.get(Const.FIFTH_ELEMENT).asXml().contains(Const.HAS_ADJUNCT);
        try {
            preview = cells.get(Const.THIRD_ELEMENT).asXml()
                    .split("<!--<br><span class='Fragment'>")[1].split("</span>-->")[0];
        } catch (IndexOutOfBoundsException e) {
            preview = "<sin preview :(>";
        }
        url = cells.get(Const.THIRD_ELEMENT).getElementsByTagName("a")
                .get(Const.FIRST_ELEMENT).getAttribute("href");
    }

    public String completeToString(String text) {
        this.text = text;
        return getInfo() + "📄  Texto:\n" + this.text;
    }

    public String getID() {
        return ID;
    }

    public String getUrl() {
        return url;
    }

    public Integer getTelegramID() {
        return telegramID;
    }

    public void setTelegramID(Integer telegramID) {
        this.telegramID = telegramID;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String toString() {
         return getInfo() + "📄  Texto:\n" + preview;
    }

    private String getInfo() {
        return "👀  Leído: " + (read ? "✅" : "❌") + "\n" +
                "👤  De: " + sender + "\n" +
                "💬  Asunto: " + topic + "\n" +
                "📅  Fecha: " + stringDate + "\n" +
                "🔗  Adjunto(s): " + (adjunct ? "✅" : "❌") + "\n\n";
    }
}
