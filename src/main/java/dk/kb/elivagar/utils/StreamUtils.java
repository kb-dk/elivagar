package dk.kb.elivagar.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for handling standard stream issues.
 */
public final class StreamUtils {
    /** The default buffer size. 32 kb. */
    private static final int IO_BUFFER_SIZE = 32*1024;
    
    /**
     * Utility function for moving data from an inputstream to an outputstream.
     * TODO move to a utility class.
     * 
     * @param in The input stream to copy to the output stream.
     * @param out The output stream where the input stream should be copied.
     * @throws IOException If anything problems occur with transferring the 
     * data between the streams.
     */
    public static void copyInputStreamToOutputStream(InputStream in,
            OutputStream out) throws IOException {
        if(in == null || out == null) {
            throw new IllegalArgumentException("InputStream: " + in 
                    + ", OutputStream: " + out);
        }
        
        try {
            byte[] buf = new byte[IO_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buf)) != -1) {
                out.write(buf, 0, bytesRead);
            }
            out.flush();
        } finally {
            in.close();
            out.close();
        }
    }
    

    /**
     * Extracts the content of an input stream as lines.
     * @param is The input stream.
     * @return A list of all the lines from the inputstream.
     * @throws IOException If it fails.
     */
    public static List<String> extractInputStreamAsLines(InputStream is) throws IOException {
        List<String> res = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while((line = br.readLine()) != null) {
                res.add(line);
            }
        }
        return res;
    }
    
    /**
     * Extracts the content of an input stream as a string.
     * @param is The input stream to extract.
     * @return The string of the input stream.
     * @throws IOException If the input stream cannot be read.
     */
    public static String extractInputStreamAsString(InputStream is) throws IOException {
        StringBuffer res = new StringBuffer();
        for(String s : extractInputStreamAsLines(is)) {
            res.append(s + "\n");
        }
        
        return res.toString();
    }
}
