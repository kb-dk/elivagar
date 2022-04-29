package dk.kb.elivagar.transfer;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.utils.CalendarUtils;
import dk.kb.elivagar.utils.FileUtils;
import dk.pubhub.service.BookTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Class for dealing with the transfer of the packaged data to the pre-ingest area of the preservation repository.
 * 
 * This does two things; transfer new uningested books to the ingest-area, and transfer new files to their designated 
 * update area.
 * Several requirements have to be meet before performing the pre-ingest transfer.
 * And it will first try to update, when it has been ingested.
 * 
 * When updating, the content files and technical metadata files will be copied to the designated update content 
 * directory, whereas the other types of metadata will be copied to the designated update metadata directory.
 * And update will only occur, if the last modified timestamp is newer than the latest update timestamp.
 */
public class PreIngestTransfer {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(PreIngestTransfer.class);

    /** The XPATH for finding the publication date in the pubhub metadata file.*/
    protected static final String XPATH_PUBLICATION_DATE = "/*:Book/*:PublicationDate/text()";
    /** The date format for the publication date in the pubhub metadata file.*/
    protected static final String DATE_FORMAT_PUBLICATION_DATE = "dd-MM-yyyy";
    
    /** The suffix for the transferring dir name.*/
    protected static final String TRANSFERRING_DIR_SUFFIX = "_transfer";
    
    /** 
     * The list of suffixes of the metadata files, which should be updated at the metadata destination.
     * This is currently the MODS metadata and the pubhub metadata.
     */
    protected static final List<String> UPDATE_METADATA_SUFFIXES = Collections.unmodifiableList(Arrays.asList(
            Constants.PUBHUB_METADATA_SUFFIX, Constants.MODS_METADATA_SUFFIX));
    /** 
     * The list of suffixes of technical metadata files which should be update at the content destination.
     * This is currently the FITS metadata and the EpubCheck metadata.
     */
    protected static final List<String> UPDATE_TECH_METADATA_SUFFIXES = Collections.unmodifiableList(Arrays.asList(
            Constants.EPUBCHECK_METADATA_SUFFIX, Constants.FITS_METADATA_SUFFIX));

    /** The configuration.*/
    protected final Configuration conf;

    /** The document builder factory.*/
    protected final DocumentBuilderFactory documentBuilderFactory;
    /** The XPath factory.*/
    protected final XPathFactory xPathFactory;

    /**
     * Constructor.
     * @param conf The configuration.
     */
    public PreIngestTransfer(Configuration conf) {
        this.conf = conf;

        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        xPathFactory = XPathFactory.newInstance();
    }

    /**
     * Perform the transfer of all the books, which are ready for the transfer.
     * This include both transfer to ingest and transfer to update.
     */
    public void transferReadyBooks() {
        File ebookDir = conf.getEbookOutputDir();
        transferBook(ebookDir, BookTypeEnum.EBOG);

        File audioDir = conf.getAudioOutputDir();
        if(audioDir == ebookDir) {
            log.info("Ebook and Audio books have the same directory.");
        } else {
            transferBook(audioDir, BookTypeEnum.LYDBOG);
        }
    }

    /**
     * Transfer the books, who need to be transfered.
     * @param dir The root directory which contains the book directories.
     * @param bookType The type of book.
     */
    protected void transferBook(File dir, BookTypeEnum bookType) {
        try {
            for(File bookDir : FileUtils.getFilesInDirectory(dir)) {
                String id = bookDir.getName();
                if(!bookDir.isDirectory()) {
                    log.warn("Dir for book '" + id + "' is not a directory ('" + bookDir.getAbsolutePath() + "'). "
                            + "Skipping.");
                    continue;
                }

                TransferRegistry register = new TransferRegistry(bookDir);

                if(register.getIngestDate() != null) {
                    validateRegistry(bookDir, register);
                    updateBook(bookDir, register, bookType);
                } else {
                    ingestBook(bookDir, register, bookType);
                }
            }
        } catch (IOException e) {
            log.error("Failure while transfering books from '" + dir + "'", e);
        }
    }

    /**
     * Validates that the registry has any of the books in
     * @param bookDir The directory to validate the registry for.
     * @param register The register.
     * @throws IOException If it fails to validate or update the register.
     */
    protected void validateRegistry(File bookDir, TransferRegistry register) throws IOException {
        List<Path> contentFiles = getContentFiles(bookDir);
        boolean hasAny = false;
        for(Path p : contentFiles) {
            hasAny = hasAny || register.hasFileEntry(p.toFile());
        }
        log.debug("Had any content-files in registry: " + hasAny);
        if(!hasAny) {
            log.warn("Registry for book '" + bookDir.getName() + "' needs to be rebuild.");
            for(Path p : contentFiles) {
                register.setChecksumAndDate(p.toFile());
            }
        }
    }

