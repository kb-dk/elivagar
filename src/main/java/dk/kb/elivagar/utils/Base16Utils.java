package dk.kb.elivagar.utils;

import dk.kb.elivagar.exception.ArgumentCheck;

/**
 * Utility class for handling encoding and decoding of base16 bytes.
 */
public class Base16Utils {
    /**
     * Decodes a base16 encoded byte set into a human readable string.
     * @param data The data to decode.
     * @return The decoded data, or null if a null is given.
     */
    public static String decodeBase16(byte[] data) {
        if(data == null) {
            return null;
        }
        
        StringBuffer sb = new StringBuffer(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            int v = data[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString();
    }
    
    /**
     * Encoding a hex string to base16.
     * 
     * @param hexString The string to encode to base16.
     * @return The string encoded to base16.
     */
    public static byte[] encodeBase16(String hexString) {
        ArgumentCheck.checkNotNullOrEmpty(hexString, "String hexString");
        ArgumentCheck.checkTrue((hexString.length() % 2) == 0, "String hexString, '" + hexString 
                + "', must be an even number of characters.");
        
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                 + Character.digit(hexString.charAt(i+1), 16));
        }
        return data;
    }
}
