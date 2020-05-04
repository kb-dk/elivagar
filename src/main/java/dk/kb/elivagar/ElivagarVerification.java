package dk.kb.elivagar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.pubhub.PubhubMetadataRetriever;
import dk.kb.elivagar.utils.FileUtils;
import dk.kb.elivagar.utils.YamlUtils;

/**
 * Verification instance for Elivagar.
 * 
 * Requires just the configuration file.
 * Will validate that all of the resources required by the configuration are valid and accessible.
 */
public class ElivagarVerification {

    /**
     * Main method.
     * @param args The list of arguments. Only the first is used, which must be path to the configuration file.
     */
    public static void main(String[] args) {
        if(args.length < 1) {
            System.err.println("Needs one argument: the configuration file.");
            System.exit(-1);
        }
        String confPath = args[0];
        File confFile = new File(confPath); 
        if(!confFile.isFile()) {
            System.err.println("The configuration file '" + confFile.getAbsolutePath() + "' is not a valid file.");
            System.exit(-1);
        }
        
        LinkedHashMap<String, LinkedHashMap> map = YamlUtils.loadYamlSettings(confFile);
        Map<String, Object> confMap = (Map<String, Object>) map.get(Configuration.CONF_ELIVAGAR);
        
        boolean failure = false;
        
        failure = validateConfiguration(confMap) || failure;
        failure = validatePubhubSetup(confMap) || failure;
        
        if(failure) {
            System.err.println("Setup is not valid!");
            System.exit(1);
        } else {
            System.out.println("Setup is valid.");
            System.exit(0);
        }
    }
    
    /**
     * Validates the configuration.
     * @param confMap The map for the configuration.
     * @return Whether or not the configuration is valid.
     */
    protected static boolean validateConfiguration(Map<String, Object> confMap) {
        boolean failure = false;
        
        failure = validateReadWriteDirectory((String) confMap.get(Configuration.CONF_EBOOK_OUTPUT_DIR),
                "Ebook Output Dir") || failure;
        failure = validateReadWriteDirectory((String) confMap.get(Configuration.CONF_AUDIO_OUTPUT_DIR),
                "Audio Output Dir") || failure;
        failure = validateReadOnlyDirectory((String) confMap.get(Configuration.CONF_EBOOK_FILE_DIR), 
                "Ebook Orig Dir") || failure;
        failure = validateReadOnlyDirectory((String) confMap.get(Configuration.CONF_AUDIO_FILE_DIR), 
                "Audio Orig Dir") || failure;
        // TODO: Validate LICENSE ??
        failure = validateExecutableFile((String) confMap.get(Configuration.CONF_CHARACTERIZATION_SCRIPT), 
                "Characterization Script") || failure;
        failure = validateReadOnlyDirectory((String) confMap.get(Configuration.CONF_STATISTIC_DIR),
                "Statistics Dir") || failure;
        // TODO: Validate formats?

        failure = verifyAlmaUrl((String) confMap.get(Configuration.CONF_ALMA_SRU_SEARCH)) || failure;
        failure = verifyTransferConfiguration((Map<String, Object>) confMap.get(Configuration.CONF_TRANSFER_ROOT))
                || failure;
        
        return failure;
    }
    
    /**
     * Validates that the pubhub metadata retriever can be instantiated (false means no errors).
     * @param confMap The map for the configuration.
     * @return Whether or not it fails to instantiate the pubhub metadata retriever.
     */
    protected static boolean validatePubhubSetup(Map<String, Object> confMap) {
        try {
            new PubhubMetadataRetriever((String) confMap.get(Configuration.CONF_LICENSE_KEY));
        } catch (Exception e) {
            System.err.println("Failed to instantiate pubhub retriever!");
            return true;
        }
        System.out.println("Pubhub Metadata Retrieval operational.");
        return false;
    }

    /**
     * Verifies the Alma URL.
     * @param serverUrl The url for Alma.
     * @return Whether or not it fails.
     */
    protected static boolean verifyAlmaUrl(String serverUrl) {
        try {
            HttpClient httpClient = new HttpClient();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            httpClient.retrieveUrlContent(serverUrl, out);
            System.out.println("Alma Server Url (" + serverUrl + ") is responding");
        } catch (Exception e) {
            System.err.println("Alma Server Url (" + serverUrl + ") is inaccessible or giving bad responses!");
            e.printStackTrace(System.out);
            return true;
        }
        return false;
    }

