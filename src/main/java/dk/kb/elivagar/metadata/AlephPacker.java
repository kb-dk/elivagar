package dk.kb.elivagar.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.exception.ArgumentCheck;

/**
 * The Aleph packer.
 * It will iterate through all the books - both E-books and Audio books.
 * The books which does not have a MODS metadata file in their package directory, will have the metadata retrieved.
 * This is done by extracting the ISBN number from the Publizon metadata file, then use this ISBN to retrieve the 
 * Aleph DanMarc2 metadata, and then transforming that Aleph metadata into MARC21, and finally transforming the
 * MARC21 metadata into MODS.
 * This MODS metadata file is then placed in the book's package directory.
 */
public class AlephPacker {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(AlephPacker.class);

    /** The XPATH for extracting the Identifier from the pubhub Book xml file.*/
    protected static final String XPATH_FIND_IDENTIFIER = "/*:Book/*:Identifier/text()";
    /** The XPATH for extracting the IdentifierType from the pubhub Book xml file.*/
    protected static final String XPATH_FIND_IDENTIFIER_TYPE = "/*:Book/*:IdentifierType/text()";
    
    /** The configuration.*/
    protected final Configuration conf;
    /** The metadata retriever for the Aleph metadata.*/
    protected final AlephMetadataRetriever metadataRetriever;
    /** The metadata transformer, for transforming the Aleph metadata first into MARC21 and then into MODS.*/
    protected final MetadataTransformer transformer;

    /** The document builder factory.*/
    protected final DocumentBuilderFactory factory;
    /** The XPath factory.*/
    protected final XPathFactory xPathfactory;

    
    /**
     * Constructor.
     * @param conf The configuration.
     * @param alephRetriever The retriever of Aleph metadata.
     * @param transformer The metadata transformer.
     */
    public AlephPacker(Configuration conf, AlephMetadataRetriever alephRetriever, MetadataTransformer transformer) {
        ArgumentCheck.checkNotNull(conf, "Configuration conf");
        ArgumentCheck.checkNotNull(alephRetriever, "AlephMetadataRetriever alephRetriever");
        ArgumentCheck.checkNotNull(transformer, "MetadataTransformer transformer");
        this.conf = conf;
        this.metadataRetriever = alephRetriever;
        this.transformer = transformer;
        this.factory = DocumentBuilderFactory.newInstance();
        this.xPathfactory = XPathFactory.newInstance();
    }
    
    /**
     * Pack Aleph metadata for all books; both E-books and Audio books.
     * Will not retrieve the metadata, if it has already been retrieved.
     * 
     * If the e-book package base directory and the audio book package base directory are the same, then they
     * are only traversed once.
     */
    public void packAlephMetadataForBooks() {
        traverseBooksInFolder(conf.getEbookOutputDir());
        if(conf.getEbookOutputDir().getAbsolutePath().equals(conf.getAudioOutputDir().getAbsolutePath())) {
            log.debug("Ebooks and Audio books have same base-dir.");
        } else {
            traverseBooksInFolder(conf.getAudioOutputDir());            
        }
    }
    
    /**
     * Traverses the books in the base directory to retrieve, transform and package the Aleph metadata.
     * @param baseBookDir The base directory for the books (either E-books or Audio books).
     */
    protected void traverseBooksInFolder(File baseBookDir) {
        File[] files = baseBookDir.listFiles();
        if(files == null) {
            log.warn("No books to retrieve and transform Aleph metadata for within the directory: "
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
     * Otherwise it retrieves the Aleph metadata and transforms it into MODS.
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
                log.debug("Could not retrieve a ISBN from '" + dir.getAbsolutePath() + "'.");
                return;
            }
            
            File alephMetadata = getAlephMetadata(isbn);
            File marcMetadata = transformAlephMetadataToMarc(alephMetadata, isbn);

            try (OutputStream out = new FileOutputStream(modsMetadata)) {
                transformMarcToMods(marcMetadata, out);
            }

            alephMetadata.deleteOnExit();
            marcMetadata.deleteOnExit();
        } catch (IOException e) {
            log.warn("Failed to handle the book directory '" + dir.getAbsolutePath() + "'", e);
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
        File pubhubMetadataFile = new File(dir, dir.getName() + ".xml");
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
            if(!idType.startsWith("ISBN")) {
                log.info("Not an ISBN type of identifier. Found: '" + idType + "'. Returning a null.");
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
     * Retrieves the Aleph DanMarc2 XML metadata file for a given ISBN number.
     * @param isbn The ISBN number for book, whose metadata record will be retrieved.
     * @return The file with the Aleph DanMarc2 XML metadata.
     * @throws IOException If it somehow fails to retrieve or write the output file.
     */
    protected File getAlephMetadata(String isbn) throws IOException {
        File res = new File(conf.getAlephConfiguration().getTempDir(), isbn + Constants.ALEPH_METADATA_SUFFIX);
        try (OutputStream out = new FileOutputStream(res)) {
            metadataRetriever.retrieveMetadataForISBN(isbn, out);
            out.flush();
        }
        return res;
    }
    
    /**
     * Transforms an Aleph DanMarc2 XML metadata file into a MARC21 XML metadata file.
     * @param alephMetadata The file with the Aleph DanMarc2 XML metadata.
     * @param isbn The ISBN number for the metadata record.
     * @return The file with the MARC21 XML metadata file.
     * @throws IOException If it somehow fails to make the transformation.
     */
    protected File transformAlephMetadataToMarc(File alephMetadata, String isbn) throws IOException {
        File res = new File(conf.getAlephConfiguration().getTempDir(), isbn + Constants.MARC_METADATA_SUFFIX);
        try (InputStream in = new FileInputStream(alephMetadata);
                OutputStream out = new FileOutputStream(res)) {
            transformer.transformMetadata(in, out, MetadataTransformer.TransformationType.ALEPH_TO_MARC21);
            out.flush();
        } 
        return res;
    }
    
    /**
     * Transforms a file with MARC21 XML metadata into MODS.
     * @param marcMetadata The file with MARC 21 metadata.
     * @param modsOutput The output stream, where the MODS metadata is written.
     * @throws IOException If it somehow fails to make the transformation.
     */
    protected void transformMarcToMods(File marcMetadata, OutputStream modsOutput) throws IOException {
        try (InputStream in = new FileInputStream(marcMetadata)) {
            transformer.transformMetadata(in, modsOutput, MetadataTransformer.TransformationType.MARC21_TO_MODS);
            modsOutput.flush();
        } 
    }
}
