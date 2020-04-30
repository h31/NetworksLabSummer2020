import com.fasterxml.jackson.databind.SequenceWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class Crawler {

    private final Set<URL> allCrawledURLs;
    private int maxCrawledURLS;
    FileController fileController;

    public Crawler(String fileName, int maxCrawledURLS) {
        this.maxCrawledURLS = maxCrawledURLS;
        this.allCrawledURLs = new HashSet<>();
        fileController = new FileController(fileName);
        fileController.deleteFile();

    }

    public Set<URL> crawlerAndIndexing(URL startURL) {
        Set<URL> crawledURLStartingWithRoot = new HashSet<>();
        try {
            SequenceWriter seqW = fileController.initJSonWriter();
            crawl(initURLS(startURL), seqW, crawledURLStartingWithRoot);
            if (seqW != null) {
                seqW.close();
            }
            fileController.removeUnwantedChars();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return crawledURLStartingWithRoot;
    }


    private Set<URL> initURLS(final URL startURL) {
        final Set<URL> startURLS = new HashSet<>();
        startURLS.add(startURL);
        return startURLS;
    }

    private void crawl(final Set<URL> aboutToBeCrawledURLS, SequenceWriter seqW, Set<URL> crawledURLStartingWithRoot) {
        aboutToBeCrawledURLS.removeAll(this.allCrawledURLs);         //remove crawled links
        if (!aboutToBeCrawledURLS.isEmpty()) {
            final Set<URL> newURLS = new HashSet<>();
            try {
                for (final URL url : aboutToBeCrawledURLS) {

                    final Document document = Jsoup.connect(url.toString()).get();
                    getDetailsOfLink(url, seqW);

                    allCrawledURLs.add(url);

                    crawledURLStartingWithRoot.add(url);
                    if (crawledURLStartingWithRoot.size() >= maxCrawledURLS)
                        return;

                    final Elements linksOnPage = document.select("a[href]");
                    for (final Element page : linksOnPage) {
                        final String urlText = page.attr("abs:href").trim();
                        final URL discoveredURL = new URL(urlText);
                        newURLS.add(discoveredURL);
                    }
                }
            } catch (final IOException | Error ignored) {
            }
            crawl(newURLS, seqW, crawledURLStartingWithRoot);
        }
    }

    private void getDetailsOfLink(final URL url, SequenceWriter seqW) {
        try {
            final Document document = Jsoup.connect(url.toString()).get();
            String title = document.title();

            String description = "";
            String keywords = "";
            Elements metas = document.select("meta");
            for (final Element meta : metas) {
                if (meta.attr("name").toLowerCase().equals("description")) {
                    description = meta.attr("content");
                }
                if (meta.attr("name").toLowerCase().equals("keywords")) {
                    keywords = meta.attr("content");
                }
            }
            fileController.setAndWriteValuesToFile(title, description, keywords, url, seqW);
        } catch (final Exception | Error ignored) {
        }

    }

    public String toString(Set<URL> crawledURLStartingWithRoot) {

        StringBuilder result = new StringBuilder();
        result.append("Crawled ").append(crawledURLStartingWithRoot.size()).append(" links<hr />");
        for (URL url : crawledURLStartingWithRoot) {
            result.append(url).append("<br /><br />");
        }
        return result.toString();
    }

}
