package dk.kb.elivagar.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

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
    
    /**
     * Copies from one directory to another.
     * @param from The from directory.
     * @param to The to directory.
     * @throws IOException If it fails to copy.
     */
    public static void copyDirectory(File from, File to) throws IOException {
        ArgumentCheck.checkExistsDirectory(from, "File from");
        if(to.exists()) {
            ArgumentCheck.checkExistsDirectory(to, "File to");
        } else {
            createDirectory(to.getAbsolutePath());
        }
        
        for(File f : getFilesInDirectory(from)) {
            copyFileFollowSymbolicLinks(f, new File(to, f.getName()));
        }
    }

    /**
     * Copies a file or the content of its symbolic link to a given destination.
     * @param fromFile The from file.
     * @param toFile The to file.
     * @throws IOException If it fails to handle the copy or symbolic links.
     */
    public static void copyFileFollowSymbolicLinks(File fromFile, File toFile) throws IOException {
        ArgumentCheck.checkExistsNormalFile(fromFile, "File from");
        Path toPath = toFile.toPath();
        Path fromPath;
        if(Files.isSymbolicLink(fromFile.toPath())) {
            fromPath = Files.readSymbolicLink(fromFile.toPath());
        } else {
            fromPath = fromFile.toPath();
        }
        Files.copy(fromPath, toPath);
    }
    
    /**
     * Retrieves the list of files from a directory, with the notorious null-pointer check.
     * @param dir The directory to retrieve the files from.
     * @return The collection of files in a directory.
     */
    public static Collection<File> getFilesInDirectory(File dir) {
        ArgumentCheck.checkExistsDirectory(dir, "File dir");
        if(dir.listFiles() == null) {
            throw new IllegalStateException("Unable to obtain the list of files from directory "
                    + dir.getAbsolutePath());
        } else {
            return Arrays.asList(dir.listFiles());
        }
    }
    
    /**
     * Retrieves the path for the given file or the path which a symbolic link points to.
     * @param f The file to retrieve the path for.
     * @return The path for the file or the path of the symbolic link.
     * @throws IOException If it fails to ready the symbolic link.
     */
    public static Path getFileOrSymlinkPath(File f) throws IOException {
        ArgumentCheck.checkExistsNormalFile(f, "File f");
        if(Files.isSymbolicLink(f.toPath())) {
            return Files.readSymbolicLink(f.toPath());
        } else {
            return f.toPath();
        }
    }
}
