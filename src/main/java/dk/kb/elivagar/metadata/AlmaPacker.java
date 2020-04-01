package dk.kb.elivagar.metadata;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.exception.ArgumentCheck;
import dk.kb.elivagar.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The Alma packer.
 * It will iterate through all the books - both E-books and Audio books.
 * The books which does not have a MODS metadata file in their package directory, will have the metadata retrieved.
 * This is done by extracting the ISBN number from the Publizon metadata file, then use this ISBN to retrieve the 
 * MODS from Alma.
 * This MODS metadata file is then placed in the book's package directory.
 */
public class AlmaPacker {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(AlmaPacker.class);

    /** The XPATH for extracting the Identifier from the pubhub Book xml file.*/
    protected static final String XPATH_FIND_IDENTIFIER = "/*:Book/*:Identifier/text()";
    /** The XPATH for extracting the IdentifierType from the pubhub Book xml file.*/
    protected static final String XPATH_FIND_IDENTIFIER_TYPE = "/*:Book/*:IdentifierType/text()";
    
    /** The configuration.*/
    protected final Configuration conf;
    /** The metadata retriever for the Alma metadata.*/
    protected final AlmaMetadataRetriever almaMetadataRetriever;
    /** The metadata validator.*/
    protected final MetadataValidator validator;

    /** The document builder factory.*/
    protected final DocumentBuilderFactory factory;
    /** The XPath factory.*/
    protected final XPathFactory xPathfactory;
    
    /**
     * Constructor.
     * @param conf The configuration.
     * @param almaMetadataRetriever The retriever of Alma metadata.
     */
    public AlmaPacker(Configuration conf, AlmaMetadataRetriever almaMetadataRetriever) {
        ArgumentCheck.checkNotNull(conf, "Configuration conf");
        ArgumentCheck.checkNotNull(almaMetadataRetriever, "AlmaMetadataRetriever almaMetadataRetriever");
        this.conf = conf;
        this.almaMetadataRetriever = almaMetadataRetriever;
        this.factory = DocumentBuilderFactory.newInstance();
        this.xPathfactory = XPathFactory.newInstance();
        this.validator = new MetadataValidator();
    }
    
    /**
     * Pack Alma metadata for all books; both E-books and Audio books.
     * Will not retrieve the metadata, if it has already been retrieved.
     * 
     * If the e-book package base directory and the audio book package base directory are the same, then they
     * are only traversed once.
     */
    public void packAlmaMetadataForBooks() {
        traverseBooksInFolder(conf.getEbookOutputDir());
        if(conf.getEbookOutputDir().getAbsolutePath().equals(conf.getAudioOutputDir().getAbsolutePath())) {
            log.debug("Ebooks and Audio books have same base-dir.");
        } else {
            traverseBooksInFolder(conf.getAudioOutputDir());            
        }
    }
    
    /**
     * Traverses the books in the base directory to retrieve and package the Alma metadata.
     * @param baseBookDir The base directory for the books (either E-books or Audio books).
     */
    protected void traverseBooksInFolder(File baseBookDir) {
        File[] files = baseBookDir.listFiles();
        if(files == null) {
            log.warn("No books to retrieve and transform Alma metadata for within the directory: "
                    + baseBookDir.getAbsolutePath());
        } else {
            for(File f : files) {
                packageMetadataForBook(f);
            }
        }
    }
    
    /**
     * Packages the metadata for a given book.
     * It will not do anything, if a MODS record already exists, or if it fails to extract the ISBN.
     * Otherwise it retrieves the Alma metadata in MODS.
     * @param dir The book package directory, where the Publizon metadata already is placed.
     */
    protected void packageMetadataForBook(File dir) {
        try {
            File modsMetadata = new File(dir, dir.getName() + Constants.MODS_METADATA_SUFFIX);
            if(modsMetadata.exists()) {
                log.trace("Already retrieved MODS file.");
                return;
            }
            String isbn = getIsbn(dir);
            if(isbn == null) {
                log.debug("Could not retrieve a ISBN or GTIN from '" + dir.getAbsolutePath() + "'.");
                return;
            }

            getAlmaMetadata(isbn, modsMetadata);
        } catch (Exception e) {
            log.info("Non-critical failure while trying to retrieve the Alma metadata for the book directory '"
                    + dir.getAbsolutePath() + "'", e);
        }
    }
    
    /**
     * Checks whether an XML file is valid, and if it is not, then move it to 'XXX.error'.
     * @param xmlFile The XML file to validate.
     */
    protected void handleXmlValidity(File xmlFile) throws IOException {
        if(validator.isValid(xmlFile)) {
            log.debug("Valid MODS!");
        } else {
            log.warn("Invalid MODS! Moving it to error");
            File errorFile = new File(xmlFile.getAbsolutePath() + Constants.ERROR_SUFFIX);
            FileUtils.moveFile(xmlFile, errorFile);
        }
    }
    
    /**
     * Retrieves the ISBN number for a book.
     * Uses the directory for the packaged book to locate the already retrieved Publizon metadata file,
     * and then extracts the ISBN number from that file.
     * Will return null if it fails to get the Publizon metadata file, if it fails to extract the ISBN, 
     * or if the identifier is not of the type ISBN.
     * @param dir The directory for a packaged book, which should contain the publizon metadata.
     * @return The ISBN number, or null if no ISBN could be found.
     */
    protected String getIsbn(File dir) {
        File pubhubMetadataFile = new File(dir, dir.getName() + Constants.PUBHUB_METADATA_SUFFIX);
        if(!pubhubMetadataFile.isFile()) {
            log.warn("No pubhub metadata file for '" + dir.getName() + "', thus cannot extract ISBN. "
                    + "Returning a null.");
            return null;            
        }
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(pubhubMetadataFile);
            XPath xpath = xPathfactory.newXPath();
            XPathExpression identifierXpath = xpath.compile(XPATH_FIND_IDENTIFIER);
            XPathExpression identifierTypeXpath = xpath.compile(XPATH_FIND_IDENTIFIER_TYPE);
            String idType = (String) identifierTypeXpath.evaluate(doc, XPathConstants.STRING);
            if(!idType.startsWith("ISBN") && !idType.startsWith("GTIN13")) {
                log.info("Not an ISBN or GTIN13 type of identifier. Found: '" + idType + "'. Returning a null.");
                return null;
            }
            return (String) identifierXpath.evaluate(doc, XPathConstants.STRING);
        } catch (Exception e) {
            log.warn("Could not extract the ISBN number from the file '" + pubhubMetadataFile + "'. Returning a null", 
                    e);
            return null;
        }
    }

    /**
     * Retrieves the Alma MODS record metadata file for a given ISBN number.
     * @param isbn The ISBN number for book, whose metadata record will be retrieved.
     * @param modsFile The output file where the MODS will be placed.
     * @throws IOException If it somehow fails to retrieve or write the output file.
     */
    protected void getAlmaMetadata(String isbn, File modsFile) throws IOException {
        try (OutputStream out = new FileOutputStream(modsFile)) {
            almaMetadataRetriever.retrieveMetadataForISBN(isbn, out);
            out.flush();
        }
    }
}
