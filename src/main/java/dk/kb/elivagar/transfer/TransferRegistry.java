package dk.kb.elivagar.transfer;

import com.google.common.collect.Lists;
import dk.kb.elivagar.exception.ArgumentCheck;
import dk.kb.elivagar.utils.ChecksumUtils;
import dk.kb.elivagar.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * The transfer registry for a given book.
 * Writes a line for when the book has been ingested and every time the book has been updated 
 * (either content or metadata).
 *
 * Whenever a book is ingested or updated, it also writes a line for the checksum of the content-file along with
 * the last modified time-stamp.
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

    /** The line prefix for checksum lines (use default checksum algorithm).*/
    protected static final String LINE_PREFIX_CHECKSUM = ChecksumUtils.MD5_ALGORITHM + ": ";
    /** The line prefix for file date lines.*/
    protected static final String LINE_PREFIX_FILE_DATE = "File date: ";
    /** The separator between the filename and the value.*/
    protected static final String LINE_FILENAME_VALUE_SEPARATOR = "##";

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

        String line = LINE_PREFIX_INGEST + ingestDate.getTime();
        writeLine(line);
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
            for(String line : lines) {
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

        String line = LINE_PREFIX_UPDATE + updateDate.getTime();
        writeLine(line);
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
            return getIngestDate();
        } catch (IOException e) {
            log.error("Could not read the registry file for book '" + bookDir.getName() + "'. Returning a null.", e);
        }
        return null;
    }

    /**
     * Checks whether the registry has an entry for the file.
     * Requires that both the checksum and the date for the file exists.
     * @param f The file whose entry should be checked.
     * @return Whether or not it has an entry.
     */
    public boolean hasFileEntry(File f) {
        if(!hasBeenIngested()) {
            return false;
        }

        String datePrefix = LINE_PREFIX_FILE_DATE + f.getName() + LINE_FILENAME_VALUE_SEPARATOR;
        String date = getLatestEntryWithPrefix(datePrefix);
        if(date == null || date.isEmpty()) {
            return false;
        }

        String checksumPrefix = LINE_PREFIX_CHECKSUM + f.getName() + LINE_FILENAME_VALUE_SEPARATOR;
        String checksum = getLatestEntryWithPrefix(checksumPrefix);
        if(checksum == null || checksum.isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Writes the checksum and the date for the given file.
     * @param f The file to handle.
     */
    public void setChecksumAndDate(File f) {
        try (InputStream in = new FileInputStream(f)) {
            String checksum = ChecksumUtils.generateMD5Checksum(in);
            String line = LINE_PREFIX_CHECKSUM + f.getName() + LINE_FILENAME_VALUE_SEPARATOR + checksum;
            writeLine(line);
        } catch (IOException e) {
            throw new IllegalStateException("Could not calculate the chekcum of file '" + f.getAbsolutePath()
                    + "'.", e);
        }

        String dateLine = LINE_PREFIX_FILE_DATE + f.getName() + LINE_FILENAME_VALUE_SEPARATOR + f.lastModified();
        writeLine(dateLine);
    }

    /**
     * Updates the checksum and date for all the files.
     * @param files The file to have their checksum and date updated at the registry.
     */
    public void updateFileEntries(Collection<File> files) {
        for(File f : files) {
            setChecksumAndDate(f);
        }
    }

    /**
     * Checks whether or not the given file has the right date or checksum.
     * Returns false, if the file has no entry, or if both the last modified date and checksums differ from the latest
     * entry in the registry for the file.
     * Thus false, if it need updating.
     *
     * @param f The file to validate.
     * @return Whether or not the file is the latest with its name in the registry.
     */
    public boolean verifyFile(File f) {
        String datePrefix = LINE_PREFIX_FILE_DATE + f.getName() + LINE_FILENAME_VALUE_SEPARATOR;
        String checksumPrefix = LINE_PREFIX_CHECKSUM + f.getName() + LINE_FILENAME_VALUE_SEPARATOR;

        // first check date.
        String lastModifiedDate = getLatestEntryWithPrefix(datePrefix);
        if(lastModifiedDate == null || lastModifiedDate.isEmpty()) {
            log.debug("No last modified date for the file.");
            return false;
        }
        try {
            Long date = Long.parseLong(lastModifiedDate);
            if (f.lastModified() == date) {
                return true;
            }
            log.debug("Unexpected last modified date: " + f.lastModified() + "! expected: " + date);
        } catch (NumberFormatException e) {
            log.warn("Cannot parse the date. Verification failed, false returned.", e);
            return false;
        }

        // Then check checksum
        String latestChecksum = getLatestEntryWithPrefix(checksumPrefix);
        if(latestChecksum == null || latestChecksum.isEmpty()) {
            log.debug("No last checksum for the file.");
            return false;
        }
        String currentChecksum;
        try (InputStream in = new FileInputStream(f)) {
            currentChecksum = ChecksumUtils.generateMD5Checksum(in);
        } catch (IOException e) {
            log.warn("Could not calculate the checksum. Returns false on verification.", e);
            return false;
        }

        return latestChecksum.equals(currentChecksum);
    }

    /**
     * Retrieves the content of the latest line with the given prefix.
     * @param prefix The prefix to look for.
     * @return The line - after the prefix. Or null if no such line was found.
     */
    protected String getLatestEntryWithPrefix(String prefix) {
        if(!hasBeenIngested()) {
            return null;
        }

        try(InputStream input = new FileInputStream(registryFile)) {
            List<String> lines = StreamUtils.extractInputStreamAsLines(input);
            for(String line : Lists.reverse(lines)) {
                if(line.startsWith(prefix)) {
                    return line.replace(prefix, "");
                }
            }
            log.debug("Could not find an entry in the registry with the prefix: " + prefix);
            return null;
        } catch (IOException e) {
            log.error("Could not read the registry file for book '" + bookDir.getName() + "'. Returning a null.", e);
        }
        return null;
    }

    /**
     * Writes the given line to the registry file.
     * @param line The line to write.
     */
    protected void writeLine(String line) {
        try(OutputStream out = new FileOutputStream(registryFile, APPENDS_TO_FILE)) {
            out.write(line.getBytes(StandardCharsets.UTF_8));
            out.write("\n".getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch(IOException e) {
            throw new IllegalStateException("Error when trying to write the line (" + line + ") " + "for the book: "
                    + bookDir.getName(), e);
        }
    }
}
