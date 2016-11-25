package dk.kb.elivagar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import dk.kb.elivagar.utils.StreamUtils;

public class HttpClient {

    /**
     * Constructor.
     * TODO: does currently nothing!!!
     */
    public HttpClient() {}

    /**
     * Retrieves the data from a given url and puts it onto a given 
     * outputstream. It has to be a 'HTTP' url, since the data is retrieved 
     * through a HTTP-request.
     * 
     * @param out The output stream to put the data.
     * @param url The url for where the data should be retrieved.
     * @throws IOException If any problems occurs during the retrieval of the 
     * data.
     */
    public void performDownload(OutputStream out, URL url)
            throws IOException {
        if(out == null || url == null) {
            throw new IllegalArgumentException("OutputStream out: '" + out + "', URL: '" + url + "'");
        }
        InputStream is = retrieveStream(url);
        StreamUtils.copyInputStreamToOutputStream(is, out);
    }

    /**
     * Retrieves the Input stream for a given URL.
     * @param url The URL to retrieve.
     * @return The InputStream to the given URL.
     * @throws IOException If any problems occurs during the retrieval.
     */
    protected InputStream retrieveStream(URL url) throws IOException {
        HttpURLConnection conn = getConnection(url);
        conn.setDoInput(true);
        conn.setRequestMethod("GET");
        return conn.getInputStream();
    }

    /**
     * Method for opening a HTTP connection to the given URL.
     * 
     * @param url The URL to open the connection to.
     * @return The HTTP connection to the given URL.
     */
    protected HttpURLConnection getConnection(URL url) {
        try {
            return (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            throw new IllegalStateException("Could not open the connection to the url '" + url + "'", e);
        }
    }
}
