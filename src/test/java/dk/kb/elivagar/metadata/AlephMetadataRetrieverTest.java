package dk.kb.elivagar.metadata;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.config.AlephConfiguration;
import dk.kb.elivagar.testutils.TestConfigurations;
import dk.kb.elivagar.testutils.TestFileUtils;

public class AlephMetadataRetrieverTest extends ExtendedTestCase {

    AlephConfiguration configuration;
    
    @BeforeClass
    public void setup() throws IOException {
        TestFileUtils.setupTempDir();
        configuration = TestConfigurations.getAlephConfigurationForTest();
    }
    
    @Test
    public void testStuff() {
        addDescription("Test ");
        
        HttpClient httpClient = new HttpClient();
        
        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        retriever.retrieveMetadataForID("9788711436981", baos);
        
        System.err.println(baos.toString());
    }
}