    /**
     * Update a book.
     * Will try to find any metadata, technical metadata or content files which are newer than the latest
     * update (or the ingest date, if it has not yet been updated).
     * New files will be copied to their designated update directory, and it will be registered 
     * that a new update has occurred.
     * @param bookDir The directory of the book.
     * @param register The register for the book.
     * @param bookType The type of book.
     */
    protected void updateBook(File bookDir, TransferRegistry register, BookTypeEnum bookType) throws IOException {
        log.info("Updating the book: " + bookDir.getName());
        Date updateDate = register.getLatestUpdateDate();
        if(updateDate == null) {
            log.warn("Cannot retrieve neither update date nor ingest date from the registry. "
                    + "Skipping update of book-dir: " + bookDir.getAbsolutePath());
            return;
        }
        boolean updated = false;

        // Check for any metadata to update
        List<File> metadataFiles = getNewFilesWithSuffix(bookDir, UPDATE_METADATA_SUFFIXES, updateDate);
        if(!metadataFiles.isEmpty()) {
            log.info("Found " + metadataFiles.size() + " new metadata files for update.");
            String updateDirPath = getUpdateMetadataDir(bookDir, bookType);
            copyUpdatedFiles(metadataFiles, updateDirPath);
            updated = true;
        }
        
        // Check for any technical metadata to update
        List<File> techMetadataFiles = getNewFilesWithSuffix(bookDir, UPDATE_TECH_METADATA_SUFFIXES, updateDate);
        if(!techMetadataFiles.isEmpty()) {
            log.info("Found " + techMetadataFiles.size() + " new technical metadata files for update.");
            String updateDirPath = getUpdateContentDir(bookDir, bookType);
            copyUpdatedFiles(techMetadataFiles, updateDirPath);
            updated = true;
        }

        // Check for any content files to update.
        List<File> contentFiles = getNewContentFiles(bookDir, register);
        if(!contentFiles.isEmpty()) {
            log.info("Found " + contentFiles.size() + " new content files for update.");
            String updateDirPath = getUpdateContentDir(bookDir, bookType);
            copyUpdatedFiles(contentFiles, updateDirPath);
            register.updateFileEntries(contentFiles);
            updated = true;
        }
        
        if(updated) {
            log.debug("Setting the new update date in the register for book '" + bookDir.getName() + "'");
            register.setUpdateDate(new Date());
        }
    }
    
    /**
     * Move updated files to the destination directory, though through a transfer directory.
     * @param files The files to copy to the destination directory.
     * @param destDirPath The destination directory.
     * @throws IOException If it fails to create directory or copy files.
     */
    protected void copyUpdatedFiles(List<File> files, String destDirPath) throws IOException {
        File transferDir = getTransferDir(destDirPath);
        for(File fromFile : files) {
            File toFile = new File(transferDir, fromFile.getName());
            FileUtils.copyFile(fromFile, toFile);
        }
        File destDir = FileUtils.createDirectory(destDirPath);
        FileUtils.moveDirectory(transferDir, destDir);
    }

    /**
     * Performs the ingest of a book directory.
     * Sets the ingest date along with each content-files checksum and last modified date.
     * @param bookDir The book directory.
     * @param register The register for the book.
     * @param bookType The type of book.
     * @throws IOException If it fails to transfer the book.
     */
    protected void ingestBook(File bookDir, TransferRegistry register, BookTypeEnum bookType) throws IOException {
        log.info("Ingesting the book: " + bookDir.getName());
        if(readyForIngest(bookDir)) {
            String outputDirPath = getIngestDir(bookDir, bookType);
            File transferDir = getTransferDir(outputDirPath);
            FileUtils.copyDirectory(bookDir, transferDir);
            
            File outputDir = FileUtils.createDirectory(outputDirPath);
            FileUtils.moveDirectory(transferDir, outputDir);
            register.setIngestDate(new Date());
            for(Path path : getContentFiles(bookDir)) {
                register.setChecksumAndDate(path.toFile());
            }
        }
    }

