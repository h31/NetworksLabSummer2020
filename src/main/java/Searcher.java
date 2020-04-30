import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Searcher {
    private String fileName;

    public Searcher(String fileName) {
        this.fileName = fileName;
    }

    public List<DetailsOfLink> search(String keyword) {
        List<DetailsOfLink> matchedLinks = new ArrayList<>();

        try {
            FileController fc = new FileController(fileName);
            InputStream is = fc.fileInputStream();

            for (DetailsOfLink detail : fc.readJsonFile(is)) {
                for (String word : splitKeyword(keyword)) {
                    if (detail.getTitle().toLowerCase().contains(word)
                            || detail.getDescription().toLowerCase().contains(word)
                            || detail.getKeywords().toLowerCase().contains(word)) {
                        matchedLinks.add(detail);
                        break;
                    }

                }
            }

            is.close();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return matchedLinks;
    }

    private String[] splitKeyword(String keyword) {
        return keyword.toLowerCase().replaceAll("\\s+", " ").split(" ");
    }

    public String toString(List<DetailsOfLink> matchedLinks) {
        StringBuilder result = new StringBuilder();
        result.append(matchedLinks.size()).append(" results found<hr />");
        for (DetailsOfLink link: matchedLinks) {
            result.append(link.getTitle()).append("<br />");
            if (link.getDescription().isEmpty())
                result.append("No description available <br />");
            else result.append(link.getDescription()).append("<br />");
            result.append(link.getUrl()).append("<hr />");
        }
        return result.toString();
    }
}
