package dk.kb.elivagar.metadata;

import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.config.AlephConfiguration;
import dk.kb.elivagar.config.AlmaConfiguration;
import dk.kb.elivagar.exception.ArgumentCheck;
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
import java.util.List;

/**
 * Alma Metadata Retriever.
 *
 * Retrieves the metadata entries by first searching for the given ID, and then using
 * the search result to extract the actual metadata entry from Alma.
 */
public class AlmaMetadataRetriever {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(AlmaMetadataRetriever.class);

//    private static
//
//    /** The Aleph argument for performing the find-operation, for searching in Aleph.*/
//    protected static final String OPERATION_FIND = "op=find";
//    /** The Aleph argument for performing the operation 'present', which retrieves the OAI XML. */
//    protected static final String OPERATION_PRESENT = "op=present";
//    /** The Aleph find argument for setting the code to 'WRD'.*/
//    protected static final String CODE_WRD = "code=wrd";
//    /** The Aleph present argument for only extracting the first entry from the entry set.*/
//    protected static final String SET_ENTRY_SINGLE_RANGE = "set_entry=000000001-000000001";
//
//    /** The XPATH for extracting any error from the Aleph search results.*/
//    protected static final String XPATH_FIND_ERROR = "/find/error/text()";
//    /** The XPATH for extracting the SetNumber from the Aleph search results.*/
//    protected static final String XPATH_FIND_SET_NUMBER = "/find/set_number/text()";
//    /** The XPATH for extracting the number of entries in the Aleph search results.*/
//    protected static final String XPATH_FIND_NUMBER_OF_ENTRIES = "/find/no_entries/text()";

    /** The configuration for dealing with Aleph.*/
    protected final AlmaConfiguration conf;
    /** The HTTP client for making the HTTP Get operations towards the Aleph server.*/
    protected final HttpClient httpClient;

    /** The document builder factory.*/
    protected final DocumentBuilderFactory documentBuilderFactory;
    /** The XPath factory.*/
    protected final XPathFactory xPathFactory;

    /**
     * Constructor.
     * @param configuration The configurations regarding dealing with Alma.
     * @param httpClient The HTTP client for performing the HTTP Get operations.
     */
    public AlmaMetadataRetriever(AlmaConfiguration configuration, HttpClient httpClient) {
        ArgumentCheck.checkNotNull(configuration, "AlmaConfiguration configuration");
        ArgumentCheck.checkNotNull(httpClient, "HttpClient httpClient");
        this.conf= configuration;
        this.httpClient = httpClient;
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        xPathFactory = XPathFactory.newInstance();
    }

    /**
     * Retrieves the Alma metadata for a given ID.
     * @param isbn The ID to retrieve the Alma metadata for.
     * @param out The output stream, where the Alma metadata will be written.
     */
    public void retrieveMetadataForISBN(String isbn, OutputStream out) {
        ArgumentCheck.checkNotNullOrEmpty(isbn, "String isbn");
        ArgumentCheck.checkNotNull(out, "OutputStream out");
        
        log.debug("Retrieve Alma metadata for ISBN: " + isbn);
        try {
            String requestUrl = conf.getSruBaseUrl() + conf.getFixedParameters() + isbn;
            httpClient.retrieveUrlContent(requestUrl, out);
        } catch (IOException e) {
            throw new IllegalStateException("Could not download the metadata for set '" + isbn + "'", e);
        }
    }

}
