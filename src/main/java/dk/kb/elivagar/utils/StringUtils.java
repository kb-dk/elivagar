package dk.kb.elivagar.utils;

/**
 * Utility class for String operations.
 */
public class StringUtils {
    /**
     * Extracts the suffix for the file at a given path.
     * If no suffix is found, then the empty string is returned.
     * @param path The path to the file (or just the filename).
     * @return The suffix of the file.
     */
    public static String getSuffix(String path) {
        if(path.contains(".")) {
            int end = path.lastIndexOf(".");
            return path.substring(end + 1);
        }
        return "";
    }
    
    /**
     * Extracts the prefix of a filename.
     * @param filename
     * @return
     */
    public static String getPrefix(String filename) {
        if(filename.contains(".")) {
            int end = filename.indexOf(".");
            return filename.substring(0, end);
        }
        return filename;
    }
}
