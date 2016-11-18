package dk.kb.elivagar.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class for handling standard stream issues.
 */
public final class StreamUtils {
    /** Private constructor to prevent instantiation of this utility class.*/
    private StreamUtils() {}
    
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
        }
    }
    
}
