package dk.kb.elivagar.pubhub;

import dk.kb.elivagar.HttpClient;

public class PubhubFileRetriever {

    protected final HttpClient httpClient;
//    protected
    
    public PubhubFileRetriever() {
        httpClient = new HttpClient();
    }
}
