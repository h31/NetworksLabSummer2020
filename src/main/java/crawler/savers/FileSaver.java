package crawler.savers;

import java.io.File;
import java.net.URL;
import crawler.url.Url;
import crawler.url.Urls;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import configFileReader.ConfigFileReader;

import java.nio.file.StandardCopyOption;
import java.util.List;


public class FileSaver extends Saver {

    private Url currentUrl;
    private String savingPath;


    FileSaver(Url url, ConfigFileReader configFileReader) {
        this.currentUrl = url;
        this.programConfig = configFileReader;
        this.savingPath = (this.programConfig.getParameter("savingFolder") + "/" + Urls.removeScheme(url.toString()));
    }

    @Override
    public List<Saver> call() throws Exception {
        if (Files.exists(Paths.get(savingPath))) {
            return null;
        }

        downloadFile();
        return null;
    }

    private void downloadFile() throws IOException {
        new File(savingPath).mkdirs();
        InputStream inputStream = new URL(currentUrl.toString()).openStream();
        Files.copy(inputStream, Paths.get(savingPath), StandardCopyOption.REPLACE_EXISTING);
        inputStream.close();
    }
}
