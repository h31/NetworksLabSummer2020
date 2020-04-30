import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

public class FileController {
    private String fileName;

    public FileController(String fileName) {
        this.fileName = fileName;
    }

    public SequenceWriter initJSonWriter() {

        try {
            FileWriter fileWriter = new FileWriter(new File(fileName), true);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writer().writeValuesAsArray(fileWriter);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void setAndWriteValuesToFile(String title, String description, String keywords, URL url, SequenceWriter sequenceWriter) {

        try {
            DetailsOfLink detailsOfLink = new DetailsOfLink();
            detailsOfLink.setDescription(description);
            detailsOfLink.setKeywords(keywords);
            detailsOfLink.setTitle(title);
            detailsOfLink.setUrl(url);
            sequenceWriter.write(detailsOfLink);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public InputStream fileInputStream() {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    public List<DetailsOfLink> readJsonFile(InputStream inputStream) {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<DetailsOfLink>> typeReference = new TypeReference<List<DetailsOfLink>>() {
        };
        List<DetailsOfLink> detailsOfLinks = null;
        try {
            detailsOfLinks = mapper.readValue(inputStream, typeReference);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return detailsOfLinks;
    }

    public void removeUnwantedChars() {
        try {
            File tempFile = new File("src/main/resources/out1.json");
            FileWriter fw = new FileWriter(tempFile);

            Reader fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);

            fw.write(br.readLine().replaceAll("]\\[", ","));

            fw.close();
            br.close();
            fr.close();
            deleteFile();
            tempFile.renameTo(new File(fileName));

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void deleteFile() {
        File file = new File(fileName);
        try {
            Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