    /**
     * Checks whether a given book directory meets the requirements for the ingest transfer.
     * It must comply to the requirements:
     * - The book has all the needed files.
     * - The content file of the book is old enough (creation date and modify date), according to the configuration.
     * - The publication date from Publizon must exist and be older than the configuration requires.
     * 
     * @param bookDir The directory of the book.
     * @return Whether or not the current book directory is ready for the transfer.
     * @throws IOException If it fails to read the files, especially symlinks.
     */
    protected boolean readyForIngest(File bookDir) throws IOException {
        // Check for required files.
        for(String suffix : conf.getTransferConfiguration().getRequiredFormats()) {
            if(!hasRequiredFile(bookDir, suffix)) {
                log.debug(bookDir.getName() + " does not have a file with suffix: " + suffix);
                return false;
            }
        }

        // check content file date
        List<Path> contentFiles = getContentFiles(bookDir);
        for(Path f : contentFiles) {
            if(!hasContentFileDate(f)) {
                log.debug("Content file is too new.");
                return false;
            }
        }

        // Check for the publication date.
        if(conf.getTransferConfiguration().getRetainPublicationDate() >= 0) {
            File pubhubMetadataFile = new File(bookDir, bookDir.getName() + Constants.PUBHUB_METADATA_SUFFIX);
            if(!pubhubMetadataFile.isFile()) {
                log.debug(bookDir.getName() + " has no pubhub metadata file.");
                return false;
            }

            Date publicationDate = findPublicationDate(pubhubMetadataFile);
            Date earliestPublicationDate = new Date(System.currentTimeMillis() 
                    - conf.getTransferConfiguration().getRetainPublicationDate());
            
            if(publicationDate != null && publicationDate.getTime() < earliestPublicationDate.getTime()) {
                log.debug(bookDir.getName() + " has a too new publication date.");
                return false;
            }
        } else {
            log.debug("Not configured to check publication date.");
        }

        return true;
    }

