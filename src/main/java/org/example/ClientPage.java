package org.example;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class ClientPage {
    private final WebClient client;
    private HtmlPage page;

    public ClientPage(WebClient client, HtmlPage page) {
        this.client = client;
        this.page = page;
    }

    public WebClient getClient() {
        return client;
    }

    public HtmlPage getPage() {
        return page;
    }

    public void setPage(HtmlPage page) {
        this.page = page;
    }
}
