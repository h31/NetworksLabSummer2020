package server;

import configFileReader.ConfigFileReader;

import java.io.IOException;
import java.net.ServerSocket;

public class Server {

    private final ConfigFileReader config;

    public Server(ConfigFileReader config) {
        this.config = config;
    }

    public void start() {
        try (ServerSocket clientSocket = new ServerSocket(Integer.parseInt(config.getParameter("port")))) {
            while (true) {
                new RequestHandler(clientSocket.accept(), config).run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
