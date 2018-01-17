package dk.kb.elivagar.testutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestFileUtils {
    
    protected static final String TEMPDIR_NAME = "tempDir";
    protected static File tempDir = new File(TEMPDIR_NAME);

    public static String readFile(File file) throws IOException {
        try(BufferedReader br = new BufferedReader(new FileReader(file));) {
            StringBuffer res = new StringBuffer();
            String line;
            while((line = br.readLine()) != null) {
                res.append(line);
                // Add new line feed??
            }

            return res.toString();
        }
    }
    
    public static void setupTempDir() throws IOException {
        tempDir = createEmptyDirectory(TEMPDIR_NAME);
    }
    
    public static void tearDown() {
        deleteFile(tempDir);
    }

    public static File getTempDir() {
        return tempDir;
    }
    
    public static File createEmptyDirectory(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if(Files.exists(path)) {
            deleteFile(path.toFile());
        }
        Files.createDirectories(path);
        return new File(dirPath);
    }
    
    public static void deleteFile(File file) {
        if(file.isDirectory()) {
            for(File f : file.listFiles()) {
                deleteFile(f);
            }
        }
        file.delete();
    }
    
    public static void createFile(File outputFile, String content) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(content.getBytes());
        }
    }
}