    /**
     * Validates the transfer configuration (false means no errors).
     * @param transferMap The transfer configuration.
     * @return Whether or not the transfer configuration is valid.
     */
    protected static boolean verifyTransferConfiguration(Map<String, Object> transferMap) {
        boolean failure = false;
        failure = validateReadWriteDirectory((String) transferMap.get(
                Configuration.CONF_TRANSFER_EBOOK_INGEST_PATH), 
                "Transfer Ingest Ebook Dir") || failure;
        failure = validateReadWriteDirectory((String) transferMap.get(
                Configuration.CONF_TRANSFER_EBOOK_UPDATE_CONTENT_PATH), 
                "Transfer Update Ebook Content Dir") || failure;
        failure = validateReadWriteDirectory((String) transferMap.get(
                Configuration.CONF_TRANSFER_EBOOK_UPDATE_METADATA_PATH), 
                "Transfer Update Ebook Metadata Dir") || failure;
        failure = validateReadWriteDirectory((String) transferMap.get(
                Configuration.CONF_TRANSFER_AUDIO_INGEST_PATH), 
                "Transfer Ingest Audio Dir") || failure;
        failure = validateReadWriteDirectory((String) transferMap.get(
                Configuration.CONF_TRANSFER_AUDIO_UPDATE_CONTENT_PATH), 
                "Transfer Update Audio Content Dir") || failure;
        failure = validateReadWriteDirectory((String) transferMap.get(
                Configuration.CONF_TRANSFER_AUDIO_UPDATE_METADATA_PATH), 
                "Transfer Update Audio Metadata Dir") || failure;
        // TODO: Validate retain dirs or formats?
        
        return false;
    }
    
    /**
     * Validates a directory including its rights (false means no errors). 
     * @param path The path to the directory.
     * @param configurationName The name of the related configuration.
     * @return Whether or not the directory is valid.
     */
    protected static boolean validateReadWriteDirectory(String path, String configurationName) {
        File dir = new File(path);
        if(dir.isDirectory()) {
            System.out.println(configurationName + " (" + dir.getAbsolutePath() + ") is a valid directory.");
        } else {
            System.err.println(configurationName + " (" + dir.getAbsolutePath() + ") is not a valid directory!");
            return true;
        }
        if(dir.canRead() && dir.canWrite()) {
            System.out.println(configurationName + " (" + dir.getAbsolutePath() + ") have the read/write rights.");
        } else {
            System.err.println(configurationName + " (" + dir.getAbsolutePath() + ") does not have the needed "
                    + "read/write rights!");
            return true;
        }
        
        return false;
    }
    
    /**
     * Validates a directory including its rights (false means no errors). 
     * @param path The path to the directory.
     * @param configurationName The name of the related configuration.
     * @return Whether or not the directory is valid.
     */
    protected static boolean validateReadOnlyDirectory(String path, String configurationName) {
        File dir = new File(path);
        if(dir.isDirectory()) {
            System.out.println(configurationName + " (" + dir.getAbsolutePath() + ") is a valid directory.");
        } else {
            System.err.println(configurationName + " (" + dir.getAbsolutePath() + ") is not a valid directory!");
            return true;
        }
        if(dir.canRead()) {
            System.out.println(configurationName + " (" + dir.getAbsolutePath() + ") have the right to read.");
        } else {
            System.err.println(configurationName + " (" + dir.getAbsolutePath() + ") does not have the needed "
                    + "read rights!");
            return true;
        }
        
        return false;
    }
    
    /**
     * Validates that a given file is valid, readable and executable (false means no errors).
     * @param path The path to the file to validate.
     * @param configurationName The name of the configuration for the file.
     * @return Whether or not is it valid.
     */
    protected static boolean validateExecutableFile(String path, String configurationName) {
        File file = new File(path);
        if(file.isFile()) {
            System.out.println(configurationName + " (" + file.getAbsolutePath() + ") is a valid file.");
        } else {
            System.err.println(configurationName + " (" + file.getAbsolutePath() + ") is not a valid file!");
            return true;
        }
        if(file.canRead() && file.canExecute()) {
            System.out.println(configurationName + " (" + file.getAbsolutePath() + ") have the needed "
                    + "read/execute rights.");
        } else {
            System.err.println(configurationName + " (" + file.getAbsolutePath() + ") does not have the needed "
                    + "read/execute rights!");
            return true;
        }
        
        return false;
    }
}
