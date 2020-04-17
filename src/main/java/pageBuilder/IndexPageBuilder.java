package pageBuilder;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import crawler.url.Urls;
import crawler.url.Url;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexPageBuilder {

    private Map<String, List<String>> mapOfUrls;
    private static IndexPageBuilder instance;

    private IndexPageBuilder() {
        this.mapOfUrls = new HashMap<>();
    }

    synchronized public static IndexPageBuilder getInstance() {
        if (instance == null) {
            instance =  new IndexPageBuilder();
        }
        return instance;
    }

    synchronized private void addUrl(String hostName, String url) {
        if (mapOfUrls.containsKey(hostName)) {
            mapOfUrls.get(hostName).add(url);
        } else {
            List<String> listOfUrls = new ArrayList<>();
            listOfUrls.add(url);
            mapOfUrls.put(hostName, listOfUrls);
        }
    }

    public void addUrl(String url) {
        String hostName = Urls.extractHostName(url);
        addUrl(hostName, url);
    }

    public void addUrl(Url url) {
        String hostName = url.getHostName();
        addUrl(hostName, url.toString());
    }

    private void addList(Element list) {
        for (Map.Entry<String, List<String>> entry : mapOfUrls.entrySet()) {
            Element newElement = list.appendElement("li").text(entry.getKey());
            addSublist(newElement, entry.getValue());
        }
    }

    private void addSublist(Element list, List<String> urls) {
        Element listHeader = list.appendElement("ul");

        for (String url: urls) {
            listHeader.appendElement("li").appendElement("a").attr("href", url).text(url);
        }
    }

    private Document generateDocument() {
        Document document = Document.createShell("");
        Element list = document.body().appendElement("ul").attr("id", "main_list");
        addList(list);
        return document;
    }

    public String toString() {
        return generateDocument().toString();
    }

    private String normaliseFileName(String fileName) {
        return fileName.endsWith(".html") ? fileName : fileName + ".html";
    }

    public void saveToFile(String fileName) throws IOException {
        PrintWriter writer = new PrintWriter(normaliseFileName(fileName));
        writer.println(generateDocument().outerHtml());
        writer.close();
    }

}
