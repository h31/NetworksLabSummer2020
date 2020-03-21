import crawler.Crawler;
import configFileReader.ConfigFileReader;
import serverSpark.Server;

import java.io.IOException;

public class RunServerSpark {

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

    public static void main(String[] args) {
        checkArgs(args);

        try {
            runCrawler(new ConfigFileReader(args[0]));

            System.out.println("Crawler has finished.");

            runServer(new ConfigFileReader(args[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
