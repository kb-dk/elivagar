package dk.kb.elivagar.transfer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.utils.CalendarUtils;
import dk.kb.elivagar.utils.FileUtils;

/**
 * Class for dealing with the transfer of the packaged data to the pre-ingest area of the preservation repository.
 * 
 * 
 */
public class PreIngestTransfer {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(PreIngestTransfer.class);

    /** The XPATH for finding the publication date in the pubhub metadata file.*/
    protected static final String XPATH_PUBLICATION_DATE = "/Book/PublicationDate/text()";
    /** The date format for the publication date in the pubhub metadata file.*/
    protected static final String DATE_FORMAT_PUBLICATION_DATE = "DD-MM-YYYY";
    
    /** The list of suffixes of the metadata files, which should be updated at the metadata destination.*/
    protected static final List<String> UPDATE_METADATA_SUFFIXES = Arrays.asList(Constants.PUBHUB_METADATA_SUFFIX,
            Constants.MODS_METADATA_SUFFIX);
    /** The list of suffixes of technical metadata files which should be update along the content destination.*/
    protected static final List<String> UPDATE_TECH_METADATA_SUFFIXES = Arrays.asList(
            Constants.EPUBCHECK_METADATA_SUFFIX, Constants.FITS_METADATA_SUFFIX);

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
        transferBook(ebookDir);

