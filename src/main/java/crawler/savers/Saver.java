package crawler.savers;

import crawler.url.Url;
import configFileReader.ConfigFileReader;

import java.util.ArrayList;
import java.util.concurrent.Callable;


public abstract class Saver implements Callable<ArrayList<Saver>> {

    ConfigFileReader programConfig;

    @Override
    public ArrayList<Saver> call() throws Exception {
        return null;
    }


    ArrayList<Saver> createCssPageSavers(ArrayList<Url> urls) {
        ArrayList<Saver> result = new ArrayList<>();

        for (Url url: urls) {
            result.add(new CssPageSaver(url, programConfig));
        }

        return result;
    }


    ArrayList<Saver> createWebPageSavers(ArrayList<Url> urls, int searchDepth) {
        ArrayList<Saver> result = new ArrayList<>();

        for (Url relativeLink: urls) {
            result.add(new WebPageSaver(searchDepth, relativeLink, programConfig));
        }

        return result;
    }


    protected ArrayList<Saver> createFileSavers(ArrayList<Url> urls) {
        ArrayList<Saver> result = new ArrayList<>();

        for (Url url: urls) {
            result.add(new FileSaver(url, programConfig));
        }

        return result;
    }
}
