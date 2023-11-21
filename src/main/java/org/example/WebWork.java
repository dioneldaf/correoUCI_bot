package org.example;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WebWork {
    public static ClientPage initSession(String url, String username, String password) throws Exception {
        WebClient client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
        HtmlPage loginPage = client.getPage(url);
//        client.close();

        HtmlForm form = loginPage.getForms().get(Const.FIRST_ELEMENT);
        HtmlInput usernameInput = form.getInputByName("username");
        HtmlInput passwordInput = form.getInputByName("password");
        usernameInput.setValueAttribute(username);
        passwordInput.setValueAttribute(password);

        HtmlSelect selectInput = (HtmlSelect) loginPage.getElementById("client");
        selectInput.setSelectedIndex(Const.THIRD_ELEMENT);
        HtmlSubmitInput submitButton = form.getFirstByXPath("//input[@class='ZLoginButton DwtButton']");
        HtmlPage page = submitButton.click();

        HtmlDivision divError = (HtmlDivision) page.getElementById("ZLoginErrorPanel");
        if (divError != null) throw new IllegalArgumentException(divError.asNormalizedText());
        page = changePreferences(page);
        return new ClientPage(client, page);
    }

    private static HtmlPage changePreferences(HtmlPage page) throws IOException {
        HtmlPage preferencesPage = page.getElementById("TAB_OPTIONS").click();
        preferencesPage = preferencesPage.getAnchorByHref("/h/options?selected=mail&prev=mail").click();

        HtmlSelect numberOfMessages = (HtmlSelect) preferencesPage.getElementById("itemsPP");
        HtmlSelect group = (HtmlSelect) preferencesPage.getElementById("groupMailBy");
        HtmlInput howHtml = (HtmlInput) preferencesPage.getElementById("viewHtml");
        HtmlInput viewRight = (HtmlInput) preferencesPage.getElementById("viewRight");
        HtmlInput preview = (HtmlInput) preferencesPage.getElementById("zimbraPrefShowFragments");
        HtmlInput searchMode = (HtmlInput) preferencesPage.getElementById("zimbraPrefMailInitialSearch");

        numberOfMessages.setSelectedIndex(Const.SECOND_ELEMENT);
        group.setSelectedIndex(Const.SECOND_ELEMENT);
        howHtml.setChecked(true);
        viewRight.setChecked(true);
        preview.setChecked(true);
        searchMode.setValue("in:inbox");

        HtmlInput save = (HtmlInput) preferencesPage.getElementById("SOPSEND");
        preferencesPage = save.click();

        return preferencesPage.getElementById("TAB_MAIL").click();
    }

    private static HtmlPage searchForAnchor(HtmlPage page) throws IOException {
        DomNodeList<DomElement> anchors = page.getElementsByTagName("a");
        for (DomElement element : anchors) {
            HtmlAnchor anchor = (HtmlAnchor) element;
            if (anchor.asXml().contains("id=")) continue;
            if (anchor.asXml().contains("Correo")) {
                return anchor.click();
            }
        }
        throw new IOException("No se pudieron cambiar las preferencias");
    }

    public static ArrayList<HtmlTableRow> getHtmlMessages(ClientPage clientPage) throws IOException {
        HtmlPage page = clientPage.getPage();
        ArrayList<HtmlTableRow> htmlMessages = new ArrayList<>();
        while (true) {
            HtmlTableBody messList = (HtmlTableBody) page.getElementById("mess_list_tbody");
            htmlMessages.addAll(messList.getRows());
            DomNodeList<DomElement> images = page.getElementsByTagName("img");
            HtmlImage image = (HtmlImage) images.get(images.size() - 1);
            if (!image.getAltAttribute().equals(Const.NEXT_PAGE)) break;
            page = ((DomElement) image).click();
        }
        clientPage.getClient().close();
        return htmlMessages;
    }

    public static String getCompleteText(
            String url, String messageUrl, String username, String password) throws Exception {
        ClientPage clientPage = initSession(url, username, password);
        HtmlPage page = clientPage.getPage();
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
        clientPage.getClient().close();
        return text;
    }
}
