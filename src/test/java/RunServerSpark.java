import crawler.Crawler;
import configFileReader.ConfigFileReader;
import pageBuilder.IndexPageBuilder;
import serverSpark.Server;

import java.io.IOException;

public class RunServerSpark {

    private static String indexFileName = "index.html";

    private static void checkArgs(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: crawler_settings_file_path spark_settings_file_path");
            System.exit(1);
        }
    }

    private static void runCrawler(ConfigFileReader configFileReader) throws IOException {
        Crawler crawler = new Crawler(configFileReader);
        crawler.run();
    }

    private static void runServer(ConfigFileReader config) {
        Server server = new Server(config);
        server.start();
    }

    private static void runIndexPageBuilder(ConfigFileReader config) throws IOException{
        String savingFolder = config.getParameter("savingFolder");
        IndexPageBuilder builder = new IndexPageBuilder(savingFolder);
        builder.build();
        builder.savePage(savingFolder.concat(savingFolder.endsWith("/") ? indexFileName : "/".concat(indexFileName)));
    }

    public static void main(String[] args) {
        checkArgs(args);

        try {
            ConfigFileReader crawlerConfig = new ConfigFileReader(args[0]);
            runCrawler(crawlerConfig);
            System.out.println("Crawler has finished.");

            runIndexPageBuilder(crawlerConfig);
            System.out.println("Builder has finished.");

            runServer(new ConfigFileReader(args[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
