package server;

import configFileReader.ConfigFileReader;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class RequestHandler implements Runnable{

    private Socket socket;
    private ConfigFileReader config;

    RequestHandler(Socket socket, ConfigFileReader config) {
        this.socket = socket;
        this.config = config;
    }

    @Override
    public void run() {
        try {
            String queryPath = getQueryPath(readHeader());
            String requestedFilePath = config.getParameter("staticFilesLocation") +queryPath;

            if (queryPath == null) {
                sendResponse("404 Not Found", config.getParameter("errorPageLocation"));
                socket.close();
                return;
            }

            if (!Files.exists(Paths.get(requestedFilePath))) {
                if (requestedFilePath.contains(".html")) {
                    sendResponse("404 Not Found", config.getParameter("errorPageLocation"));
                } else {
                    sendResponse("404 Not Found", null);
                }
                socket.close();
                return;
            };

            sendResponse("200 OK", config.getParameter("staticFilesLocation") +queryPath);
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getQueryPath(String queryHeader) {
        String[] queryParts = queryHeader.split(" ");
        return queryParts.length > 1 ? queryHeader.split(" ")[1].replaceAll("\\?.*", "") : null;
    }

    private String readHeader() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line;

        while ( (line = reader.readLine()) != null && !line.isEmpty() ) {
            builder.append(line);
            builder.append(System.getProperty("line.separator"));
        }

        return builder.toString();
    }

    private void sendResponse(String code, String filePath) throws IOException {
        byte[] fileBytes = filePath == null ? new byte[0] : Files.readAllBytes(Paths.get(filePath));
        int contentLength = filePath == null ? 0 : fileBytes.length;
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

        outputStream.writeBytes("HTTP/1.1 " + code + "\r\n");
        outputStream.writeBytes("Server: Java HTTPServer");
        outputStream.writeBytes("Content-Type: \r\n");
        outputStream.writeBytes("Content-Length: " + contentLength + "\r\n");
        outputStream.writeBytes("Connection: close\r\n");
        outputStream.writeBytes("\r\n");
        outputStream.write(fileBytes);
        outputStream.close();
    }

}
