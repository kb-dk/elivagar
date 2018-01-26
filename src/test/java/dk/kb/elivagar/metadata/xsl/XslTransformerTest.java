package dk.kb.elivagar.metadata.xsl;

import java.io.File;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.testutils.TestFileUtils;

public class XslTransformerTest extends ExtendedTestCase {

    @BeforeClass
    public void setup() {
        TestFileUtils.setup();
    }
    
    @Test
    public void testStuff() throws Exception {
        File xslFile = TestFileUtils.copyFileToTemp(new File("src/main/resources/scripts/oaimarc2slimmarc.xsl"));
        XslTransformer transformer = XslTransformer.getTransformer(xslFile);
        
        Assert.assertNotNull(transformer.getTransformerImpl());
    }
}
