package serverSpark;

import configFileReader.ConfigFileReader;
import spark.Spark;

import static spark.Spark.port;
import static spark.Spark.staticFiles;

public class Server {

    public Server(ConfigFileReader config) {
        port(Integer.parseInt(config.getParameter("port")));
        staticFiles.location(config.getParameter("staticFilesLocation"));
        staticFiles.expireTime(600L);
        Spark.init();
    }

    public void start() {
        Spark.init();
    }

    public void finish() {
        Spark.stop();
    }
}
