package dk.kb.elivagar.utils;

import java.io.InputStream;
import java.security.MessageDigest;

/**
 * Utility class for dealing with checksums.
 */
public class ChecksumUtils {
    /** Name of the MD5 algorithm.*/
    public static final String MD5_ALGORITHM = "MD5";
    
    /** The maximal size of the byte array for digest.*/
    private static final int BYTE_ARRAY_SIZE_FOR_DIGEST = 4096;    
    
    /**
     * Calculates a checksum of a inputstream based on a MD5 checksum-algorithm.
     * 
     * @param content The inputstream for the data to calculate the checksum of.
     * @return The HMAC calculated checksum in hexadecimal.
     */
    public static String generateMD5Checksum(InputStream content) {
        byte[] digest = null;
        digest = calculateChecksumWithMessageDigest(content, MD5_ALGORITHM);

        return Base16Utils.decodeBase16(digest);
    }
    
    /**
     * Calculation of the checksum for a given input stream through the use of message digestion on the checksum 
     * algorithm.
     * 
     * @param content The input stream with the content to calculate the checksum of.
     * @param algorithm The checksum algorithm to calculate with.
     * @return The calculated checksum.
     */
    private static byte[] calculateChecksumWithMessageDigest(InputStream content, String algorithm) {
        byte[] bytes = new byte[BYTE_ARRAY_SIZE_FOR_DIGEST];
        int bytesRead;
        
        try {
            MessageDigest digester = MessageDigest.getInstance(algorithm);
            while ((bytesRead = content.read(bytes)) > 0) {
                digester.update(bytes, 0, bytesRead);
            }
            return digester.digest();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot calculate the checksum.", e);
        }
    }
}
