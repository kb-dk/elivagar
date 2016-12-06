package dk.kb.elivagar;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import dk.kb.elivagar.utils.FileUtils;
import dk.kb.elivagar.utils.YamlUtils;

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
     * @throws IOException If the output directory does not exist and cannot be created.
     */
    public Configuration(Map<String, String> confMap) throws IOException {
        outputDir = FileUtils.createDirectory(confMap.get(Constants.CONF_OUTPUT_DIR));
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
        final String CONF_LICENSE_KEY = "license_key";
        /** The configuration name for the file directory.*/
        final String CONF_FILE_DIR = "book_dir";
        /** The configuration root element for elivagar.*/
        final String CONF_ELIVAGAR = "elivagar";
    }
    
    /**
     * Creates a configuration from a file.
     * @param f The YAML file with the configuration.
     * @return The configuration.
     * @throws IOException If it fails to load, or the configured elements cannot be instantiated.
     */
    public static Configuration createFromYAMLFile(File f) throws IOException {
        LinkedHashMap<String, LinkedHashMap> map = YamlUtils.loadYamlSettings(f);
        Map<String, String> confMap = (Map<String, String>) map.get(Constants.CONF_ELIVAGAR);
        return new Configuration(confMap);
    }
}