    /**
     * Checks whether the bookDir has a file with the required suffix.
     * @param bookDir The directory with the files for the book.
     * @param suffix The suffix to find.
     * @return Whether or not a file with the given suffix is found in the book dir.
     */
    protected boolean hasRequiredFile(File bookDir, String suffix) {
        for(File f : FileUtils.getFilesInDirectory(bookDir)) {
            if(f.getName().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Retrieves the path to the directory for updating the metadata for the given book.
     * @param bookDir The current directory of the book.
     * @param bookType The type of book.
     * @return The path to the update metadata directory for the book.
     */
    protected String getUpdateMetadataDir(File bookDir, BookTypeEnum bookType) {
        File dir;
        if(bookType == BookTypeEnum.LYDBOG) {
            dir = new File(conf.getTransferConfiguration().getUpdateAudioMetadataDir(), bookDir.getName());
        } else {
            dir = new File(conf.getTransferConfiguration().getUpdateEbookMetadataDir(), bookDir.getName());
        }
        return dir.getAbsolutePath();
    }
    
    /**
     * Retrieves the path to the directory for updating the content and technical metadata for the given book.
     * @param bookDir The current directory of the book.
     * @param bookType The type of book.
     * @return The path to update content directory for the book.
     */
    protected String getUpdateContentDir(File bookDir, BookTypeEnum bookType) {
        File dir;
        if(bookType == BookTypeEnum.LYDBOG) {
            dir = new File(conf.getTransferConfiguration().getUpdateAudioContentDir(), bookDir.getName());
        } else {
            dir = new File(conf.getTransferConfiguration().getUpdateEbookContentDir(), bookDir.getName());
        }
        return dir.getAbsolutePath();
    }
    
    /**
     * Retrieves the path to the ingest directory for the given book.
     * @param bookDir The current directory of the book.
     * @param bookType The type of book.
     * @return The path to the ingest directory for the book.
     */
    protected String getIngestDir(File bookDir, BookTypeEnum bookType) {
        File dir;
        if(bookType == BookTypeEnum.LYDBOG) {
            dir = new File(conf.getTransferConfiguration().getAudioIngestDir(), bookDir.getName());
        } else {
            dir = new File(conf.getTransferConfiguration().getEbookIngestDir(), bookDir.getName());
        }
        return dir.getAbsolutePath();        
    }

    /**
     * Check whether the given file has the correct dates, both create-date and last-modify-date.
     * 
     * @param path The path to the file to check.
     * @return Whether or not it has a new enough date.
     * @throws IOException If it fails to ready the system timestamps on the file.
     */
    protected boolean hasContentFileDate(Path path) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
        
        if(conf.getTransferConfiguration().getRetainCreateDate() >= 0) {
            long earliestDate = System.currentTimeMillis() - conf.getTransferConfiguration().getRetainCreateDate();
            if(earliestDate < attributes.creationTime().toMillis()) {
                return false;
            }
        }
        if(conf.getTransferConfiguration().getRetainModifyDate() >= 0) {
            long earliestDate = System.currentTimeMillis() - conf.getTransferConfiguration().getRetainModifyDate();
            if(earliestDate < attributes.lastModifiedTime().toMillis()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Retrieves the date for the publication date from the pubhub metadata file.
     * @param pubhubMetadata The pubhub metadata file.
     * @return The publication date from the pubhub metadata file. Will return null if it cannot find the 
     * publication date in the xml-file.
     */
    protected Date findPublicationDate(File pubhubMetadata) {
        try {
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            Document doc = builder.parse(pubhubMetadata);
            XPath xpath = xPathFactory.newXPath();
            XPathExpression publicationDatePath = xpath.compile(XPATH_PUBLICATION_DATE);

            String date = publicationDatePath.evaluate(doc);
            if(date.isEmpty()) {
                log.warn("Could not extract the publication date from file '" 
                        + pubhubMetadata.getAbsolutePath() + "'. Returning a null.");
                return null;
            } else {
                return CalendarUtils.getDateFromString(date, DATE_FORMAT_PUBLICATION_DATE);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not aquire publication date in file '" 
                    + pubhubMetadata.getAbsolutePath() + "'", e);
        }
    }

    /**
     * Retrieves the path to the content files for the given book.
     * It will most times only give one file, but occasionally there will be multiple content files.
     * @param bookDir The directory with the files.
     * @return The path to the content files for the book.
     * @throws IOException If it fails to follow the symbolic links to the content files.
     */
    protected List<Path> getContentFiles(File bookDir) throws IOException {
        List<Path> res = new ArrayList<Path>();

        // Find books with Ebook suffixes.
        for(String format : conf.getEbookFormats()) {
            File f = new File(bookDir, bookDir.getName() + "." + format);
            if(f.exists()) {
                res.add(FileUtils.getFileOrSymlinkPath(f));
            }
        }

        // Find books with Audio book suffixes.
        for(String format : conf.getAudioFormats()) {
            File f = new File(bookDir, bookDir.getName() + "." + format);
            if(f.exists()) {
                res.add(FileUtils.getFileOrSymlinkPath(f));
            }
        }

        return res;
    }
    
    /**
     * Retrieves the files from the directory with the given suffixes and which have a newer last modified
     * timestamp than a given limit.
     * @param bookDir The directory for the book.
     * @param suffixes The suffixes to find.
     * @param lastModifiedLimit The earliest last modified timestamp for the files to be considered new.
     * @return The list of new files with the given suffixes.
     */
    protected List<File> getNewFilesWithSuffix(File bookDir, List<String> suffixes, Date lastModifiedLimit) {
        List<File> res = new ArrayList<File>();
        for(File f : FileUtils.getFilesInDirectory(bookDir)) {
            for(String suffix : suffixes) {
                if(f.getName().endsWith(suffix) && f.lastModified() > lastModifiedLimit.getTime()) {
                    res.add(f);
                }
            }
        }
        return res;
    }
    
    /**
     * Retrieves the new content files from the given book directory.
     * If no entries in the registry, then it is not doomed.
     * @param bookDir The book directory.
     * @param register The register with the date and checksums for the files.
     * @return The list of content files, which are newer than the last modified timestamp limit.
     * @throws IOException If it fails to find the content files (through symlinks). 
     */
    protected List<File> getNewContentFiles(File bookDir, TransferRegistry register) throws IOException {
        List<Path> contentFilePaths = getContentFiles(bookDir);
        List<File> res = new ArrayList<File>();
        for(Path p : contentFilePaths) {
            // If no entry, make one and assume no update.
            if(!register.hasFileEntry(p.toFile())) {
                register.setChecksumAndDate(p.toFile());
            } else if(!register.verifyFile(p.toFile())) {
                res.add(p.toFile());
            }
        }
        return res;
    }
    
    /**
     * Retrieves the active transferring directory 
     * @param dirPath The path to the destination directory, when the data transfer is done.
     * @return The transferring directory.
     * @throws IOException If it fails to create the directory.
     */
    protected File getTransferDir(String dirPath) throws IOException {
        return FileUtils.createDirectory(dirPath + TRANSFERRING_DIR_SUFFIX);
    }
}
