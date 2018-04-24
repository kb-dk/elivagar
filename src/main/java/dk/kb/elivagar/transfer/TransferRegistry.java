package dk.kb.elivagar.transfer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import dk.kb.elivagar.exception.ArgumentCheck;
import dk.kb.elivagar.utils.StreamUtils;

/**
 * The transfer registry for a given book.
 * Writes a line for when the book has been ingested and every time the book has been updated 
 * (either content or metadata).
 */
public class TransferRegistry {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(TransferRegistry.class);

    /** The name of the registry file.*/
    protected static final String REGISTRY_NAME = "transfer_registry.txt";
    /** The boolean for appending to a file.*/
    protected static final Boolean APPENDS_TO_FILE = true;

    /** The line prefix for the ingest date.*/
    protected static final String LINE_PREFIX_INGEST = "ingest date: ";
    /** The line prefix for the update date.*/
    protected static final String LINE_PREFIX_UPDATE = "update date: ";

    /** The directory for the book.*/
    protected final File bookDir;
    /** The transfer registry file for the book.*/
    protected final File registryFile;

    /**
     * Constructor.
     * @param bookDir The directory for the book.
     */
    public TransferRegistry(File bookDir) {
        ArgumentCheck.checkExistsDirectory(bookDir, "File bookDir");
        this.bookDir = bookDir;
        this.registryFile = new File(bookDir, REGISTRY_NAME);
    }

    /**
     * @return Whether or not the book has been ingested.
     */
    public boolean hasBeenIngested() {
        return registryFile.exists();
    }

    /**
     * Sets the ingest date for the book.
     * @param ingestDate The ingest date to register.
     */
    public void setIngestDate(Date ingestDate) {
        ArgumentCheck.checkNotNull(ingestDate, "Date ingestDate");

        try(OutputStream out = new FileOutputStream(registryFile, APPENDS_TO_FILE)) {
            String line = LINE_PREFIX_INGEST + ingestDate.getTime() + "\n";
            out.write(line.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch(IOException e) {
            throw new IllegalStateException("Error when trying write the ingest date (" + ingestDate + ") "
                    + "for the book: " + bookDir.getName(), e);
        }
    }

    /**
     * Retrieves the date for the ingest.
     * The lines of the registry has to be read in reverse, since every action is added.
     * @return The date for the ingest. Or null if no ingest date can be found in the registry.
     */
    public Date getIngestDate() {
        if(!hasBeenIngested()) {
            return null;
        }

        try(InputStream input = new FileInputStream(registryFile)) {
            List<String> lines = StreamUtils.extractInputStreamAsLines(input);
            for(String line : Lists.reverse(lines)) {
                if(line.startsWith(LINE_PREFIX_INGEST)) {
                    Long date = Long.parseLong(line.replace(LINE_PREFIX_INGEST, ""));
                    return new Date(date);
                }
            }
            log.warn("Could not find a ingest date for book '" + bookDir.getName() + "'. Returning a null.");
        } catch (IOException e) {
            log.error("Could not read the registry file for book '" + bookDir.getName() + "'. Returning a null.", e);
        }
        return null;
    }

    /**
     * Adds an update date to the registry.
     * @param updateDate The date for the update to be added to the registry.
     */
    public void setUpdateDate(Date updateDate) {
        ArgumentCheck.checkNotNull(updateDate, "Date updateDate");

        try(OutputStream out = new FileOutputStream(registryFile, APPENDS_TO_FILE)) {
            String line = LINE_PREFIX_UPDATE + updateDate.getTime() + "\n";
            out.write(line.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch(IOException e) {
            throw new IllegalStateException("Error when trying write the update date (" + updateDate + ") "
                    + "for the book: " + bookDir.getName(), e);
        }
    }

    /**
     * Retrieves the date for the update, or null if it has not yet been updated.
     * The lines of the registry has to be read in reverse, since every action is appended in chronological order.
     * @return The date for the latest update. Or null if no update date can be found in the registry.
     */
    public Date getLatestUpdateDate() {
        if(!hasBeenIngested()) {
            return null;
        }

        try(InputStream input = new FileInputStream(registryFile)) {
            List<String> lines = StreamUtils.extractInputStreamAsLines(input);
            for(String line : Lists.reverse(lines)) {
                if(line.startsWith(LINE_PREFIX_UPDATE)) {
                    Long date = Long.parseLong(line.replace(LINE_PREFIX_UPDATE, ""));
                    return new Date(date);
                }
            }
            log.debug("Could not find a update date for book '" + bookDir.getName() + "'. "
                    + "Has possibly not been updated yet. Trying to find the ingest date.");
            Date ingestDate = getIngestDate();
            return ingestDate;
        } catch (IOException e) {
            log.error("Could not read the registry file for book '" + bookDir.getName() + "'. Returning a null.", e);
        }
        return null;
    }
}
