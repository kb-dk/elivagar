package dk.kb.elivagar.config;

import dk.kb.elivagar.exception.ArgumentCheck;

import java.io.File;

/**
 * Configuration for Alma.
 */
public class AlmaConfiguration {
    /** The base URL for base URL for the Alma SRU service.
     * It must end with '?', or having it being added.
     * */
    protected final String sruBaseUrl;
    /** The other fixed parameters for the SRU search service.*/
    protected final String fixedParameters;

    /**
     * Constructor.
     * @param sruBaseUrl The URL for the aleph server.
     */
    public AlmaConfiguration(String sruBaseUrl, String fixedParameters) {
        ArgumentCheck.checkNotNullOrEmpty(sruBaseUrl, "String sruBaseUrl");
        ArgumentCheck.checkNotNullOrEmpty(fixedParameters, "String fixedParameters");

        this.sruBaseUrl = sruBaseUrl;
        this.fixedParameters = fixedParameters;
    }
    
    /**
     * @return The URL for the SRU Alma service.
     */
    public String getSruBaseUrl() {
        return sruBaseUrl;
    }

    /**
     * @return The fixed parameters for the SRU Alma service.
     */
    public String getFixedParameters() {
        return fixedParameters;
    }

}
