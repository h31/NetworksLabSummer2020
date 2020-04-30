import spark.Spark;

import java.net.URL;

import static spark.Spark.*;

public class SparkServer {

    private int port;
    private String folder;

    public SparkServer(int port, String folder) {
        this.port = port;
        this.folder = folder;
        runServer();
        notFoundHandling();
    }

    private void runServer() {
        port(port);
        staticFiles.location(folder);
        staticFiles.expireTime(600L);
        Spark.init();
    }

    public void crawler(String fileName, int maxCrawledURLs) {
        Crawler crawler = new Crawler(fileName, maxCrawledURLs);
        get("/index.html/Crawl", (request, response) ->
                crawler.toString(crawler.crawlerAndIndexing(new URL(request.queryParams("url")))));

    }

    public void searcher(String fileName) {
        Searcher searcher = new Searcher(fileName);
        get("/index.html/Search", ((request, response) ->
                searcher.toString(searcher.search(request.queryParams("keyword")))));
    }

    private void notFoundHandling() {
        notFound("<html>\n" +
                "<body>\n" +
                "<h1>404</h1>\n" +
                "<h2>Page not found</h2>\n" +
                "<a href=\"http://localhost:" + port + "/index.html\">Go Home</a>\n" +
                "</body>\n" +
                "</html>");
    }

}
