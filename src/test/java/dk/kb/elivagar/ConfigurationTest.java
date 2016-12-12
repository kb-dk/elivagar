package dk.kb.elivagar;

import java.io.File;
import java.io.IOException;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConfigurationTest extends ExtendedTestCase {

    @Test
    public void testLoadingFromFile() throws IOException {
        Configuration conf = Configuration.createFromYAMLFile(new File("src/test/resources/elivagar.yml"));
        Assert.assertNotNull(conf);
        Assert.assertNotNull(conf.getFileDir());
        Assert.assertNotNull(conf.getLicenseKey());
        Assert.assertNotNull(conf.getAudioOutputDir());
        Assert.assertNotNull(conf.getEbookOutputDir());
        Assert.assertTrue(conf.getAudioOutputDir().isDirectory());
        Assert.assertTrue(conf.getEbookOutputDir().isDirectory());
    }
}
