package dk.kb.elivagar.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.utils.FileUtils;
import dk.kb.elivagar.utils.YamlUtils;

/**
 * Configuration for Elivagar.
 * 
 * It should have the following YAML format:
 * <ul>
 *   <li>elivagar:</li>
 *   <ul>
 *     <li>ebook_output_dir: /path/to/ebook/output/dir/</li>
 *     <li>audio_output_dir: /path/to/audio/output/dir/</li>
 *     <li>ebook_orig_dir: /path/to/orig/book/dir/</li>
 *     <li>audio_orig_dir: /path/to/orig/audio/dir/</li>
 *     <li>license_key: DO_NOT_PUT_LICENSE_IN_GITHUB_FILE</li>
 *     <li>characterization_script: bin/run_fits.sh (optional)</li>
 *     <li>ebook_formats:</li>
 *     <ul>
 *       <li>- pdf</li>
 *       <li>- epub</li>
 *     </ul>
 *     <li>audio_formats:</li>
 *     <ul>
 *       <li>- mp3</li>
 *     </ul>
 *     <li>aleph:</li>
 *     <ul>
 *       <li>aleph_url: $ALEPH_URL</li>
 *       <li>aleph_base: $ALEPH_BASE</li>
 *       <li>temp_dir: $TEMP_DIR</li>
 *     </ul>
 *   </ul>
 * </ul>
 */
public class Configuration {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

    /** The configuration root element for elivagar.*/
    public static final String CONF_ELIVAGAR = "elivagar";
    /** The configuration name for the output directory.*/
    public static final String CONF_EBOOK_OUTPUT_DIR = "ebook_output_dir";
    /** The configuration name for the output directory.*/
    public static final String CONF_AUDIO_OUTPUT_DIR = "audio_output_dir";
    /** The configuration name for the license key.*/
    public static final String CONF_LICENSE_KEY = "license_key";
    /** The configuration name for the ebook file directory.*/
    public static final String CONF_EBOOK_FILE_DIR = "ebook_orig_dir";
    /** The configuration name for the audio book file directory.*/
    public static final String CONF_AUDIO_FILE_DIR = "audio_orig_dir";
    /** The configuration name for the characterization script file path.*/
    public static final String CONF_CHARACTERIZATION_SCRIPT = "characterization_script";
    /** The configuration name for the list of formats for the ebooks.*/
    public static final String CONF_EBOOK_FORMATS = "ebook_formats";
    /** The configuration name for the list of formats for the audio books.*/
    public static final String CONF_AUDIO_FORMATS = "audio_formats";
    
    /** The configuration Aleph element.*/
    public static final String CONF_ALEPH_ROOT = "aleph";
    /** The configuration name for the Aleph url.*/
    public static final String CONF_ALEPH_URL = "aleph_url";
    /** The configuration name for the Aleph base.*/
    public static final String CONF_ALEPH_BASE = "aleph_base";
    /** The configuration name for the directory for temporary storing aleph resources.*/
    public static final String CONF_ALEPH_TEMP_DIR = "temp_dir";
    
    /** The output directory for the ebooks.*/
    protected final File ebookOutputDir;
    /** The output directory for the audio-books.*/
    protected final File abookOutputDir;
    /** The license key for Pubhub.*/
    protected final String licenseKey;
    /** The directory containing the ebook files.*/
    protected final File ebookFileDir;
    /** The directory containing the audio files.*/
    protected final File audioFileDir;
    /** The script for performing the characterization.*/
    protected File scriptFile;
    
    /** The list of formats for the ebooks.*/
    protected List<String> ebookFormats;
    /** The list of formats for the audio books.*/
    protected List<String> audioFormats;
    
    /** The Aleph configuration.*/
    protected final AlephConfiguration alephConfiguration;
    