        File audioDir = conf.getAudioOutputDir();
        if(audioDir == ebookDir) {
            log.info("Ebook and Audio books have the same directory.");
            return;
        }
        transferBook(audioDir);
    }

    /**
     * Transfer the books, who need to be transfered.
     * @param dir The root directory which contains the book directories.
     */
    protected void transferBook(File dir) {
        try {
            for(File bookDir : dir.listFiles()) {
                String id = bookDir.getName();
                if(!bookDir.isDirectory()) {
                    log.warn("Dir for book '" + id + "' is not a directory ('" + bookDir.getAbsolutePath() + "').");
                    return;
                }

                TransferRegistry register = new TransferRegistry(bookDir);

                if(register.hasBeenIngested()) {
                    updateBook(bookDir, register);
                } else {
                    ingestBook(bookDir, register);
                }
            }
        } catch (IOException e) {
            log.error("Failure while transfering books from '" + dir + "'", e);
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
     */
    protected void updateBook(File bookDir, TransferRegistry register) throws IOException {
        Date updateDate = register.getLatestUpdateDate();
        if(updateDate == null) {
            log.warn("Cannot retrieve neither update date nor ingest date from the registry. ");
            return;
        }
        boolean updated = false;

        // Check for any metadata to update
        List<File> metadataFiles = getNewFilesWithSuffix(bookDir, UPDATE_METADATA_SUFFIXES, updateDate);
        if(!metadataFiles.isEmpty()) {
            log.info("Found new metadata files for update.");
            File updateDir = getUpdateMetadataDir(bookDir);
            copyToUpdateDir(updateDir, metadataFiles);
            updated = true;
        }
        
        // Check for any technical metadata to update
        List<File> techMetadataFiles = getNewFilesWithSuffix(bookDir, UPDATE_TECH_METADATA_SUFFIXES, updateDate);
        if(!techMetadataFiles.isEmpty()) {
            log.info("Found new technical metadata files for update.");
            File updateDir = getUpdateContentDir(bookDir);
            copyToUpdateDir(updateDir, techMetadataFiles);
            updated = true;
        }

        // Check for any content files to update.
        List<File> contentFiles = getNewContentFiles(bookDir, updateDate);
        if(!contentFiles.isEmpty()) {
            log.info("Found new content files for update.");
            File updateDir = getUpdateContentDir(bookDir);
            copyToUpdateDir(updateDir, contentFiles);
            updated = true;            
        }        
        
        if(updated) {
            log.debug("Setting the new update date in the register for book '" + bookDir.getName() + "'");
            register.setUpdateDate(new Date());
        }
    }

    /**
     * Performs the ingest of a book directory.
     * @param bookDir The book directory.
     * @param register The register for the book.
     * @throws IOException If it fails to transfer the book.
     */
    protected void ingestBook(File bookDir, TransferRegistry register) throws IOException {
        if(readyForIngest(bookDir)) {
            File outputDir = new File(conf.getTransferConfiguration().getIngestDir(), bookDir.getName());
            FileUtils.copyDirectory(bookDir, outputDir);
            register.setIngestDate(new Date());
        }
    }

    /**
     * Checks whether a given book directory meets to the requirements for the ingest transfer.
     * It must comply to the requirements:
     * The book has all the needed files.
     * The content file of the book is old enough (creation date and modify date).
     * The publication date from Publizon must not be too new.
     * 
     * @param bookDir The directory 
     * @return
     */
    protected boolean readyForIngest(File bookDir) {
        // Check for required files.
        for(String suffix : conf.getTransferConfiguration().getRequiredFormats()) {
            if(!hasRequiredFile(bookDir, suffix)) {
                log.debug(bookDir.getName() + " does not have a file with suffix: " + suffix);
                return false;
            }
        }

        // check content file date
        try {
            List<Path> contentFiles = getContentFiles(bookDir);
            for(Path f : contentFiles) {
                if(!hasContentFileDate(f)) {
                    log.debug("Content file is too new.");
                    return false;
                }
            }
        } catch (Exception e) {
            log.warn("Issue occured while finding the content files of book " + bookDir.getName(), e);
            return false;
        }

        // Check for the publication date.
        if(conf.getTransferConfiguration().getRetainPublicationDate() > 0) {
            File pubhubMetadataFile = new File(bookDir, bookDir.getName() + Constants.PUBHUB_METADATA_SUFFIX);
            if(!pubhubMetadataFile.isFile()) {
                log.debug(bookDir.getName() + " has no pubhub metadata file.");
                return false;
            }

            Date publicationDate = findPublicationDate(pubhubMetadataFile);
            Date earliestPublicationDate = new Date(System.currentTimeMillis() 
                    - conf.getTransferConfiguration().getRetainPublicationDate());

            if(publicationDate.getTime() > earliestPublicationDate.getTime()) {
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
        for(File f : bookDir.listFiles()) {
            if(f.getName().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Retrieves the directory for updating the metadata for the given book.
     * @param bookDir The current directory of the book.
     * @return The update metadata directory for the book.
     * @throws IOException If it fails to create/retrieve the directory.
     */
    protected File getUpdateMetadataDir(File bookDir) throws IOException {
        File updateDir = new File(conf.getTransferConfiguration().getUpdateMetadataDir(), bookDir.getName());
        return FileUtils.createDirectory(updateDir.getAbsolutePath());
    }
    
    /**
     * Retrieves the directory for updating the content and technical metadata for the given book.
     * @param bookDir The current directory of the book.
     * @return The update content directory for the book.
     * @throws IOException If it fails to create/retrieve the directory.
     */
    protected File getUpdateContentDir(File bookDir) throws IOException {
        File updateDir = new File(conf.getTransferConfiguration().getUpdateContentDir(), bookDir.getName());
        return FileUtils.createDirectory(updateDir.getAbsolutePath());
    }
    
    /**
     * Copies all the files to the update directory.
     * @param updateDir The directory where the files should be copied to.
     * @param files The file to update.
     * @throws IOException If they fail to copy the files.
     */
    protected void copyToUpdateDir(File updateDir, List<File> files) throws IOException {
        for(File fromFile : files) {
            File toFile = new File(updateDir, fromFile.getName());
            FileUtils.copyFileFollowSymbolicLinks(fromFile, toFile);
        }
    }

    /**
     * Check whether the given file has the right dates, both create-date and last-modify-date.
     * 
     * @param path The path to the file to check.
     * @return Whether or not it has a new enough date.
     * @throws IOException If it fails to ready the system timestamps on the file.
     */
    protected boolean hasContentFileDate(Path path) throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
        
        if(conf.getTransferConfiguration().getRetainCreateDate() > 0) {
            long earliestDate = System.currentTimeMillis() - conf.getTransferConfiguration().getRetainCreateDate();
            if(earliestDate < attributes.creationTime().toMillis()) {
                return false;
            }
        }
        if(conf.getTransferConfiguration().getRetainModifyDate() > 0) {
            long earliestDate = System.currentTimeMillis() - conf.getTransferConfiguration().getRetainModifyDate();
            if(earliestDate < attributes.lastModifiedTime().toMillis()) {
                return false;
            }
        }

        return false;
    }

    /**
     * Retrieves the date for the publication date from the pubhub metadata file.
     * @param pubhubMetadata The pubhub metadata file.
     * @return The publication date from the pubhub metadata file. 
     */
    protected Date findPublicationDate(File pubhubMetadata) {
        try {
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            Document doc = builder.parse(pubhubMetadata);
            XPath xpath = xPathFactory.newXPath();
            XPathExpression publicationDatePath = xpath.compile(XPATH_PUBLICATION_DATE);

            String date = publicationDatePath.evaluate(doc);
            return CalendarUtils.getDateFromString(date, DATE_FORMAT_PUBLICATION_DATE);
        } catch (Exception e) {
            throw new IllegalStateException("Could not find publication date in file '" 
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
                res.add(Files.readSymbolicLink(f.toPath()));
            }
        }

        // Find books with Audio book suffixes.
        for(String format : conf.getAudioFormats()) {
            File f = new File(bookDir, bookDir.getName() + "." + format);
            if(f.exists()) {
                res.add(Files.readSymbolicLink(f.toPath()));
            }
        }

        return res;
    }
    
    /**
     * Retrieves the files from the directory with the given suffixes and which have a newer last modified
     * timestamp than a given limit.
     * @param bookDir The directory for the book.
     * @param suffixes The suffixes to find.
     * @param lastModifiedLimit The 
     * @return The list of new files with the given suffixes.
     */
    protected List<File> getNewFilesWithSuffix(File bookDir, List<String> suffixes, Date lastModifiedLimit) {
        List<File> res = new ArrayList<File>();
        for(String suffix : suffixes) {
            for(File f : bookDir.listFiles()) {
                if(f.getName().endsWith(suffix)) {
                    if(f.lastModified() > lastModifiedLimit.getTime()) {
                        res.add(f);
                    }
                }
            }
        }
        return res;
    }
    
    /**
     * Retrieves the new content files from the given book directory.
     * @param bookDir The book directory.
     * @param lastModifiedLimit The lower limit for the last modified timestamp.
     * @return The list of content files, which are newer than the last modified timestamp limit.
     * @throws IOException If it fails to find the content files (through symlinks). 
     */
    protected List<File> getNewContentFiles(File bookDir, Date lastModifiedLimit) throws IOException {
        List<Path> contentFilePaths = getContentFiles(bookDir);
        List<File> res = new ArrayList<File>();
        for(Path p : contentFilePaths) {
            if(p.toFile().lastModified() > lastModifiedLimit.getTime()) {
                res.add(p.toFile());
            }
        }
        return res;
    }
}
