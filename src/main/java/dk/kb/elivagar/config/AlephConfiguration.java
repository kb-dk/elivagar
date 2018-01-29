package dk.kb.elivagar.config;

import java.io.File;

import dk.kb.elivagar.exception.ArgumentCheck;

/**
 * Configuration for Aleph.
 */
public class AlephConfiguration {
    /** The base URL for the Aleph X-Service. It must end with a '?' to ensure for creating the requests.*/
    protected final String serverUrl;
    /** The value of the Aleph base.*/
    protected final String baseValue;
    /** The directory for dealing with temporary resources from Aleph.*/
    protected final File tempDir;
    
    /**
     * Constructor.
     * @param serverUrl The URL for the aleph server.
     * @param base The base.
     * @param tempDir The directory for the temporary aleph resources.
     */
    public AlephConfiguration(String serverUrl, String base, File tempDir) {
        ArgumentCheck.checkNotNullOrEmpty(serverUrl, "String serverUrl");
        ArgumentCheck.checkNotNullOrEmpty(base, "String base");
        ArgumentCheck.checkExistsDirectory(tempDir, "File tempDir");
        
        if(serverUrl.endsWith("?")) {
            this.serverUrl = serverUrl;
        } else {
            this.serverUrl = serverUrl + "?";
        }
        
        this.baseValue = base;
        this.tempDir = tempDir;
    }
    
    /**
     * @return The URL for the aleph server.
     */
    public String getServerUrl() {
        return serverUrl;
    }
    
    /**
     * @return The aleph base.
     */
    public String getBase() {
        return baseValue;
    }
    
    /**
     * @return The directory for the temporary Aleph resources.
     */
    public File getTempDir() {
        return tempDir;
    }
}
