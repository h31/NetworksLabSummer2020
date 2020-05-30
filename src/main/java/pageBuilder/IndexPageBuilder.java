package pageBuilder;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;

public class IndexPageBuilder {

    private Map<String, List<String>> mapOfUrls;
    private final String startDir;

    public IndexPageBuilder(String startDir) {
        this.startDir = startDir;
        this.mapOfUrls = new HashMap<>();
    }

    private void walk(String curDir) {
        File curFile = new File(curDir);

        if (curFile.isFile() && curDir.endsWith(".html")) {
            String relativePath = curDir.split(startDir)[1];
            relativePath = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
            String key = relativePath.split("/")[0];
            addUrl(key, relativePath);
        }

        if (curFile.isDirectory()) {
            for (String dir: curFile.list()) {
                walk(curDir + "/"+ dir);
            }
        }
    }

    public void build() {
        walk(startDir);
    }

    private void addUrl(String hostName, String url) {
        if (mapOfUrls.containsKey(hostName)) {
            mapOfUrls.get(hostName).add(url);
        } else {
            List<String> listOfUrls = new ArrayList<>();
            listOfUrls.add(url);
            mapOfUrls.put(hostName, listOfUrls);
        }
    }

    private void addSublist(Element list, List<String> urls) {
        Element listHeader = list.appendElement("ul");

        for (String url: urls) {
            listHeader.appendElement("li").appendElement("a").attr("href", url).text(url);
        }
    }

    private void addList(Element list) {
        for (Map.Entry<String, List<String>> entry : mapOfUrls.entrySet()) {
            Element newElement = list.appendElement("li").text(entry.getKey());
            addSublist(newElement, entry.getValue());
        }
    }

    private Document generateDocument() {
        Document document = Document.createShell("");
        Element list = document.body().appendElement("ul").attr("id", "main_list");
        addList(list);
        return document;
    }

    private String normaliseFileName(String fileName) {
        return fileName.endsWith(".html") ? fileName : fileName + ".html";
    }

    public void savePage(String fileName) throws IOException {
        PrintWriter writer = new PrintWriter(normaliseFileName(fileName));
        writer.println(generateDocument().outerHtml());
        writer.close();
    }

}
