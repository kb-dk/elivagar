package dk.kb.elivagar.statistics;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.config.Configuration;

/**
 * Method for calculating the statistics for the books retrieved from pubhub. 
 */
public class ElivagarStatistics {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(ElivagarStatistics.class);
    
    /** The number of directories traversed. */
    protected long totalCount;
    /** The number of new directory traversed.*/
    protected long newDirCount;
    /** The number of non-standard named items found.*/
    protected long numberOfOtherCount;
    /** The mapping between file suffices and the number of files with given suffix.*/
    protected SuffixMap numberOfFiles;
    /** The mapping between file suffices and the number of new files with the given suffix.*/
    protected SuffixMap numberOfNewFiles;

    /**
     * Constructor.
     * @param baseDir The base directory, where each retrieved book has its own subdirectory.
     */
    public ElivagarStatistics(Configuration conf) {
        totalCount = 0l;
        newDirCount = 0l;
        numberOfOtherCount = 0l;
        numberOfFiles = new SuffixMap();
        numberOfNewFiles = new SuffixMap();
    }
    
    /**
     * Traverses the given base directory, containing the books directories (either audio books or ebooks).
     * @param baseDir The base directory.
     * @param date The date in millis, where everything with a newer date is considered 'new'.
     */
    public void traverseBaseDir(File baseDir, long date) {
        File[] directories = baseDir.listFiles();
        if(directories == null) {
            throw new IllegalStateException("No directories at '" + baseDir.getAbsolutePath() 
                    + "' to make statistics on.");
        } else {
            log.info("Calculating the statistics on the books in directory '" + baseDir.getAbsolutePath()
                    + "'. Expecting '" + directories.length + "' books.");
            for(File dir : directories) {
                calculateStatisticsOnBookDir(dir, date);
            }
        }
    }
    
    /**
     * Calculates the statistics on a specific directory.
     * It checks whether it has an metadata file (with '.xml' extension), and whether it has a book
     * file (with either '.epub' or 'pdf' extension).
     * @param dir The directory to calculate the statistics upon.
     */
    protected void calculateStatisticsOnBookDir(File dir, long date) {
        File[] files = dir.listFiles();
        if(files == null) {
            log.warn("Expected the directory '" + dir.getAbsolutePath() + "' to be a directory for a book. "
                    + "Continue to next.");
        } else {
            totalCount++;
            checkNewDirectory(dir, date);
            String dirName = dir.getName();
            
            for(File f : files) {
                String filename = f.getName();
                if(filename.startsWith(dirName)) {
                    String suffix = filename.replace(dirName, "");
                    numberOfFiles.addSuffix(suffix);
                    if(f.lastModified() > date) {
                        numberOfNewFiles.addSuffix(suffix);
                    }
                } else {
                    numberOfOtherCount++;
                }
            }
        }
    }
    
    /**
     * Checks whether a given directory modified at a newer date than the given date.
     * If so, then it is added to the list of new directories.
     * @param dir The directory for check.
     * @param date The date to check against.
     */
    protected void checkNewDirectory(File dir, long date) {
        if(dir.lastModified() > date) {
            newDirCount++;
        }
    }
    
    /** @return The number of directories traversed. */
    public long getTotalCount() {
        return totalCount;
    }
    
    /** @return The number of new directories traversed.*/
    public long getNewDirCount() {
        return newDirCount;
    }
    
    /** @return The number of non-standard named files encountered.*/
    public long getNonStandardNamedCount() {
        return numberOfOtherCount;
    }
    
    /** @return The suffices map for all files.*/
    public SuffixMap getMapOfFileSuffices() {
        return numberOfFiles;
    }
    
    /** @return The suffices map for the new files.*/
    public SuffixMap getMapOfNewFileSuffices() {
        return numberOfNewFiles;
    }
}
