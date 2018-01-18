package dk.kb.elivagar.metadata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.config.AlephConfiguration;
import dk.kb.elivagar.testutils.TestConfigurations;
import dk.kb.elivagar.testutils.TestFileUtils;

public class AlephMetadataRetrieverTest extends ExtendedTestCase {

    String ID = "9788711436981";
    AlephConfiguration configuration;
    
    @BeforeClass
    public void setup() throws IOException {
        TestFileUtils.setupTempDir();
        configuration = TestConfigurations.getAlephConfigurationForTest();
    }
    
    @Test
    public void testCompleteRetrieval() {
        addDescription("Test a complete retrieval of metadata from Aleph based on a ISBN.");
        HttpClient httpClient = new HttpClient();
        
        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        retriever.retrieveMetadataForID(ID, baos);
        
        String extractedMetadata = baos.toString();
        
        Assert.assertTrue(extractedMetadata.contains("<subfield label=\"e\">" + ID + "</subfield>"));
    }
    
    @Test
    public void testPerformAlephSearch() throws Exception {
        addDescription("Test the proper retrieval of an entry from Aleph.");
        HttpClient httpClient = new HttpClient();
        
        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);
        
        File searchResult = retriever.performAlephSearch(ID);
        String searchResults = TestFileUtils.readFile(searchResult);

        Assert.assertTrue(searchResults.contains("<no_records>000000001</no_records>"));
        Assert.assertTrue(searchResults.contains("<no_entries>000000001</no_entries>"));
    }
    
//    @Test
//    public void 
}
