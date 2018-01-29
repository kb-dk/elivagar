package dk.kb.elivagar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.exception.ArgumentCheck;
import dk.kb.elivagar.utils.StreamUtils;

/**
 * Http client for downloading stuff (mostly the cover image files).
 */
public class HttpClient {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

    /**
     * Constructor.
     * TODO: does currently nothing!!!
     */
    public HttpClient() {}

    /**
     * Method for extracting the content of a given URL.
     * It will throw an exception, if the response status code is not in the 200-299 range.
     * @param url The text URL to retrieve.
     * @param out The output stream, where the content from the URL is delivered.
     * @throws IOException If any connection issues occur.
     */
    public void retrieveUrlContent(String url, OutputStream out) throws IOException {
        ArgumentCheck.checkNotNullOrEmpty(url, "String url");
        ArgumentCheck.checkNotNull(out, "OutputStream out");
        
        log.debug("Retrieving content from URL: " + url);
        CloseableHttpClient client = HttpClients.createDefault();
        try {
            HttpGet getMethod = new HttpGet(url);
            
            CloseableHttpResponse response = client.execute(getMethod);
            int statusCode = response.getStatusLine().getStatusCode();
            if(statusCode < 200 || statusCode > 300) {
                throw new IllegalStateException("Received erroneous status code for url " + url + ", " + statusCode);
            }
            
            StreamUtils.copyInputStreamToOutputStream(response.getEntity().getContent(), out);
        } finally {
            client.close();
        }
    }
    
    /**
     * Retrieves the data from a given url and puts it onto a given outputstream. 
     * It has to be a 'HTTP' url, since the data is retrieved through a HTTP-request.
     * 
     * @param out The output stream to put the data.
     * @param url The url for where the data should be retrieved.
     * @throws IOException If any problems occurs during the retrieval of the data.
     */
    public void performDownload(OutputStream out, URL url) throws IOException {
        ArgumentCheck.checkNotNull(out, "OutputStream out");
        ArgumentCheck.checkNotNull(url, "URL url");
        
        InputStream is = retrieveStream(url);
        StreamUtils.copyInputStreamToOutputStream(is, out);
    }

    /**
     * Retrieves the Input stream for a given URL.
     * Remember to close the returned input stream after use.
     * @param url The URL to retrieve.
     * @return The InputStream to the given URL.
     * @throws IOException If any problems occurs during the retrieval.
     */
    protected InputStream retrieveStream(URL url) throws IOException {
        ArgumentCheck.checkNotNull(url, "URL url");
        
        HttpURLConnection conn = getConnection(url);
        conn.setDoInput(true);
        conn.setRequestMethod("GET");
        if(conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            log.warn("Received a bad http response for the retrieval of '" + url.toString() + "':"
                    + conn.getResponseCode() + ". Tries to continue anyway.");
        }
        return conn.getInputStream();
    }

    /**
     * Method for opening a HTTP connection to the given URL.
     * 
     * @param url The URL to open the connection to.
     * @return The HTTP connection to the given URL.
     */
    protected HttpURLConnection getConnection(URL url) {
        ArgumentCheck.checkNotNull(url, "URL url");

        try {
            return (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new IllegalStateException("Could not open the connection to the url '" + url + "'", e);
        }
    }
}