    /**
     * Constructor.
     * @param confMap The YAML map for the configuration.
     * @throws IOException If the output directory does not exist and cannot be created.
     */
    public Configuration(Map<String, Object> confMap) throws IOException {
        validateThatMapContainsKey(confMap, CONF_EBOOK_OUTPUT_DIR);
        validateThatMapContainsKey(confMap, CONF_AUDIO_OUTPUT_DIR);
        validateThatMapContainsKey(confMap, CONF_LICENSE_KEY);
        validateThatMapContainsKey(confMap, CONF_EBOOK_FILE_DIR);
        validateThatMapContainsKey(confMap, CONF_AUDIO_FILE_DIR);
        validateThatMapContainsKey(confMap, CONF_EBOOK_FILE_DIR);
        validateThatMapContainsKey(confMap, CONF_AUDIO_FILE_DIR);
        validateThatMapContainsKey(confMap, CONF_ALEPH_ROOT);
        
        ebookOutputDir = FileUtils.createDirectory((String) confMap.get(CONF_EBOOK_OUTPUT_DIR));
        abookOutputDir = FileUtils.createDirectory((String) confMap.get(CONF_AUDIO_OUTPUT_DIR));
        licenseKey = (String) confMap.get(CONF_LICENSE_KEY);
        ebookFileDir = new File((String) confMap.get(CONF_EBOOK_FILE_DIR));
        audioFileDir = new File((String) confMap.get(CONF_AUDIO_FILE_DIR));
        if(confMap.containsKey(CONF_CHARACTERIZATION_SCRIPT)) {
            scriptFile = new File((String) confMap.get(CONF_CHARACTERIZATION_SCRIPT));
        }
        
        ebookFormats = (List<String>) confMap.get(CONF_EBOOK_FORMATS);
        audioFormats = (List<String>) confMap.get(CONF_AUDIO_FORMATS);
        
        this.alephConfiguration = getAlephConfiguration((Map<String, Object>) confMap.get(CONF_ALEPH_ROOT));
    }
    
    /**
     * Instantiates the AlephConfiguration from the given map.
     * @param alephMap The map with the Aleph elements.
     * @return The configuration for retrieving data from Aleph.
     * @throws IOException If the temporary directory cannot be instantiated.
     */
    protected AlephConfiguration getAlephConfiguration(Map<String, Object> alephMap) throws IOException {
        validateThatMapContainsKey(alephMap, CONF_ALEPH_URL);
        validateThatMapContainsKey(alephMap, CONF_ALEPH_BASE);
        validateThatMapContainsKey(alephMap, CONF_ALEPH_TEMP_DIR);
        
        String url = (String) alephMap.get(CONF_ALEPH_URL);
        String base = (String) alephMap.get(CONF_ALEPH_BASE);
        String tempDirPath = (String) alephMap.get(CONF_ALEPH_TEMP_DIR);
        File tempDir = FileUtils.createDirectory(tempDirPath);
        return new AlephConfiguration(url, base, tempDir);
    }
    
    /** @return The aleph configuration. */
    public AlephConfiguration getAlephConfiguration() {
        return alephConfiguration;
    }
    
    /** @return The output directory for the ebook directories. */
    public File getEbookOutputDir() {
        return ebookOutputDir;
    }
    
    /** @return The output directory for the audio book directories. */
    public File getAudioOutputDir() {
        return abookOutputDir;
    }
    
    /** @return The license key for pubhub. */
    public String getLicenseKey() {
        return licenseKey;
    }
    
    /** @return The directory with the files for the ebooks. */
    public File getEbookFileDir() {
        return ebookFileDir;
    }
    
    /** @return The directory with the files for the audio books. */
    public File getAudioFileDir() {
        return audioFileDir;
    }
    
    /** @return The file for the characterization. */
    public File getCharacterizationScriptFile() {
        return scriptFile;
    }
    
    /** @return The list of formats for the ebooks.*/
    public List<String> getEbookFormats() {
        return new ArrayList<String>(ebookFormats);
    }
    
    /** @return The list of formats for the audio books.*/
    public List<String> getAudioFormats() {
        return new ArrayList<String>(audioFormats);
    }
    
    /**
     * Validates that the map contains a given key.
     * @param map The map to validate.
     * @param key The key which must be present and have a value in it.
     */
    protected void validateThatMapContainsKey(Map<String, Object> map, String key) {
        if(!map.containsKey(key)) {
            throw new IllegalStateException("The configuration must include '" + key + "'");
        }
        if(map.get(key) == null) {
            throw new IllegalStateException("The configuration field '" + key + "' must have a value.");
        }
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
        Map<String, Object> confMap = (Map<String, Object>) map.get(CONF_ELIVAGAR);
        return new Configuration(confMap);
    }
}
