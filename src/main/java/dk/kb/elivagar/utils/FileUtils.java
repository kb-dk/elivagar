package dk.kb.elivagar.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
        Path path = Paths.get(dirPath);
        if(!Files.exists(path)) {
            Files.createDirectories(path);
        }
        return new File(dirPath);
    }
}
