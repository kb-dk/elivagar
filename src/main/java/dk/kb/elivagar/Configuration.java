package dk.kb.elivagar;

import java.io.File;
import java.util.Map;

/**
 * Configuration for Elivagar.
 * 
 * It should have the following format:
 * 
 * 
 */
public class Configuration {

    /** The output directory.*/
    protected final File outputDir;
    /** The license key for Pubhub.*/
    protected final String licenseKey;
    /** The directory containing the files.*/
    protected final File fileDir;
    
    /**
     * Constructor.
     * @param confMap The YAML map for the configuration.
     */
    public Configuration(Map<String, String> confMap) {
        outputDir = new File(confMap.get(Constants.CONF_OUTPUT_DIR));
        licenseKey = confMap.get(Constants.CONF_LICENSE_KEY);
        fileDir = new File(confMap.get(Constants.CONF_FILE_DIR));
    }
    
    /**
     * @return The output directory for the book directories.
     */
    public File getOutputDir() {
        return outputDir;
    }
    
    /**
     * @return The license key for pubhub.
     */
    public String getLicenseKey() {
        return licenseKey;
    }
    
    /** 
     * @return The directory with the files for the books.
     */
    public File getFileDir() {
        return fileDir;
    }
    
    /**
     * Constants for the configuration.
     */
    protected interface Constants {
        /** The configuration name for the output directory.*/
        final String CONF_OUTPUT_DIR = "output_dir";
        /** The configuration name for the license key.*/
        final String CONF_LICENSE_KEY = "retrieve_file_script";
        /** The configuration name for the file directory.*/
        final String CONF_FILE_DIR = "book_dir";
    }
}
