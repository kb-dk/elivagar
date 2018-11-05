package dk.kb.elivagar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import dk.kb.elivagar.config.AlephConfiguration;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.config.TransferConfiguration;
import dk.kb.elivagar.pubhub.PubhubMetadataRetriever;

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
        Configuration conf = null;
        try {
            conf = Configuration.createFromYAMLFile(confFile);
        } catch(IOException e) {
            System.err.println("Failed to load the configuration");
            e.printStackTrace(System.err);
            System.exit(-1);
        }
        
        boolean failure = false;
        
        failure = validateConfiguration(conf) || failure;
        failure = validatePubhubSetup(conf) || failure;
        
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
     * @param conf The configuration.
     * @return Whether or not the configuration is valid.
     */
    protected static boolean validateConfiguration(Configuration conf) {
        boolean failure = false;
        
        failure = validateReadWriteDirectory(conf.getEbookOutputDir(), "Ebook Output Dir") || failure;
        failure = validateReadWriteDirectory(conf.getAudioOutputDir(), "Audio Output Dir") || failure;
        failure = validateReadOnlyDirectory(conf.getEbookFileDir(), "Ebook Orig Dir") || failure;
        failure = validateReadOnlyDirectory(conf.getAudioFileDir(), "Audio Orig Dir") || failure;
        // TODO: Validate LICENSE ??
        failure = validateExecutableFile(conf.getCharacterizationScriptFile(), "Characterization Script") || failure;
        failure = validateReadOnlyDirectory(conf.getXsltFileDir(), "XSLT Dir") || failure;
        failure = validateReadOnlyDirectory(conf.getStatisticsDir(), "Statistics Dir") || failure;
        // TODO: Validate formats?
        
        failure = verifyAlephConfiguration(conf.getAlephConfiguration()) || failure;
        failure = verifyTransferConfiguration(conf.getTransferConfiguration()) || failure;
        
        return failure;
    }
    
    /**
     * Validates that the pubhub metadata retriever can be instantiated (false means no errors).
     * @param conf The configuration.
     * @return Whether or not it fails to instantiate the pubhub metadata retriever.
     */
    protected static boolean validatePubhubSetup(Configuration conf) {
        try {
            new PubhubMetadataRetriever(conf.getLicenseKey());
        } catch (Exception e) {
            System.err.println("Failed to instantiate pubhub retriever!");
            return true;
        }
        System.out.println("Pubhub Metadata Retrieval operational.");
        return false;
    }
    
    /**
     * Validates the Aleph Configuration (false means no errors).
     * @param alephConf The aleph configuration.
     * @return Whether or not the aleph configuration has errors.
     */
    protected static boolean verifyAlephConfiguration(AlephConfiguration alephConf) {
        boolean failure = false;
        
        failure = validateReadWriteDirectory(alephConf.getTempDir(), "Aleph Temp Dir");
        try {
            HttpClient httpClient = new HttpClient();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            httpClient.retrieveUrlContent(alephConf.getServerUrl(), out);
            System.out.println("Aleph Server Url (" + alephConf.getServerUrl() + ") is responding");
        } catch (Exception e) {
            System.err.println("Aleph Server Url (" + alephConf.getServerUrl() + ") is inaccessible or "
                    + " giving bad responses!");
            e.printStackTrace(System.err);
            failure = true;
        }
        return failure;
    }
    
    /**
     * Validates the transfer configuration (false means no errors).
     * @param transferConf The transfer configuration.
     * @return Whether or not the transfer configuration is valid.
     */
    protected static boolean verifyTransferConfiguration(TransferConfiguration transferConf) {
        boolean failure = false;
        failure = validateReadWriteDirectory(transferConf.getEbookIngestDir(), "Transfer Ingest Ebook Dir") || failure;
        failure = validateReadWriteDirectory(transferConf.getUpdateEbookContentDir(), 
                "Transfer Update Ebook Content Dir") || failure;
        failure = validateReadWriteDirectory(transferConf.getUpdateEbookMetadataDir(), 
                "Transfer Update Ebook Metadata Dir") || failure;
        failure = validateReadWriteDirectory(transferConf.getAudioIngestDir(), "Transfer Ingest Audio Dir") || failure;
        failure = validateReadWriteDirectory(transferConf.getUpdateAudioContentDir(), 
                "Transfer Update Audio Content Dir") || failure;
        failure = validateReadWriteDirectory(transferConf.getUpdateAudioMetadataDir(), 
                "Transfer Update Audio Metadata Dir") || failure;
        // TODO: Validate retain dirs or formats?
        
        return false;
    }
    
    /**
     * Validates a directory including its rights (false means no errors). 
     * @param dir The directory.
     * @param configurationName The name of the related configuration.
     * @return Whether or not the directory is valid.
     */
    protected static boolean validateReadWriteDirectory(File dir, String configurationName) {
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
     * @param dir The directory.
     * @param configurationName The name of the related configuration.
     * @return Whether or not the directory is valid.
     */
    protected static boolean validateReadOnlyDirectory(File dir, String configurationName) {
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
     * @param file The file to validate.
     * @param configurationName The name of the configuration for the file.
     * @return Whether or not is it valid.
     */
    protected static boolean validateExecutableFile(File file, String configurationName) {
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
