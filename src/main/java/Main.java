
public class Main {

    private static final int PORT = 4567;
    private static final String FOLDER_STATIC_FILES = "/public";
    private static final int MAX_CRAWLED_URLS = 100;
    private static final String FILE_NAME = "src/main/resources/out.json";


    public static void main(String[] args) {

        SparkServer sparkServer = new SparkServer(PORT, FOLDER_STATIC_FILES);

        sparkServer.crawler(FILE_NAME, MAX_CRAWLED_URLS);
        sparkServer.searcher(FILE_NAME);
    }


}
