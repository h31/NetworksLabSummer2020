package crawler.savers;

import crawler.url.Url;
import configFileReader.ConfigFileReader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


public abstract class Saver implements Callable<List<Saver>> {

    ConfigFileReader programConfig;

    @Override
    public List<Saver> call() throws Exception {
        return null;
    }


    List<Saver> createCssPageSavers(List<Url> urls) {
        List<Saver> result = new ArrayList<>();

        for (Url url: urls) {
            result.add(new CssPageSaver(url, programConfig));
        }

        return result;
    }


    List<Saver> createWebPageSavers(List<Url> urls, int searchDepth) {
        List<Saver> result = new ArrayList<>();

        for (Url relativeLink: urls) {
            result.add(new WebPageSaver(searchDepth, relativeLink, programConfig));
        }

        return result;
    }


    protected List<Saver> createFileSavers(List<Url> urls) {
        List<Saver> result = new ArrayList<>();

        for (Url url: urls) {
            result.add(new FileSaver(url, programConfig));
        }

        return result;
    }
}
