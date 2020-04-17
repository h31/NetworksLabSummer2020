package crawler.url;

public class Url {

    private String url;


    public Url(String url) {
        this.url = url;
    }


    Url removeScheme() {
        this.setScheme("");
        url = url.replaceFirst("://", "");
        return this;
    }

    public Url removeFileName() {
        url = url.replaceFirst(getFileName(), "");
        return this;
    }

    private String _removeHash() {
        return url.replaceAll("#.*", "");
    }

    public Url removeHash() {
        url = _removeHash();
        return this;
    }

    // + removeHash
    public Url removeQuery() {
        url = url.replaceAll("\\?" + getQuery() , "");
        return this;
    }


    public boolean isAbsolute() {
        return url.contains("://");
    }


    public boolean isRelative() {
        return !this.isAbsolute();
    }


    public boolean isRelated(Url anotherUrl) {
        return url.contains(anotherUrl.getHostName());
    }


    // ------------------------------
    // --          setters         --
    // ------------------------------

    public Url setScheme(String scheme) {
        url = url.replaceFirst(getScheme(), scheme);
        return this;
    }


    public Url setHostName(String hostName) {
        url = url.replaceFirst(getHostName(), hostName);
        return this;
    }


    // ------------------------------
    // --          getters         --
    // ------------------------------

    public String getScheme() {
        return url.split("://")[0];
    }


    public String getPath() {
        return url.replaceFirst("\\?.*", "").replaceFirst("#.*", "").
                replaceFirst(getRoot() + "/", "");
    }


    public String getRoot() {
        return this.getScheme() + "://" + this.getHostName();
    }


    public String getFileName() {
        String[] pathParts = this.getPath().split("/");

        if (pathParts[pathParts.length - 1].contains(".")) {
            return pathParts[pathParts.length - 1];
        }

        return "";
    }


    public String getHostName() {
        return url.split("://").length == 1 ?
                url.split("/")[0] :
                url.split("://")[1].split("/")[0];
    }

    public String getHash() {
        String[] parts = this.url.split("#");
        return parts.length == 1 ? "" : parts[1];
    }

    public String getQuery() {
        String[] parts = _removeHash().split("\\?");
        return parts.length == 1 ? "" : parts[1];
    }

    @Override
    public String toString() {
        return url;
    }
}
