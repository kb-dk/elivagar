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
    /** The base directory for the ingest for the ebooks.*/
    protected final File ingestEbookDir;
    /** The base directory for the content files and the technical metadata for the ebooks.*/
    protected final File updateEbookContentDir;
    /** The base directory for the metadata (except technical metadata) for the ebooks.*/
    protected final File updateEbookMetadataDir;
    /** The base directory for the ingest for the audio books.*/
    protected final File ingestAudioDir;
    /** The base directory for the content files and the technical metadata for the audio books.*/
    protected final File updateAudioContentDir;
    /** The base directory for the metadata (except technical metadata) for the audio books.*/
    protected final File updateAudioMetadataDir;
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
     * @param ingestEbookDir The directory for the ingest of ebooks.
     * @param updateEbookContentDir The base directory for the content files and the technical metadata of ebooks.
     * @param updateEbookMetadataDir The base directory for the metadata (except technical metadata) of ebooks.
     * @param ingestAudioDir The directory for the ingest of audio books.
     * @param updateAudioContentDir The base directory for the content files and the technical metadata of audio books.
     * @param updateAudioMetadataDir The base directory for the metadata (except technical metadata) of audio books.
     * @param retainCreateDate The retain interval for the create date, in millis.
     * @param retainModifyDate The retain interval for the modify data, in millis.
     * @param retainPublicationDate The retain interval for the publication date, in millis.
     * @param requiredFormats The list of required formats for initiating the transfer.
     */
    public TransferConfiguration(File ingestEbookDir, File updateEbookContentDir, File updateEbookMetadataDir, 
            File ingestAudioDir,  File updateAudioContentDir, File updateAudioMetadataDir, Long retainCreateDate, 
            Long retainModifyDate, Long retainPublicationDate, Collection<String> requiredFormats) {
        ArgumentCheck.checkExistsDirectory(ingestEbookDir, "File ingestEbookDir");
        ArgumentCheck.checkExistsDirectory(updateEbookContentDir, "File baseEbookContentDir");
        ArgumentCheck.checkExistsDirectory(updateEbookMetadataDir, "File baseEbookMetadataDir");
        ArgumentCheck.checkExistsDirectory(ingestAudioDir, "File ingestAudioDir");
        ArgumentCheck.checkExistsDirectory(updateAudioContentDir, "File baseAudioContentDir");
        ArgumentCheck.checkExistsDirectory(updateAudioMetadataDir, "File baseAudioMetadataDir");
        ArgumentCheck.checkNotNull(retainCreateDate, "Long retainCreateDate");
        ArgumentCheck.checkNotNull(retainModifyDate, "Long retainModifyDate");
        ArgumentCheck.checkNotNull(retainPublicationDate, "Long retainPublicationDate");
        ArgumentCheck.checkNotNullOrEmpty(requiredFormats, "Collection<String> requiredFormats");
        
        this.ingestEbookDir = ingestEbookDir;
        this.updateEbookContentDir = updateEbookContentDir;
        this.updateEbookMetadataDir = updateEbookMetadataDir;
        this.ingestAudioDir = ingestAudioDir;
        this.updateAudioContentDir = updateAudioContentDir;
        this.updateAudioMetadataDir = updateAudioMetadataDir;
        this.retainCreateDate = retainCreateDate;
        this.retainModifyDate = retainModifyDate;
        this.retainPublicationDate = retainPublicationDate;
        this.requiredFormats = new ArrayList<String>(requiredFormats);
    }
    
    /** @return The base directory for the ingest for the ebooks.*/
    public File getEbookIngestDir() {
        return ingestEbookDir;
    }

    /** @return The base directory for the content files and the technical metadata for the ebooks.*/
    public File getUpdateEbookContentDir() {
        return updateEbookContentDir;
    }
    
    /** @return The base directory for the metadata (except technical metadata) for the ebooks.*/
    public File getUpdateEbookMetadataDir() {
        return updateEbookMetadataDir;
    }
    
    /** @return The base directory for the ingest for the audio books.*/
    public File getAudioIngestDir() {
        return ingestAudioDir;
    }

    /** @return The base directory for the content files and the technical metadata for the audio books.*/
    public File getUpdateAudioContentDir() {
        return updateAudioContentDir;
    }
    
    /** @return The base directory for the metadata (except technical metadata) for the audio books.*/
    public File getUpdateAudioMetadataDir() {
        return updateAudioMetadataDir;
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
