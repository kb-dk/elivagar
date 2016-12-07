package dk.kb.elivagar.pubhub;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.Constants;

/**
 * Method for calculating the statistics for the books retrieved from pubhub. 
 */
public class PubhubStatistics {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(PubhubStatistics.class);

    /** The base directory.*/
    protected final File baseDir;
    
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
     */
    public PubhubStatistics(File baseDir) {
        this.baseDir = baseDir;
    }
    
    /**
     * Calculate the statistics.
     * This should be run before retrieving the values.
     */
    public void calculateStatistics() {
        log.info("Calculating the statistics on the books. Expecting '" + baseDir.list().length + "' books.");
        for(File dir : baseDir.listFiles()) {
            if(dir.isDirectory()) {
                calculateStatisticsOnBookDir(dir);
            } else {
                log.warn("Expected the file '" + dir.getAbsolutePath() + "' to be a directory for a book. Continues.");
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
        count++;
        boolean hasMetadata = false;
        boolean hasFile = false;
        for(File f : dir.listFiles()) {
            String filename = f.getName().toLowerCase();
            if(filename.endsWith(Constants.XML_SUFFIX)) {
                // TODO perhaps only, if the file has the name ${book-id}.xml, where book-id is the dir-name.
                hasMetadata = true;
            } else if(filename.endsWith(Constants.EPUD_SUFFIX) || filename.endsWith(Constants.PDF_SUFFIX)) {
                hasFile = true;
            }
        }
        if(hasMetadata && hasFile) {
            bothDataCount++;
        } else if(hasMetadata && !hasFile) {
            onlyMetadata++;
        } else if(!hasMetadata && hasFile) {
            onlyBookFile++;
        } else {
            neitherData++;
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
