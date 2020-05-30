package configFileReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigFileReader {

    private Map<String, String> parameters = new HashMap<>();

    public ConfigFileReader(String path) throws IOException {
        readFile(path);
    }

    private void readFile(String path) throws IOException {
        String line;

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(Paths.get(path).toFile()))){
            while ((line = bufferedReader.readLine()) != null) {
                saveParameter(parseLine(line));
            }
        }
    }

    private String[] parseLine(String line) throws IllegalArgumentException {
        String[] lineParts = line.split(":");

        if (lineParts.length != 2) {
            throw new IllegalArgumentException("Wrong line: " + "\"" + line + "\"");
        }

        return lineParts;
    }

    private void saveParameter(String[] keyValue) {
        parameters.put(keyValue[0].trim().toLowerCase(), keyValue[1].trim());
    }

    public String getParameter(String name) {
        return parameters.get(name.toLowerCase());
    }
    
}
