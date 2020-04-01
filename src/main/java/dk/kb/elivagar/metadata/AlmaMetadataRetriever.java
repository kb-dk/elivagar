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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Alma Metadata Retriever.
 *
 * Makes a direct ISBN search in Alma and extracts the MODS records.
 *
 * It should create MODS retrieval URLs like the following:
 * https://kbdk-kgl.alma.exlibrisgroup.com/view/sru/45KBDK_KGL?version=1.2&operation=searchRetrieve&startRecord=1&maximumRecords=2&recordSchema=mods&query=isbn=$ISBN
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


    /** The XPATH for the number of records.
     * Using '*' as wildcard for the namespace.*/
    protected static final String XPATH_NUM_RESULTS = "/*:searchRetrieveResponse/*:numberOfRecords/text()";
    /** The XPATH for the MODS record.
     * Using '*' as wildcard for the namespace.*/
    protected static final String XPATH_MODS_RECORD = "/*:searchRetrieveResponse/*:records/*:record/*:recordData/*:mods";

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
     * @param out The output stream, where the MODS metadata from Alma will be written.
     */
    public void retrieveMetadataForISBN(String isbn, OutputStream out) {
        ArgumentCheck.checkNotNullOrEmpty(isbn, "String isbn");
        ArgumentCheck.checkNotNull(out, "OutputStream out");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        retrieveAlmaMetadata(isbn, byteArrayOutputStream);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        extractModsFromAlma(byteArrayInputStream, out);
    }

    /**
     * Retrieves the Alma metadata for the given ISBN and writes it to the output stream.
     * @param isbn The ISBN number for the record to retrieve metadata for.
     * @param out Output stream where the retrieved metadata is written.
     */
    protected void retrieveAlmaMetadata(String isbn, OutputStream out) {
        log.debug("Retrieving Alma metadata for ISBN: " + isbn);

        try {
            String requestUrl = conf.getAlmaSruSearch() + ALMA_SEARCH_RANGE + ALMA_SCHEMA_MODS + ALMA_QUERY_ISBN + isbn;
            httpClient.retrieveUrlContent(requestUrl, out);
        } catch (IOException e) {
            throw new IllegalStateException("Could not download the metadata for set '" + isbn + "'", e);
        }
    }

    /**
     * Extracts the MODS record from the Alma record.
     * @param almaInput The input stream with the Alma metadata.
     * @param modsOutput The output stream with the MODS metadata.
     */
    protected void extractModsFromAlma(InputStream almaInput, OutputStream modsOutput) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(almaInput);
            XPath xpath = xPathFactory.newXPath();

            // assert numResults == 1 - else fail
            // TODO: should we also fail, when it has more than 1 number of results?
            String numResults = (String) xpath.evaluate(XPATH_NUM_RESULTS, doc, XPathConstants.STRING);
            if(!numResults.equals("1")) {
                throw new IllegalStateException("Did not receive exactly 1 result from Alma. Received: " + numResults);
            }

            XPathExpression modsResultsXpath = xpath.compile(XPATH_MODS_RECORD);
            NodeList modsResults = (NodeList) modsResultsXpath.evaluate(doc, XPathConstants.NODESET);

            Node mods = modsResults.item(0);

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // Turn the node into a string
            transformer.transform(new DOMSource(mods), new StreamResult(modsOutput));
        } catch (Exception e) {
            throw new IllegalStateException("Could not extract the MODS record", e);
        }
    }
}
