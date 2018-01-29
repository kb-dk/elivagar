package dk.kb.elivagar.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import dk.kb.elivagar.exception.ArgumentCheck;

/**
 * Utility class for dealing with files.
 */
public class FileUtils {

    /**
     * Create or reuse directory
     * @param dirPath Path to directory
     * @return The direcory at the given path.
     * @throws IOException When creating a directory fail
     */
    public static File createDirectory(String dirPath) throws IOException {
        ArgumentCheck.checkNotNullOrEmpty(dirPath, "String dirPath");
        Path path = Paths.get(dirPath);
        if(!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return new File(dirPath);
    }
    
    /**
     * Delete method, which validates that the file is actually not present afterwards.
     * @param f The file to delete.
     */
    public static void deleteFile(File f) {
        if(f == null || !f.exists()) {
            return;
        }
        boolean success = f.delete();
        if(!success) {
            throw new IllegalStateException("Could not delete the file '" + f.getAbsolutePath() + "'");
        }
    }
}
