package dk.kb.elivagar;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    /** The output directory for the ebooks.*/
    protected final File ebookOutputDir;
    /** The output directory for the audio-books.*/
    protected final File abookOutputDir;
    /** The license key for Pubhub.*/
    protected final String licenseKey;
    /** The directory containing the files.*/
    protected final File fileDir;
    /** The script for performing the characterization.*/
    protected File scriptFile;
    
    /**
     * Constructor.
     * @param confMap The YAML map for the configuration.
     * @throws IOException If the output directory does not exist and cannot be created.
     */
    public Configuration(Map<String, String> confMap) throws IOException {
        validateThatMapContainsKey(confMap, Constants.CONF_EBOOK_OUTPUT_DIR);
        ebookOutputDir = FileUtils.createDirectory(confMap.get(Constants.CONF_EBOOK_OUTPUT_DIR));
        abookOutputDir = FileUtils.createDirectory(confMap.get(Constants.CONF_AUDIO_OUTPUT_DIR));
        licenseKey = confMap.get(Constants.CONF_LICENSE_KEY);
        fileDir = new File(confMap.get(Constants.CONF_FILE_DIR));
        if(confMap.containsKey(Constants.CONF_CHARACTERIZATION_SCRIPT)) {
            scriptFile = new File(confMap.get(Constants.CONF_CHARACTERIZATION_SCRIPT));
        }
    }
    
    /**
     * @return The output directory for the ebook directories.
     */
    public File getEbookOutputDir() {
        return ebookOutputDir;
    }
    
    /**
     * @return The output directory for the audio book directories.
     */
    public File getAudioOutputDir() {
        return abookOutputDir;
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
     * @return The file for the characterization.
     */
    public File getCharacterizationScriptFile() {
        return scriptFile;
    }
    
    /**
     * Validates that the map contains a given key.
     * @param map The map to validate.
     * @param key The key which must be present and have a value in it.
     */
    protected void validateThatMapContainsKey(Map<String, String> map, String key) {
        if(!map.containsKey(key)) {
            throw new IllegalStateException("The configuration must include '" + key + "'");
        }
        if(map.get(key).isEmpty()) {
            throw new IllegalStateException("The configuration field '" + key + "' must have a value.");
        }
    }
    
    /**
     * Constants for the configuration.
     */
    protected interface Constants {
        /** The configuration name for the output directory.*/
        String CONF_EBOOK_OUTPUT_DIR = "ebook_output_dir";
        /** The configuration name for the output directory.*/
        String CONF_AUDIO_OUTPUT_DIR = "audio_output_dir";
        /** The configuration name for the license key.*/
        String CONF_LICENSE_KEY = "license_key";
        /** The configuration name for the file directory.*/
        String CONF_FILE_DIR = "book_orig_dir";
        /** The configuration name for the characterization script file path.*/
        String CONF_CHARACTERIZATION_SCRIPT = "characterization_script";
        /** The configuration root element for elivagar.*/
        String CONF_ELIVAGAR = "elivagar";
    }
    
    /**
     * Creates a configuration from a file.
     * @param yamlFile The YAML file with the configuration.
     * @return The configuration.
     * @throws IOException If it fails to load, or the configured elements cannot be instantiated.
     */
    public static Configuration createFromYAMLFile(File yamlFile) throws IOException {
        log.debug("Loading configuration from file '" + yamlFile.getAbsolutePath() + "'");
        LinkedHashMap<String, LinkedHashMap> map = YamlUtils.loadYamlSettings(yamlFile);
        Map<String, String> confMap = (Map<String, String>) map.get(Constants.CONF_ELIVAGAR);
        return new Configuration(confMap);
    }
}
