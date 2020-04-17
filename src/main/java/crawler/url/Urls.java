package crawler.url;

public class Urls {

    public static String removeScheme(String url) {
        String[] urlParts = url.split("://");
        return urlParts.length == 1 ? url : urlParts[1];
    }

    static String extractScheme(String url) {
        return url.split("://")[0];
    }

    public static String extractHostName(String url) {
        return url.split("://").length == 1 ?
                url.split("/")[0] :
                url.split("://")[1].split("/")[0];
    }

    static String extractPath(String url) {
        String result = url.replaceAll(extractScheme(url) + "://" + extractHostName(url) + "/", "");
        result = removeHash(result);
        result = removeQuery(result);
        return result;
    }

    static String removeHash(String url) {
        return url.replaceAll("#.*", "");
    }

    static String removeQuery(String url) {
        return url.replaceAll("\\?.*", "");
    }

    public static boolean isUrlAbsolute(String url) {
        return url.contains("://");
    }

    public static boolean isRelative(String url) {
        return !url.contains("://");
    }

    public static boolean areRelated(String firstUrl, String secondUrl) {
        return new Url(firstUrl).isRelated(new Url(secondUrl));
    }

    public static String getFileName(String url) {
        String[] pathParts = extractPath(url).split("/");

        if (pathParts[pathParts.length - 1].contains(".")) {
            return pathParts[pathParts.length - 1];
        }

        return "";
    }

    public static String removeFileName(String url) {
        return url.replaceFirst(getFileName(url), "");
    }

    private static String removeLastPart(String urlPath) {
        return urlPath.substring(0, urlPath.length() - urlPath.split("/")[0].length());
    }

    public static String reach(String src, String dst) {
        src = removeFileName(removeQuery(removeHash(src)));

        while (dst.startsWith("../")) {
            dst = dst.replaceFirst("../", "");
            src = removeLastPart(src);
        }

        if (!src.endsWith("/")) {
            src = src.concat("/");
        }

        if (dst.startsWith("/")) {
            dst = dst.replaceFirst("/", "");
        }

        return src.concat(dst);
    }
}
