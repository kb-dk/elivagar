package dk.kb.elivagar.config;

import java.io.File;
import java.io.IOException;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.testutils.TestConfigurations;
import dk.kb.elivagar.testutils.TestFileUtils;
import dk.kb.elivagar.utils.FileUtils;

public class ConfigurationTest extends ExtendedTestCase {

    @BeforeClass
    public void setup() throws IOException {
        TestFileUtils.setup();
    }
    
    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }
    
    @Test
    public void testLoadingFromFile() throws IOException {
        FileUtils.createDirectory("tempDir/transfer/ebook/ingest");
        FileUtils.createDirectory("tempDir/transfer/ebook/content");
        FileUtils.createDirectory("tempDir/transfer/ebook/metadata");
        FileUtils.createDirectory("tempDir/transfer/audio/ingest");
        FileUtils.createDirectory("tempDir/transfer/audio/content");
        FileUtils.createDirectory("tempDir/transfer/audio/metadata");
        
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
        Assert.assertNotNull(conf.getStatisticsDir());
        
        Assert.assertTrue(conf.getAudioOutputDir().isDirectory());
        Assert.assertTrue(conf.getEbookOutputDir().isDirectory());
        Assert.assertFalse(conf.getAudioFormats().isEmpty());
        Assert.assertFalse(conf.getEbookFormats().isEmpty());
        Assert.assertTrue(conf.getStatisticsDir().isDirectory());

        Assert.assertNotNull(conf.getAlmaSruSearch());

        addStep("Test the transfer configuration", "");
        Assert.assertNotNull(conf.getTransferConfiguration());
        Assert.assertNotNull(conf.getTransferConfiguration().getEbookIngestDir());
        Assert.assertNotNull(conf.getTransferConfiguration().getUpdateEbookContentDir());
        Assert.assertNotNull(conf.getTransferConfiguration().getUpdateEbookMetadataDir());
        Assert.assertNotNull(conf.getTransferConfiguration().getAudioIngestDir());
        Assert.assertNotNull(conf.getTransferConfiguration().getUpdateAudioContentDir());
        Assert.assertNotNull(conf.getTransferConfiguration().getUpdateAudioMetadataDir());
        Assert.assertNotNull(conf.getTransferConfiguration().getRetainCreateDate());
        Assert.assertNotNull(conf.getTransferConfiguration().getRetainModifyDate());
        Assert.assertNotNull(conf.getTransferConfiguration().getRetainPublicationDate());
        Assert.assertNotNull(conf.getTransferConfiguration().getRequiredFormats());
        Assert.assertTrue(conf.getTransferConfiguration().getEbookIngestDir().isDirectory());
        Assert.assertTrue(conf.getTransferConfiguration().getUpdateEbookContentDir().isDirectory());
        Assert.assertTrue(conf.getTransferConfiguration().getUpdateEbookMetadataDir().isDirectory());
        Assert.assertTrue(conf.getTransferConfiguration().getAudioIngestDir().isDirectory());
        Assert.assertTrue(conf.getTransferConfiguration().getUpdateAudioContentDir().isDirectory());
        Assert.assertTrue(conf.getTransferConfiguration().getUpdateAudioMetadataDir().isDirectory());
    }
    
    @Test
    public void testConfigurationWithoutTransfer() throws IOException {
        Configuration conf = TestConfigurations.getConfigurationForTestWithoutTransfer();
        Assert.assertNull(conf.getTransferConfiguration());
    }
}
