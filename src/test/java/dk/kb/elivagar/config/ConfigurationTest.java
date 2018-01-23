package dk.kb.elivagar.config;

import java.io.File;
import java.io.IOException;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.testutils.TestFileUtils;

public class ConfigurationTest extends ExtendedTestCase {

    @BeforeClass
    public void setup() throws IOException {
        TestFileUtils.setupTempDir();
    }
    
    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }
    
    @Test
    public void testLoadingFromFile() throws IOException {
        Configuration conf = Configuration.createFromYAMLFile(new File("src/test/resources/elivagar.yml"));
        Assert.assertNotNull(conf);
        Assert.assertNotNull(conf.getAudioFileDir());
        Assert.assertNotNull(conf.getAudioFormats());
        Assert.assertNotNull(conf.getAudioOutputDir());
        Assert.assertNotNull(conf.getCharacterizationScriptFile());
        Assert.assertNotNull(conf.getEbookFileDir());
        Assert.assertNotNull(conf.getEbookFormats());
        Assert.assertNotNull(conf.getEbookOutputDir());
        Assert.assertNotNull(conf.getLicenseKey());
        Assert.assertTrue(conf.getAudioOutputDir().isDirectory());
        Assert.assertTrue(conf.getEbookOutputDir().isDirectory());
        Assert.assertFalse(conf.getAudioFormats().isEmpty());
        Assert.assertFalse(conf.getEbookFormats().isEmpty());
        Assert.assertNotNull(conf.getAlephConfiguration());
        Assert.assertNotNull(conf.getAlephConfiguration().getServerUrl());
        Assert.assertNotNull(conf.getAlephConfiguration().getBase());
        Assert.assertTrue(conf.getAlephConfiguration().getTempDir().isDirectory());
    }
}
