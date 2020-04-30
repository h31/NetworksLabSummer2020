package crawler.savers;

import java.io.File;

import org.jsoup.Jsoup;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.io.IOException;

import crawler.url.Url;
import crawler.url.Urls;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import configFileReader.ConfigFileReader;

import java.io.FileNotFoundException;
import java.util.List;

public class WebPageSaver extends Saver {

    private int currentSearchDepth;
    private Document document;
    private String savingPath;
    private String fileName;
    private Url currentUrl;

    private final String DEFAULT_FILE_NAME = "index.html";

    public WebPageSaver(int currentSearchDepth, Url currentUrl, ConfigFileReader programConfig) {
        this.currentUrl = currentUrl;
        this.programConfig = programConfig;
        this.currentSearchDepth = currentSearchDepth;
        this.fileName = currentUrl.getFileName().equals("") ? DEFAULT_FILE_NAME : currentUrl.getFileName();
        this.savingPath = programConfig.getParameter("savingFolder") + "/" +
                Urls.removeScheme(Urls.removeFileName(currentUrl.toString()));
    }


    @Override
    public List<Saver> call() throws IOException {
        if (Files.exists(Paths.get(savingPath + fileName)) ||
                currentSearchDepth > Integer.parseInt(programConfig.getParameter("searchDepth"))) {
            return null;
        }

        return handleWebPage();
    }


    private List<Saver> handleWebPage() throws IOException {
        List<Saver> result;

        downloadWebPage();
        result = createSavers();
        saveWebPage();

        return result;
    }

    private List<Saver> createSavers() {
        List<Saver> result;

        result = createWebPageSavers(extractRelativeLinks(), currentSearchDepth + 1);
        result.addAll(createCssPageSavers(extractCssLinks()));
        result.addAll(createFileSavers(extractScriptUrls()));
        result.addAll(createFileSavers(extractFileUrls()));

        return result;
    }

    private void downloadWebPage() throws IOException {
        document = Jsoup.connect(currentUrl.toString()).get();
    }

    private void saveWebPage() throws FileNotFoundException {
        new File(savingPath).mkdirs();
        PrintWriter printWriter = new PrintWriter(savingPath + "/" + fileName);
        printWriter.println(document);
        printWriter.close();
    }

    private List<Url> extractRelativeLinks() {
        List<Url> result = new ArrayList<>();
        Elements links = document.select("a[href]");

        for (Element link: links) {
            if (Urls.areRelated(link.attr("abs:href"), currentUrl.toString()) &&
                    !Urls.isUrlAbsolute(link.attr("href"))) {
                result.add( new Url(link.attr("abs:href")).removeHash().removeQuery());
            }
        }

        return result;
    }

    private List<Url> extractCssLinks() {
        List<Url> result = new ArrayList<>();
        Elements cssLinks = document.head().select("[href*=.css]");

        for (Element cssLink: cssLinks) {
            result.add( new Url(cssLink.attr("abs:href")).removeHash().removeQuery() );
            cssLink.attr("href", "/" + Urls.removeScheme(cssLink.attr("abs:href")));
        }

        return result;
    }

    private List<Url> extractScriptUrls() {
        List<Url> result = new ArrayList<>();
        Elements scriptUrls = document.select("script[src]");

        for (Element scriptUrl: scriptUrls) {
            result.add(new Url(scriptUrl.attr("abs:src")).removeHash().removeQuery());
            scriptUrl.attr("src", "/" + Urls.removeScheme(scriptUrl.attr("abs:src")));
        }

        return result;
    }

    private List<Url> extractFileUrls() {
        List<Url> result = new ArrayList<>();
        Elements fileUrls = document.select("link[href]");

        for (Element fileUrl: fileUrls) {
            if (!fileUrl.attr("abs:href").isEmpty() && !fileUrl.attr("href").contains(".css")) {
                result.add(new Url(fileUrl.attr("abs:href")).removeHash().removeQuery());
                fileUrl.attr(
                        "href",
                        "/" + Urls.removeScheme(fileUrl.attr("abs:href"))
                );
            }
        }

        return result;
    }
}