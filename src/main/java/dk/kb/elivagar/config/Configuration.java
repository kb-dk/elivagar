package dk.kb.elivagar.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.exception.ArgumentCheck;
import dk.kb.elivagar.utils.FileUtils;
import dk.kb.elivagar.utils.LongUtils;
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
 *     <li>xslt_dir: scripts</li>
 *     <li>statistics_dir: /path/to/statistics/dir/</li>
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
 *     <li>transfer: (THIS ELEMENT IS NOT REQUIRED)</li>
 *     <ul>
 *       <li>ingest_ebook_path: /transfer/path/root/ingest/ebook/</li>
 *       <li>update_ebook_content_path: /transfer/path/root/content/ebook/</li>
 *       <li>update_ebook_metadata_path: /transfer/path/root/metadata/ebook/</li>
 *       <li>ingest_audio_path: /transfer/path/root/ingest/audio/</li>
 *       <li>update_audio_content_path: /transfer/path/root/content/audio/</li>
 *       <li>update_audio_metadata_path: /transfer/path/root/metadata/audio/</li>
 *       <li>retain_create_date: -1 // TIME IN MILLIS</li>
 *       <li>retain_modify_date: -1 // TIME IN MILLIS</li>
 *       <li>retain_pub_date: -1 </li>
 *       <li>required_formats:</li>
 *       <ul>
 *         <li>- fits.xml</li>
 *         <li>- mods.xml</li>
 *         <li>- pubhub.xml</li>
 *       </ul>
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
    /** The configuration name for the characterization script file path.*/
    public static final String CONF_XSLT_DIR = "xslt_dir";
    /** The configuration name for the list of formats for the ebooks.*/
    public static final String CONF_EBOOK_FORMATS = "ebook_formats";
    /** The configuration name for the list of formats for the audio books.*/
    public static final String CONF_AUDIO_FORMATS = "audio_formats";
    /** The directory where the output statistics will be placed.*/
    public static final String CONF_STATISTIC_DIR = "statistics_dir";
    
    /** The configuration Aleph element.*/
    public static final String CONF_ALEPH_ROOT = "aleph";
    /** The configuration name for the Aleph url.*/
    public static final String CONF_ALEPH_URL = "aleph_url";
    /** The configuration name for the Aleph base.*/
    public static final String CONF_ALEPH_BASE = "aleph_base";
    /** The configuration name for the directory for temporary storing aleph resources.*/
    public static final String CONF_ALEPH_TEMP_DIR = "temp_dir";
    
    /** The configuration transfer element.*/
    public static final String CONF_TRANSFER_ROOT = "transfer";
    /** The base path for the ingest dir for ebooks.*/
    public static final String CONF_TRANSFER_EBOOK_INGEST_PATH = "ingest_ebook_path";
    /** The base path for update dir for ebook content and technical metadata.*/
    public static final String CONF_TRANSFER_EBOOK_UPDATE_CONTENT_PATH = "update_ebook_content_path";
    /** The base path for update dir for ebook metadata (except technical metadata).*/
    public static final String CONF_TRANSFER_EBOOK_UPDATE_METADATA_PATH = "update_ebook_metadata_path";
    /** The base path for the ingest dir for audio books.*/
    public static final String CONF_TRANSFER_AUDIO_INGEST_PATH = "ingest_audio_path";
    /** The base path for update dir for audio content and technical metadata.*/
    public static final String CONF_TRANSFER_AUDIO_UPDATE_CONTENT_PATH = "update_audio_content_path";
    /** The base path for update dir for audio metadata (except technical metadata).*/
    public static final String CONF_TRANSFER_AUDIO_UPDATE_METADATA_PATH = "update_audio_metadata_path";
    /** The retain interval for the create date, in millis.*/
    public static final String CONF_TRANSFER_RETAIN_CREATE_DATE = "retain_create_date";
    /** The retain interval for the modify data, in millis.*/
    public static final String CONF_TRANSFER_RETAIN_MODIFY_DATE = "retain_modify_date";
    /** The retain interval for the publication date, in millis.*/
    public static final String CONF_TRANSFER_RETAIN_PUBLICATION_DATE = "retain_pub_date";
    /** The list of required formats for initiating the transfer.*/
    public static final String CONF_TRANSFER_REQUIRED_FORMATS = "required_formats";
    
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
    /** The directory containing the XSLT files for transforming the metadata.*/
    protected final File xsltFileDir;
    /** The directory for the output statistics files.*/
    protected final File statisticsDir;
    
    /** The list of formats for the ebooks.*/
    protected List<String> ebookFormats;
    /** The list of formats for the audio books.*/
    protected List<String> audioFormats;
    
    /** The Aleph configuration.*/
    protected final AlephConfiguration alephConfiguration;
    
    /** The transfer configuration. This may be null.*/
    protected TransferConfiguration transferConfiguration;
    
    /**
     * Constructor.
     * @param confMap The YAML map for the configuration.
     * @throws IOException If the output directory does not exist and cannot be created.
     */
    @SuppressWarnings("unchecked")
    public Configuration(Map<String, Object> confMap) throws IOException {
        ArgumentCheck.checkNotNullOrEmpty(confMap, "Map<String, Object> confMap");
        
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_EBOOK_OUTPUT_DIR, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_AUDIO_OUTPUT_DIR, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_LICENSE_KEY, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_EBOOK_FILE_DIR, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_AUDIO_FILE_DIR, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_EBOOK_FILE_DIR, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_AUDIO_FILE_DIR, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_XSLT_DIR, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_STATISTIC_DIR, "confMap");
        ArgumentCheck.checkThatMapContainsKey(confMap, CONF_ALEPH_ROOT, "confMap");
        
        ebookOutputDir = FileUtils.createDirectory((String) confMap.get(CONF_EBOOK_OUTPUT_DIR));
        abookOutputDir = FileUtils.createDirectory((String) confMap.get(CONF_AUDIO_OUTPUT_DIR));
        licenseKey = (String) confMap.get(CONF_LICENSE_KEY);
        ebookFileDir = new File((String) confMap.get(CONF_EBOOK_FILE_DIR));
        audioFileDir = new File((String) confMap.get(CONF_AUDIO_FILE_DIR));
        if(confMap.containsKey(CONF_CHARACTERIZATION_SCRIPT)) {
            scriptFile = new File((String) confMap.get(CONF_CHARACTERIZATION_SCRIPT));
        }
        xsltFileDir = FileUtils.createDirectory((String) confMap.get(CONF_XSLT_DIR));
        statisticsDir = FileUtils.createDirectory((String) confMap.get(CONF_STATISTIC_DIR));
        
        ebookFormats = (List<String>) confMap.get(CONF_EBOOK_FORMATS);
        audioFormats = (List<String>) confMap.get(CONF_AUDIO_FORMATS);
        
        this.alephConfiguration = getAlephConfiguration((Map<String, Object>) confMap.get(CONF_ALEPH_ROOT));
        if(confMap.containsKey(CONF_TRANSFER_ROOT)) {
            this.transferConfiguration = getTransferConfiguration((Map<String, Object>) 
                    confMap.get(CONF_TRANSFER_ROOT));
        } else {
            this.transferConfiguration = null;
        }
    }
    
    /**
     * Instantiates the AlephConfiguration from the given map.
     * @param alephMap The map with the Aleph elements.
     * @return The configuration for retrieving data from Aleph.
     * @throws IOException If the temporary directory cannot be instantiated.
     */
    protected AlephConfiguration getAlephConfiguration(Map<String, Object> alephMap) throws IOException {
        ArgumentCheck.checkThatMapContainsKey(alephMap, CONF_ALEPH_URL, "alephMap");
        ArgumentCheck.checkThatMapContainsKey(alephMap, CONF_ALEPH_BASE, "alephMap");
        ArgumentCheck.checkThatMapContainsKey(alephMap, CONF_ALEPH_TEMP_DIR, "alephMap");
        
        String url = (String) alephMap.get(CONF_ALEPH_URL);
        String base = (String) alephMap.get(CONF_ALEPH_BASE);
        String tempDirPath = (String) alephMap.get(CONF_ALEPH_TEMP_DIR);
        File tempDir = FileUtils.createDirectory(tempDirPath);
        return new AlephConfiguration(url, base, tempDir);
    }
    
    /**
     * Instantiates the TransferConfiguration from the given map.
     * @param transferMap The map with the Transfer elements.
     * @return The transfer configuration.
     * @throws IOException If the directories cannot be instantiated.
     */
    @SuppressWarnings("unchecked")
    protected TransferConfiguration getTransferConfiguration(Map<String, Object> transferMap) throws IOException {
        ArgumentCheck.checkThatMapContainsKey(transferMap, CONF_TRANSFER_EBOOK_INGEST_PATH, "transferMap");
        ArgumentCheck.checkThatMapContainsKey(transferMap, CONF_TRANSFER_EBOOK_UPDATE_CONTENT_PATH, "transferMap");
        ArgumentCheck.checkThatMapContainsKey(transferMap, CONF_TRANSFER_EBOOK_UPDATE_METADATA_PATH, "transferMap");
        ArgumentCheck.checkThatMapContainsKey(transferMap, CONF_TRANSFER_AUDIO_INGEST_PATH, "transferMap");
        ArgumentCheck.checkThatMapContainsKey(transferMap, CONF_TRANSFER_AUDIO_UPDATE_CONTENT_PATH, "transferMap");
        ArgumentCheck.checkThatMapContainsKey(transferMap, CONF_TRANSFER_AUDIO_UPDATE_METADATA_PATH, "transferMap");
        ArgumentCheck.checkThatMapContainsKey(transferMap, CONF_TRANSFER_RETAIN_CREATE_DATE, "transferMap");
        ArgumentCheck.checkThatMapContainsKey(transferMap, CONF_TRANSFER_RETAIN_MODIFY_DATE, "transferMap");
        ArgumentCheck.checkThatMapContainsKey(transferMap, CONF_TRANSFER_RETAIN_PUBLICATION_DATE, "transferMap");
        ArgumentCheck.checkThatMapContainsKey(transferMap, CONF_TRANSFER_REQUIRED_FORMATS, "transferMap");

        String baseIngestEbookPath = (String) transferMap.get(CONF_TRANSFER_EBOOK_INGEST_PATH);
        File baseIngestEbookDir = FileUtils.createDirectory(baseIngestEbookPath);

        String baseContentEbookPath = (String) transferMap.get(CONF_TRANSFER_EBOOK_UPDATE_CONTENT_PATH);
        File baseContentEbookDir = FileUtils.createDirectory(baseContentEbookPath);

        String baseMetadataEbookPath = (String) transferMap.get(CONF_TRANSFER_EBOOK_UPDATE_METADATA_PATH);
        File baseMetadataEbookDir = FileUtils.createDirectory(baseMetadataEbookPath);

        String baseIngestAudioPath = (String) transferMap.get(CONF_TRANSFER_AUDIO_INGEST_PATH);
        File baseIngestAudioDir = FileUtils.createDirectory(baseIngestAudioPath);

        String baseContentAudioPath = (String) transferMap.get(CONF_TRANSFER_AUDIO_UPDATE_CONTENT_PATH);
        File baseContentAudioDir = FileUtils.createDirectory(baseContentAudioPath);

        String baseMetadataAudioPath = (String) transferMap.get(CONF_TRANSFER_AUDIO_UPDATE_METADATA_PATH);
        File baseMetadataAudioDir = FileUtils.createDirectory(baseMetadataAudioPath);

        Long retainCreateDate = LongUtils.getLong(transferMap.get(CONF_TRANSFER_RETAIN_CREATE_DATE));
        Long retainModifyDate = LongUtils.getLong(transferMap.get(CONF_TRANSFER_RETAIN_MODIFY_DATE));
        Long retainPublicationDate = LongUtils.getLong(transferMap.get(CONF_TRANSFER_RETAIN_PUBLICATION_DATE));
        List<String> requiredFormats = (List<String>) transferMap.get(CONF_TRANSFER_REQUIRED_FORMATS);
        return new TransferConfiguration(baseIngestEbookDir, baseContentEbookDir, baseMetadataEbookDir, 
                baseIngestAudioDir, baseContentAudioDir, baseMetadataAudioDir, retainCreateDate, 
                retainModifyDate, retainPublicationDate, requiredFormats);
    }
    
    /** @return The aleph configuration. */
    public AlephConfiguration getAlephConfiguration() {
        return alephConfiguration;
    }
    
    /** @return The transfer configuration. */
    public TransferConfiguration getTransferConfiguration() {
        return transferConfiguration;
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
    
    /** @return The directory with the XSLT files.*/
    public File getXsltFileDir() {
        return xsltFileDir;
    }
    
    /** @return The directory for the output statistics.*/
    public File getStatisticsDir() {
        return statisticsDir;
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
     * Creates a configuration from a file.
     * @param yamlFile The YAML file with the configuration.
     * @return The configuration.
     * @throws IOException If it fails to load, or the configured elements cannot be instantiated.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Configuration createFromYAMLFile(File yamlFile) throws IOException {
        ArgumentCheck.checkExistsNormalFile(yamlFile, "File yamlFile");
        
        log.debug("Loading configuration from file '" + yamlFile.getAbsolutePath() + "'");
        LinkedHashMap<String, LinkedHashMap> map = YamlUtils.loadYamlSettings(yamlFile);
        Map<String, Object> confMap = (Map<String, Object>) map.get(CONF_ELIVAGAR);
        return new Configuration(confMap);
    }
}
