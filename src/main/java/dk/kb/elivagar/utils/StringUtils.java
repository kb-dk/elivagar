package dk.kb.elivagar.utils;

import dk.kb.elivagar.exception.ArgumentCheck;

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
        ArgumentCheck.checkNotNullOrEmpty(path, "String path");
        if(path.contains(".")) {
            int end = path.lastIndexOf(".");
            return path.substring(end + 1);
        }
        return "";
    }
    
    /**
     * Extracts the prefix of a filename.
     * Note, in opposition to the other function, this requires the exact name of the file.
     * It cannot deal with the whole path.
     * @param filename The name of the file.
     * @return The prefix. Possibly the whole filename, if the file does not have a suffix.
     */
    public static String getPrefix(String filename) {
        ArgumentCheck.checkNotNullOrEmpty(filename, "String filename");
        if(filename.contains(".")) {
            int end = filename.indexOf(".");
            return filename.substring(0, end);
        }
        return filename;
    }
}
