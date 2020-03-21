import crawler.Crawler;
import configFileReader.ConfigFileReader;
import server.Server;

import java.io.IOException;

public class RunServer {

    private static void checkArgs(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: crawler_settings_file_path server_settings_file_path");
            System.exit(1);
        }
    }

    private static void runCrawler(ConfigFileReader configFileReader) throws IOException {
        Crawler crawler = new Crawler(configFileReader);
        crawler.run();
    }

    public static void main(String[] args) {
        checkArgs(args);

        try {
            runCrawler(new ConfigFileReader(args[0]));

            System.out.println("Crawler has finished.");

            new Server(new ConfigFileReader(args[1])).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
