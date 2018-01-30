package dk.kb.elivagar.pubhub;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.exception.ArgumentCheck;
import dk.kb.elivagar.pubhub.validator.FileSuffixValidator;

/**
 * Method for calculating the statistics for the books retrieved from pubhub. 
 */
public class PubhubStatistics {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(PubhubStatistics.class);

    /** The base directory.*/
    protected final File baseDir;
    /** The validator for which files are considered content-files.*/
    protected final FileSuffixValidator validator;
    
    /** The number of directories traversed. */
    protected long count = 0l;
    /** The number of directories containing both book file and metadata file.*/
    protected long bothDataCount = 0l;
    /** The number of directories containing only metadata file.*/
    protected long onlyMetadata = 0l;
    /** The number of directories containing only book file.*/
    protected long onlyBookFile = 0l;
    /** The number of directories with neither book file or metadata file.*/
    protected long neitherData = 0l;
    
    /**
     * Constructor.
     * @param baseDir The base directory, where each retrieved book has its own subdirectory.
     * @param validator The validator for which files are considered content-files.
     */
    public PubhubStatistics(File baseDir, FileSuffixValidator validator) {
        ArgumentCheck.checkExistsDirectory(baseDir, "File baseDir");
        ArgumentCheck.checkNotNull(validator, "FileSuffixValidator validator");
        this.baseDir = baseDir;
        this.validator = validator;
    }
    
    /**
     * Calculate the statistics.
     * This should be run before retrieving the values.
     */
    public void calculateStatistics() {
        File[] directories = baseDir.listFiles();
        if(directories == null) {
            throw new IllegalStateException("No directories at '" + baseDir.getAbsolutePath() 
                    + "' to make statistics on.");
        } else {
            log.info("Calculating the statistics on the books in directory '" + baseDir.getAbsolutePath()
                    + "'. Expecting '" + directories.length + "' books.");
            for(File dir : directories) {
                calculateStatisticsOnBookDir(dir);
            }
        }
    }
    
    /**
     * Calculates the statistics on a specific directory.
     * It checks whether it has an metadata file (with '.xml' extension), and whether it has a book
     * file (with either '.epub' or 'pdf' extension).
     * @param dir The directory to calculate the statistics upon.
     */
    protected void calculateStatisticsOnBookDir(File dir) {
        File[] files = dir.listFiles();
        if(files == null) {
            log.warn("Expected the directory '" + dir.getAbsolutePath() + "' to be a directory for a book. "
                    + "Continue to next.");
        } else {
            count++;
            boolean hasMetadata = false;
            boolean hasFile = false;
            for(File f : files) {
                String filename = f.getName();
                if(filename.equalsIgnoreCase(dir.getName() + Constants.PUBHUB_METADATA_SUFFIX)) {
                    hasMetadata = true;
                } else if(validator.hasValidSuffix(f)) {
                    hasFile = true;
                }
            }
            if(hasMetadata && hasFile) {
                bothDataCount++;
            } else if(hasMetadata) {
                onlyMetadata++;
            } else if(hasFile) {
                onlyBookFile++;
            } else {
                neitherData++;
            }
        }
    }
    
    /** @return The number of directories containing both book file and metadata file.*/
    public long getBothDataCount() {
        return bothDataCount;
    }
    
    /** @return The number of directories containing only metadata file.*/
    public long getOnlyMetadataCount() {
        return onlyMetadata;
    }
    
    /** @return The number of directories containing only book file.*/
    public long getOnlyBookFileCount() {
        return onlyBookFile;
    }
    
    /** @return The number of directories with neither book file or metadata file.*/
    public long getNeitherDataCount() {
        return neitherData;
    }
    
    /** @return The number of directories traversed. */
    public long getTotalCount() {
        return count;
    }
}
