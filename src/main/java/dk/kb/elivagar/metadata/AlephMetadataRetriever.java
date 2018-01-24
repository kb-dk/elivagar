package dk.kb.elivagar.metadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.config.AlephConfiguration;

/**
 * Aleph Metadata Retriever.
 * Retrieves the metadata entries by first searching for the given ID, and then using
 * the search result to extract the actual metadata entry from Aleph.
 */
public class AlephMetadataRetriever {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(AlephMetadataRetriever.class);

    /** The Aleph argument for performing the find-operation, for searching in Aleph.*/
    protected static final String OPERATION_FIND = "op=find";
    /** The Aleph argument for performing the operation 'present', which retrieves the OAI XML. */
    protected static final String OPERATION_PRESENT = "op=present";
    /** The Aleph find argument for setting the code to 'WRD'.*/
    protected static final String CODE_WRD = "code=wrd";
    /** The Aleph present argument for only extracting the first entry from the entry set.*/
    protected static final String SET_ENTRY_SINGLE_RANGE = "set_entry=000000001-000000001";
    
    /** The XPATH for extracting any error from the Aleph search results.*/
    protected static final String XPATH_FIND_ERROR = "/find/error/text()";
    /** The XPATH for extracting the SetNumber from the Aleph search results.*/
    protected static final String XPATH_FIND_SET_NUMBER = "/find/set_number/text()";
    /** The XPATH for extracting the number of entries in the Aleph search results.*/
    protected static final String XPATH_FIND_NUMBER_OF_ENTRIES = "/find/no_entries/text()";
    
    /** The configuration for dealing with Aleph.*/
    protected final AlephConfiguration conf;
    /** The HTTP client for making the HTTP Get operations towards the Aleph server.*/
    protected final HttpClient httpClient;
    
    /**
     * Constructor.
     * @param configuration The configurations regarding dealing with Aleph.
     * @param httpClient The HTTP client for performing the HTTP Get operations.
     */
    public AlephMetadataRetriever(AlephConfiguration configuration, HttpClient httpClient) {
        this.conf= configuration;
        this.httpClient = httpClient;
    }
    
    /**
     * Retrieves the Aleph metadata for a given ID.
     * @param isbn The ID to retrieve the Aleph metadata for.
     * @param out The output stream, where the Aleph metadata will be written.
     */
    public void retrieveMetadataForISBN(String isbn, OutputStream out) {
        log.debug("Retrieve Aleph metadata for ISBN: " + isbn);
        String setNumber = getAlephSetNumber(isbn);
        if(setNumber != null) {
            downloadAlephMetadata(setNumber, out);
        } else {
            throw new IllegalStateException("Could not extract Aleph metadata for ISBN: " + isbn);
        }
    }
    
    /**
     * Downloads the metadata for the given Aleph result set.
     * @param setNumber The SetNumber for the Aleph result set.
     * @param out The output stream, where the content will be written.
     */
    protected void downloadAlephMetadata(String setNumber, OutputStream out) {
        try {
            String requestUrl = conf.getServerUrl() + OPERATION_PRESENT + "&" + SET_ENTRY_SINGLE_RANGE
                    + "&" + "base=" + conf.getBase() + "&" + "set_number=" + setNumber;
            httpClient.retrieveUrlContent(requestUrl, out);
        } catch (IOException e) {
            throw new IllegalStateException("Could not download the metadata for set '" + setNumber + "'", e);
        }
    }
    
    /**
     * Searches Aleph for a set with the given ID.
     * Returns the SetNumber, which can be used for retrieving the actual metadata for the ID.
     * @param id The ID to locate.
     * @return The SetNumber for retrieving the actual metadata.
     */
    protected String getAlephSetNumber(String id) {
        File searchResult = performAlephSearch(id);
        try {
            String res = findSetNumberInFile(searchResult);
            return res;
        } finally {
            searchResult.deleteOnExit();
        }
    }
    
    /**
     * Locate the SetNumber in the given Aleph XML search result file.
     * @param f The file with the Aleph XML search results.
     * @return The SetNumber in the given file, or null if no results where found.
     */
    protected String findSetNumberInFile(File f) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(f);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression errorXpath = xpath.compile(XPATH_FIND_ERROR);
            XPathExpression setNumberXpath = xpath.compile(XPATH_FIND_SET_NUMBER);
            XPathExpression noEntriesXpath = xpath.compile(XPATH_FIND_NUMBER_OF_ENTRIES);
            String error = (String) errorXpath.evaluate(doc, XPathConstants.STRING);
            if(error != null && !error.isEmpty()) {
                log.debug("Aleph search gave the following error: " + error);
                return null;
            }
            String numberOfEntriesText = (String) noEntriesXpath.evaluate(doc, XPathConstants.STRING);
            int numberOfEntries = Integer.parseInt(numberOfEntriesText);
            if(numberOfEntries < 1) {
                log.info("No entries found.");
                return null;
            }
            if(numberOfEntries > 1) {
                log.debug("Found more than 1 entry. Retrieving only the first.");
            }
            return setNumberXpath.evaluate(doc);
        } catch (IOException | XPathExpressionException | ParserConfigurationException | SAXException 
                | NumberFormatException e) {
            throw new IllegalStateException("Could not extract the SetNumber from the file '" + f + "'", e);
        }
    }
    
    /**
     * Performs the Aleph search for the given ID.
     * It will return a file with the response content, or null if something goes wrong. 
     * @param id The ID of the Aleph record to search for.
     * @return The file with the response content, or null if something went wrong. 
     */
    protected File performAlephSearch(String id) {
        try {
            String requestUrl = conf.getServerUrl() + OPERATION_FIND + "&" + CODE_WRD + "&" + "base=" 
                    + conf.getBase() + "&" + "request=" + id;

            File searchResultFile = new File(conf.getTempDir(), id);
            try (OutputStream out = new FileOutputStream(searchResultFile)) {
                httpClient.retrieveUrlContent(requestUrl, out);
            }
            
            return searchResultFile;
        } catch (IOException e) {
            throw new IllegalStateException("Error occured while trying to search for the id: " + id, e);
        }
    }
}
