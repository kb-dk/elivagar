package dk.kb.elivagar.testutils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TestFileUtils {

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
}
