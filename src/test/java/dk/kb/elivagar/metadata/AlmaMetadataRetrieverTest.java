package dk.kb.elivagar.metadata;

import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.testutils.TestConfigurations;
import dk.kb.elivagar.testutils.TestFileUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class AlmaMetadataRetrieverTest extends ExtendedTestCase {

    String ID = "9789777641364";
    Configuration configuration;

    @BeforeClass
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
        retriever.retrieveMetadataForISBN(ID, new FileOutputStream(output));

        String extractedMetadata = TestFileUtils.readFile(output);

        System.err.println(extractedMetadata);

        Assert.assertTrue(extractedMetadata.contains("<identifier type=\"isbn\">" + ID + "</identifier>"));
    }
}
