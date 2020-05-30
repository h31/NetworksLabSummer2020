package crawler.url;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TestUrls {

    private final String url = "scheme://domain/path/file.name?query#hash";

    @Test
    void removeSchemeTest() {
        String expectedUrl = "domain/path/file.name?query#hash";
        assertEquals(expectedUrl, Urls.removeScheme(url));
        assertEquals(expectedUrl, Urls.removeScheme(expectedUrl));
    }


    @Test
    void extractSchemeTest() {
        String expectedUrl = "scheme";
        assertEquals(expectedUrl, Urls.extractScheme(url));
    }


    @Test
    void extractHostNameTest() {
        String expectedUrl = "domain";
        assertEquals(expectedUrl, Urls.extractHostName(url));
    }


    @Test
    void extractPathText() {
        String expectedUrl = "path/file.name";
        assertEquals(expectedUrl, Urls.extractPath(url));
    }


    @Test
    void isUrlAbsoluteTest() {
        Assertions.assertTrue(Urls.isUrlAbsolute(url));
        Assertions.assertFalse(Urls.isUrlAbsolute(Urls.removeScheme(url)));
    }

    @Test
    void removeHashTest() {
        String expectedUrl = "scheme://domain/path/file.name?query";
        assertEquals(expectedUrl, Urls.removeHash(url));
    }

    @Test
    void areRelatedTest() {
        String firstUrl = "scheme://domain/path/file.name?query#hash";
        String secondUrl = "anotherScheme://domain/anotherPath/";
        String thirdUrl = "scheme://anotherDomain/path/file.name?query#hash";

        Assertions.assertTrue(Urls.areRelated(firstUrl, secondUrl));
        Assertions.assertFalse(Urls.areRelated(firstUrl, thirdUrl));
    }

    @Test
    void getFileNameTest() {
        String fileName = "file.name";

        assertEquals(fileName, Urls.getFileName(url));
    }

    @Test
    void removeFileNameTest() {
        String expectedUrl = "scheme://domain/path/?query#hash";

        assertEquals(expectedUrl, Urls.removeFileName(url));
    }


}
