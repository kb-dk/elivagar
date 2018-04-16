package dk.kb.elivagar.statistics;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.exception.ArgumentCheck;

/**
 * Class for calculating the statistics for the books retrieved from pubhub. 
 */
public class ElivagarStatistics {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(ElivagarStatistics.class);
    
    /** The number of directories traversed. */
    protected long totalCount;
    /** The number of new directories traversed.*/
    protected long newDirCount;
    /** The number of non-standard named items found.*/
    protected long numberOfOtherCount;
    /** The mapping between file suffices and the number of files with given suffix.*/
    protected SuffixMap numberOfFiles;
    /** The mapping between file suffixes and the number of new files with the given suffix.*/
    protected SuffixMap numberOfNewFiles;

    /**
     * Constructor.
     */
    public ElivagarStatistics() {
        totalCount = 0l;
        newDirCount = 0l;
        numberOfOtherCount = 0l;
        numberOfFiles = new SuffixMap();
        numberOfNewFiles = new SuffixMap();
    }
    
    /**
     * Traverses the given base directory, containing the books directories (either audio books or ebooks).
     * @param baseDir The base directory.
     * @param date The date in millis from epoch, where everything with a newer date is considered 'new'.
     */
    public void traverseBaseDir(File baseDir, long date) {
        ArgumentCheck.checkNotNull(baseDir, "File baseDir");
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
     * Calculates the statistics on a specific book directory.
     * For each file in the directory, it increments the increments the number of times the suffix
     * of the given file is encountered.
     * It also count the number of new files and directories, and also the number of files, which does
     * not follow the naming scheme ('id'/'id'.suffix).
     * @param dir The directory to calculate the statistics upon.
     * @param date The date in millis since epoch.
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
     * Checks whether a given directory was modified at a newer date than the given date.
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
    
    /** @return The suffixes map for all files.*/
    public SuffixMap getMapOfFileSuffixes() {
        return numberOfFiles;
    }
    
    /** @return The suffixes map for the new files.*/
    public SuffixMap getMapOfNewFileSuffixes() {
        return numberOfNewFiles;
    }
    
    /**
     * Prints the statistics to the print-stream.
     * @param printer The printstream where the statistics will be printed.
     * @param conf The configuration, with the different formats of ebooks and audio books.
     */
    public void printStatistics(PrintStream printer, Configuration conf) {
        printer.println("The number of book directories traversed: " + getTotalCount());
        printer.println(" - Including new directories: " + getNewDirCount());
        
        printer.println("Number of Ebooks: " + getMapOfFileSuffixes().getMultiKeyCount(
                conf.getEbookFormats()));
        for(String ebookFormat : conf.getEbookFormats()) {
            String suffix = "." + ebookFormat;
            printer.println(" - Ebooks in format '" + suffix + "': " 
                    + getMapOfFileSuffixes().getValue(suffix));
        }
        
        printer.println("Number of new Ebooks: " + getMapOfNewFileSuffixes().getMultiKeyCount( 
                conf.getEbookFormats()));
        for(String ebookFormat : conf.getEbookFormats()) {
            String suffix = "." + ebookFormat;
            printer.println(" - new Ebooks in format '" + suffix + "': " 
                    + getMapOfNewFileSuffixes().getValue(suffix));
        }

        printer.println("Number of Audio books: " + getMapOfFileSuffixes().getMultiKeyCount( 
                conf.getAudioFormats()));
        for(String audioFormat : conf.getAudioFormats()) {
            String suffix = "." + audioFormat;
            printer.println(" - Audio books in format '" + suffix + "': " 
                    + getMapOfFileSuffixes().getValue(suffix));
        }
        
        printer.println("Number of new Audio books: " + getMapOfNewFileSuffixes().getMultiKeyCount( 
                conf.getAudioFormats()));
        for(String audioFormat : conf.getAudioFormats()) {
            String suffix = "." + audioFormat;
            printer.println(" - new Audio books in format '" + suffix + "': " 
                    + getMapOfNewFileSuffixes().getValue(suffix));
        }

        printer.println("The number of pubhub metadata records: " 
                + getMapOfFileSuffixes().getValue(Constants.PUBHUB_METADATA_SUFFIX));
        printer.println(" - whereas the number of new pubhub metadata records: " 
                + getMapOfNewFileSuffixes().getValue(Constants.PUBHUB_METADATA_SUFFIX));
        
        printer.println("The number of MODS metadata records transformed from Aleph: " 
                + getMapOfFileSuffixes().getValue(Constants.MODS_METADATA_SUFFIX));
        printer.println(" - whereas the number of new MODS metadata records transformed from Aleph: " 
                + getMapOfNewFileSuffixes().getValue(Constants.MODS_METADATA_SUFFIX));

        printer.println("The number of FITS characterization metadata records: " 
                + getMapOfFileSuffixes().getValue(Constants.FITS_METADATA_SUFFIX));
        printer.println(" - whereas the number of new FITS characterization metadata records: " 
                + getMapOfNewFileSuffixes().getValue(Constants.FITS_METADATA_SUFFIX));

        printer.println("The number of EpubCheck characterization metadata records: " 
                + getMapOfFileSuffixes().getValue(Constants.EPUBCHECK_METADATA_SUFFIX));
        printer.println(" - whereas the number of new EpubCheck characterization metadata records: " 
                + getMapOfNewFileSuffixes().getValue(Constants.EPUBCHECK_METADATA_SUFFIX));
        
        List<String> suffices = new ArrayList<String>();
        suffices.addAll(conf.getAudioFormats());
        for(String suffix : conf.getAudioFormats()) {
            suffices.add("." + suffix);
        }
        suffices.addAll(conf.getEbookFormats());
        for(String suffix : conf.getEbookFormats()) {
            suffices.add("." + suffix);
        }
        suffices.add(Constants.PUBHUB_METADATA_SUFFIX);
        suffices.add(Constants.MODS_METADATA_SUFFIX);
        suffices.add(Constants.FITS_METADATA_SUFFIX);
        suffices.add(Constants.EPUBCHECK_METADATA_SUFFIX);
        
        printer.println("The number of files encountered, which does is not amongst the other counts:"
                + getMapOfFileSuffixes().getCountExcludingKeys(suffices));
        
        printer.println(" - Where the following suffices have not been acounted for: "
                + getMapOfFileSuffixes().getMissingKeys(suffices));
    }
}
