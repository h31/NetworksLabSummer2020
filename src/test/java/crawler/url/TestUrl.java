package crawler.url;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class TestUrl {

    private Url url;

    @BeforeEach
    void urlDefinition() {
        url = new Url("scheme://domain/path/file.name?query#hash");
    }

    @Test
    void removeSchemeTest() {
        String expectedUrl = "domain/path/file.name?query#hash";
        Assertions.assertEquals(expectedUrl, url.removeScheme().toString());
        Assertions.assertEquals(expectedUrl, new Url(expectedUrl).removeScheme().toString());
    }

    @Test
    void removeFileNameTest() {
        String expectedUrl = "scheme://domain/path/?query#hash";
        Assertions.assertEquals(expectedUrl, url.removeFileName().toString());
        Assertions.assertEquals(expectedUrl, new Url(expectedUrl).removeFileName().toString());
    }

    @Test
    void removeHashTest() {
        String expectedUrl = "scheme://domain/path/file.name?query";
        Assertions.assertEquals(expectedUrl, url.removeHash().toString());
        Assertions.assertEquals(expectedUrl, new Url(expectedUrl).removeHash().toString());
    }

    @Test
    void removeQueryTest() {
        String expectedUrl = "scheme://domain/path/file.name#hash";
        Assertions.assertEquals(expectedUrl, url.removeQuery().toString());
        Assertions.assertEquals(expectedUrl, new Url(expectedUrl).removeQuery().toString());
    }

    @Test
    void isAbsoluteTest() {
        Assertions.assertTrue(url.isAbsolute());
    }

    @Test
    void isRelativeTest() {
        Assertions.assertTrue(url.removeScheme().isRelative());
    }

    @Test
    void setSchemeTest() {
        String expectedUrl = "newScheme://domain/path/file.name?query#hash";
        Assertions.assertEquals(expectedUrl, url.setScheme("newScheme").toString());
    }

    @Test
    void setHostNameTest() {
        String expectedUrl = "scheme://newDomain/path/file.name?query#hash";
        Assertions.assertEquals(expectedUrl, url.setHostName("newDomain").toString());
    }


    @Test
    void getHashTest() {
        String expectedUrl = "hash";
        Assertions.assertEquals(expectedUrl, url.getHash());
    }

    @Test
    void getQueryTest() {
        String expectedUrl = "query";
        Assertions.assertEquals(expectedUrl, url.getQuery());
    }
}
