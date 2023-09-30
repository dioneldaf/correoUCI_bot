package org.example;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebWork {
    public static HtmlPage initSession(String url, String username, String password) throws Exception {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        HtmlPage loginPage = client.getPage(url);

        HtmlForm form = loginPage.getForms().get(Const.FIRST_ELEMENT);
        HtmlInput usernameInput = form.getInputByName("username");
        HtmlInput passwordInput = form.getInputByName("password");
        usernameInput.setValueAttribute(username);
        passwordInput.setValueAttribute(password);

        HtmlSelect selectInput = (HtmlSelect) loginPage.getElementById("client");
        selectInput.setSelectedIndex(Const.THIRD_ELEMENT);
        HtmlSubmitInput submitButton = form.getFirstByXPath(
                "//input[@class='ZLoginButton DwtButton']");
        HtmlPage page = submitButton.click();
        if (page.getElementById("ZLoginErrorPanel") != null) throw new IllegalArgumentException();
        return page;
    }

    public static ArrayList<HtmlTableRow> getHtmlMessages(HtmlPage page) throws IOException {
        ArrayList<HtmlTableRow> htmlMessages = new ArrayList<>();
        while (true) {
            HtmlTableBody messList = (HtmlTableBody) page.getElementById("mess_list_tbody");
            htmlMessages.addAll(messList.getRows());
            DomNodeList<DomElement> images = page.getElementsByTagName("img");
            HtmlImage image = (HtmlImage) images.get(images.size() - 1);
            if (!image.getAltAttribute().equals(Const.NEXT_PAGE)) break;
            page = ((DomElement) image).click();
        }
        return htmlMessages;
    }

    public static String getCompleteText(
            String url, String messageUrl, String username, String password) throws Exception {
        HtmlPage page = initSession(url, username, password);
        int listNumber = Integer.parseInt(messageUrl.split("&so=")[1].split("&sc=")[0]) % 25;
        int index = Integer.parseInt(messageUrl.split("si=")[1].split("&so")[0]);
        for (int i = 0; i < listNumber; i++) {
            DomNodeList<DomElement> images = page.getElementsByTagName("img");
            HtmlImage image = (HtmlImage) images.get(images.size() - 1);
            page = ((DomElement) image).click();
        }
        HtmlTableBody table = page.getHtmlElementById("mess_list_tbody");
        List<HtmlTableRow> rows = table.getRows();
        HtmlTableRow row = rows.get(index);
        HtmlTableCell cell = row.getCells().get(Const.THIRD_ELEMENT);
        HtmlAnchor link = (HtmlAnchor) cell.getElementsByTagName("a").get(Const.FIRST_ELEMENT);
        page = link.click();
        HtmlTableCell iframe = (HtmlTableCell) page.getElementById("iframeBody");
        String text = iframe.asNormalizedText();
        if (text.length() > 3500) {
            text = text.substring(0, 3500);
            text = text.concat("\n...");
        }
        return text;
    }
}
