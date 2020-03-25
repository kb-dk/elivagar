package dk.kb.elivagar.metadata;

import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.testutils.TestConfigurations;
import dk.kb.elivagar.testutils.TestFileUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AlmaMetadataRetrieverTest extends ExtendedTestCase {

    String ID = "9789777641364";
    Configuration configuration;

    @BeforeClass
    public void setup() throws IOException {
        TestFileUtils.setup();
        configuration = TestConfigurations.getConfigurationForTest();
    }


    @Test
    public void testCompleteRetrieval() {
        addDescription("Test a complete retrieval of metadata from Alma based on a ISBN.");
        HttpClient httpClient = new HttpClient();

        AlmaMetadataRetriever retriever = new AlmaMetadataRetriever(configuration, httpClient);
//        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        retriever.retrieveMetadataForISBN(ID, baos);

        String extractedMetadata = baos.toString();

        Assert.assertTrue(extractedMetadata.contains("<identifier type=\"isbn\">" + ID + "</identifier>"));
    }
}
