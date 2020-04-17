package crawler;

import crawler.savers.Saver;
import java.io.File;
import java.io.FileReader;
import crawler.savers.WebPageSaver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.io.BufferedReader;
import configFileReader.ConfigFileReader;
import crawler.url.Url;
import pageBuilder.IndexPageBuilder;

import java.util.concurrent.Future;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class Crawler {

    private ExecutorService service;
    private ArrayList<String> initialLinks;
    private ConfigFileReader configFileReader;
    private ArrayDeque<Future<ArrayList<Saver>>> futures;
    private IndexPageBuilder listPageBuilder;

    public Crawler(ConfigFileReader configFileReader) {
        this.configFileReader = configFileReader;
        this.listPageBuilder = IndexPageBuilder.getInstance();
    }

    public void run() throws IOException {
        futures = new ArrayDeque<>();
        initialLinks = readInitialLinksFile(configFileReader.getParameter("initialLinksFile"));
        service = Executors.newFixedThreadPool(Integer.parseInt(configFileReader.getParameter("maxNumberOfStreams")));

        submitInitialLinks();
        waitForFutures();

        service.shutdown();
        listPageBuilder.saveToFile("src/main/resources/downloaded/index.html");
    }

    private void submitInitialLinks() {
        for (String link : initialLinks) {
            futures.addLast(service.submit(new WebPageSaver(1, new Url(link), configFileReader)));
        }
    }

    private void waitForFutures() {
        while (!futures.isEmpty()) {
            try {
                if (futures.getFirst().isDone()) {
                    if (futures.getFirst().get() != null ) {
                        for (Saver saver : futures.getFirst().get()) {
                            futures.addLast(service.submit(saver));
                        }
                    }

                    futures.removeFirst();
                }
            } catch (Exception e) {
                futures.removeFirst();
                e.printStackTrace();
            }
        }
    }


    private ArrayList<String> readInitialLinksFile(String path) throws IOException{
        String line;
        ArrayList<String> initialLinks = new ArrayList<>();

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(path)))){
            while ((line = bufferedReader.readLine()) != null) {
                initialLinks.add(line.trim());
            }
        }

        return initialLinks;
    }
}
