package dk.kb.elivagar.metadata;

import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.exception.ArgumentCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

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


    /** The XPATH for the number of records.*/
    protected static final String XPATH_NUM_RESULTS = "/*:searchRetrieveResponse/*:numberOfRecords/text()";
    /** The XPATH for the schema - must be MODS.*/
    protected static final String XPATH_SCHEMA = "/searchRetrieveResponse/records/record/recordSchema";
    /** The XPATH for the MODS record.*/
    protected static final String XPATH_MODS_RECORD = "/*:searchRetrieveResponse/*:records/*:record/*:recordData/*:mods";

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

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        log.debug("Retrieve Alma metadata for ISBN: " + isbn);
        try {
            String requestUrl = conf.getAlmaSruSearch() + ALMA_SEARCH_RANGE + ALMA_SCHEMA_MODS + ALMA_QUERY_ISBN + isbn;
            httpClient.retrieveUrlContent(requestUrl, byteArrayOutputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Could not download the metadata for set '" + isbn + "'", e);
        }

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(byteArrayInputStream);
            XPathFactory xPathfactory = XPathFactory.newInstance();

            InputSource inputSource = new InputSource(byteArrayInputStream);
            XPath xpath = xPathfactory.newXPath();

            // assert numResults == 1
            String numResults = (String) xpath.evaluate(XPATH_NUM_RESULTS, doc, XPathConstants.STRING);
            if(!numResults.equals("1")) {
                throw new IllegalStateException("Did not receive exactly 1 result for '" + isbn + "'. Received: " + numResults);
            }

            XPathExpression modsResultsXpath = xpath.compile(XPATH_MODS_RECORD);
            NodeList modsResults = (NodeList) modsResultsXpath.evaluate(doc, XPathConstants.NODESET);

            Node mods = modsResults.item(0);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // Turn the node into a string
            transformer.transform(new DOMSource(mods), new StreamResult(out));
        } catch (Exception e) {
            throw new IllegalStateException("Could not extract the MODS record", e);
        }
    }
}
