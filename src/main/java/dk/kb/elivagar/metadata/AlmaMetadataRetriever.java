package dk.kb.elivagar.metadata;

import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.exception.ArgumentCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Alma Metadata Retriever.
 *
 * Makes a direct ISBN search in Alma and extracts the MODS records.
 */
public class AlmaMetadataRetriever {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(AlmaMetadataRetriever.class);

    /** The search range parameters for retrieving records from Alma. */
    protected static final String ALMA_SEARCH_RANGE = "startRecord=1&maximumRecords=2&";
    /** The schema parameters for retrieving MODS records from Alma.*/
    protected static final String ALMA_SCHEMA_MODS = "recordSchema=mods&";
    /** The base query for performing ISBN search in Alma.*/
    protected static final String ALMA_QUERY_ISBN = "query=isbn=";


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

    /** The configuration.*/
    protected final Configuration conf;
    /** The HTTP client for making the HTTP Get operations towards the Alma server.*/
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
    public AlmaMetadataRetriever(Configuration configuration, HttpClient httpClient) {
        ArgumentCheck.checkNotNull(configuration, "Configuration configuration");
        ArgumentCheck.checkNotNull(httpClient, "HttpClient httpClient");
        this.conf= configuration;
        this.httpClient = httpClient;
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        xPathFactory = XPathFactory.newInstance();
    }

    /**
     * Retrieves the MODS metadata for a given ISBN from Alma.
     * @param isbn The ID to retrieve the Alma metadata for.
     * @param out The output stream, where the Alma metadata will be written.
     */
    public void retrieveMetadataForISBN(String isbn, OutputStream out) {
        ArgumentCheck.checkNotNullOrEmpty(isbn, "String isbn");
        ArgumentCheck.checkNotNull(out, "OutputStream out");
        
        log.debug("Retrieve Alma metadata for ISBN: " + isbn);
        try {
            String requestUrl = conf.getAlmaSruSearch() + ALMA_SEARCH_RANGE + ALMA_SCHEMA_MODS + ALMA_QUERY_ISBN + isbn;
            httpClient.retrieveUrlContent(requestUrl, out);
        } catch (IOException e) {
            throw new IllegalStateException("Could not download the metadata for set '" + isbn + "'", e);
        }
    }
}
