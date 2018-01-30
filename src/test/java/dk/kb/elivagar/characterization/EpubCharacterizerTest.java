package dk.kb.elivagar.characterization;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.testutils.TestFileUtils;

public class EpubCharacterizerTest extends ExtendedTestCase {

    @BeforeClass
    public void setup() {
        TestFileUtils.setup();
    }
    
    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }
    
    @Test
    public void testEpub() throws IOException {
        File f = TestFileUtils.copyFileToTemp(new File("src/test/resources/book-files/epub30-spec.epub"));
        
        EpubCharacterizer epubCharacterizer = new EpubCharacterizer();
        
        Assert.assertTrue(epubCharacterizer.hasRequiredExtension(f));
        
        File outputFile = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        
        Assert.assertFalse(outputFile.exists());
        
        epubCharacterizer.characterize(f, outputFile);
        Assert.assertTrue(outputFile.exists());
    }
}
