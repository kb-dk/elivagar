package dk.kb.elivagar.config;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dk.kb.elivagar.exception.ArgumentCheck;

/**
 * Configuration for the transfer of data from Elivagar to Preservica pre-ingest area.
 */
public class TransferConfiguration {
    /** The base directory for the ingest.*/
    protected final File ingestDir;
    /** The base directory for the content files and the technical metadata.*/
    protected final File updateContentDir;
    /** The base directory for the metadata (except technical metadata).*/
    protected final File updateMetadataDir;
    /** The retain interval for the create date, in millis.*/
    protected final Long retainCreateDate;
    /** The retain interval for the modify data, in millis.*/
    protected final Long retainModifyDate;
    /** The retain interval for the publication date, in millis.*/
    protected final Long retainPublicationDate;
    /** The list of required formats for initiating the transfer.*/
    protected final List<String> requiredFormats;
    
    /**
     * Constructor.
     * @param ingestDir The directory for the ingest.
     * @param updateContentDir The base directory for the content files and the technical metadata.
     * @param updateMetadataDir The base directory for the metadata (except technical metadata).
     * @param retainCreateDate The retain interval for the create date, in millis.
     * @param retainModifyDate The retain interval for the modify data, in millis.
     * @param retainPublicationDate The retain interval for the publication date, in millis.
     * @param requiredFormats The list of required formats for initiating the transfer.
     */
    public TransferConfiguration(File ingestDir, File updateContentDir, File updateMetadataDir, Long retainCreateDate, 
            Long retainModifyDate, Long retainPublicationDate, Collection<String> requiredFormats) {
        ArgumentCheck.checkExistsDirectory(ingestDir, "File ingestDir");
        ArgumentCheck.checkExistsDirectory(updateContentDir, "File baseContentDir");
        ArgumentCheck.checkExistsDirectory(updateMetadataDir, "File baseMetadataDir");
        ArgumentCheck.checkNotNull(retainCreateDate, "Long retainCreateDate");
        ArgumentCheck.checkNotNull(retainModifyDate, "Long retainModifyDate");
        ArgumentCheck.checkNotNull(retainPublicationDate, "Long retainPublicationDate");
        ArgumentCheck.checkNotNullOrEmpty(requiredFormats, "Collection<String> requiredFormats");
        
        this.ingestDir = ingestDir;
        this.updateContentDir = updateContentDir;
        this.updateMetadataDir = updateMetadataDir;
        this.retainCreateDate = retainCreateDate;
        this.retainModifyDate = retainModifyDate;
        this.retainPublicationDate = retainPublicationDate;
        this.requiredFormats = new ArrayList<String>(requiredFormats);
    }
    
    /** @return The base directory for the ingest.*/
    public File getIngestDir() {
        return ingestDir;
    }

    /** @return The base directory for the content files and the technical metadata.*/
    public File getUpdateContentDir() {
        return updateContentDir;
    }
    
    /** @return The base directory for the metadata (except technical metadata).*/
    public File getUpdateMetadataDir() {
        return updateMetadataDir;
    }
    
    /** @return The retain interval for the create date, in millis.*/
    public Long getRetainCreateDate() {
        return retainCreateDate;
    }
    
    /** @return The retain interval for the modify data, in millis.*/
    public Long getRetainModifyDate() {
        return retainModifyDate;
    }
    
    /** @return The retain interval for the publication date, in millis.*/
    public Long getRetainPublicationDate() {
        return retainPublicationDate;
    }
    
    /** @return The list of required formats for initiating the transfer.*/
    public List<String> getRequiredFormats() {
        return new ArrayList<String>(requiredFormats);
    }
}
