package dk.kb.elivagar.metadata;

import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.testutils.TestConfigurations;
import dk.kb.elivagar.testutils.TestFileUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public class AlmaMetadataRetrieverTest extends ExtendedTestCase {

    String VALID_ID = "9789777641364";
    String INVALID_ID = UUID.randomUUID().toString();
    Configuration configuration;

    @BeforeMethod
    public void setup() throws IOException {
        TestFileUtils.setup();
        configuration = TestConfigurations.getConfigurationForTest();
    }

    @AfterClass
    public void tearDown() throws IOException {
        TestFileUtils.tearDown();
    }

    @Test
    public void testCompleteRetrieval() throws IOException {
        addDescription("Test a complete retrieval of metadata from Alma based on a ISBN.");
        HttpClient httpClient = new HttpClient();

        AlmaMetadataRetriever retriever = new AlmaMetadataRetriever(configuration, httpClient);

        File output = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        retriever.retrieveMetadataForISBN(VALID_ID, new FileOutputStream(output));

        String extractedMetadata = TestFileUtils.readFile(output);

        System.err.println(extractedMetadata);

        Assert.assertTrue(extractedMetadata.contains("<identifier type=\"isbn\">" + VALID_ID + "</identifier>"));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testBadRetrieval() throws IOException {
        addDescription("Test a complete retrieval of metadata from Alma based on a ISBN.");
        HttpClient httpClient = new HttpClient();

        AlmaMetadataRetriever retriever = new AlmaMetadataRetriever(configuration, httpClient);

        File output = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        retriever.retrieveMetadataForISBN(INVALID_ID, new FileOutputStream(output));
    }
}
