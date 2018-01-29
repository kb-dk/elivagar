package dk.kb.elivagar.pubhub.validator;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.pubhub.PubhubPacker;
import dk.kb.elivagar.pubhub.validator.AudioSuffixValidator;
import dk.kb.elivagar.pubhub.validator.EbookSuffixValidator;
import dk.kb.elivagar.script.CharacterizationScriptWrapper;
import dk.kb.elivagar.testutils.TestFileUtils;

public class SuffixValidatorTest extends ExtendedTestCase {
    public static final String PDF_SUFFIX = ".pdf";
    public static final String EPUB_SUFFIX = ".epub";
    public static final String MP3_SUFFIX = ".mp3";

    String serviceNamespace = "" + UUID.randomUUID().toString();
    CharacterizationScriptWrapper script = null;

    @BeforeClass
    public void setup() {
        TestFileUtils.setup();
    }
    
    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }
    
    @Test
    public void testEbookSuffix() throws Exception {
        Configuration conf = Mockito.mock(Configuration.class);
        Mockito.when(conf.getEbookFormats()).thenReturn(Arrays.asList("pdf", "epub"));
        EbookSuffixValidator validator = new EbookSuffixValidator(conf);

        Assert.assertTrue(validator.hasValidSuffix(makeFile(UUID.randomUUID().toString() + EPUB_SUFFIX)));
        Assert.assertFalse(validator.hasValidSuffix(makeFile(UUID.randomUUID().toString() + PubhubPacker.FITS_SUFFIX)));
        Assert.assertTrue(validator.hasValidSuffix(makeFile(UUID.randomUUID().toString() + PDF_SUFFIX)));
        Assert.assertFalse(validator.hasValidSuffix(makeFile(UUID.randomUUID().toString() + PubhubPacker.XML_SUFFIX)));
        Assert.assertFalse(validator.hasValidSuffix(makeFile(UUID.randomUUID().toString() + MP3_SUFFIX)));
        Assert.assertFalse(validator.hasValidSuffix(makeFile(UUID.randomUUID().toString())));
    }
    
    @Test
    public void testAudioSuffix() throws Exception {
        Configuration conf = Mockito.mock(Configuration.class);
        Mockito.when(conf.getAudioFormats()).thenReturn(Arrays.asList("mp3"));
        AudioSuffixValidator validator = new AudioSuffixValidator(conf);

        Assert.assertFalse(validator.hasValidSuffix(makeFile(UUID.randomUUID().toString() + EPUB_SUFFIX)));
        Assert.assertFalse(validator.hasValidSuffix(makeFile(UUID.randomUUID().toString() + PubhubPacker.FITS_SUFFIX)));
        Assert.assertFalse(validator.hasValidSuffix(makeFile(UUID.randomUUID().toString() + PDF_SUFFIX)));
        Assert.assertFalse(validator.hasValidSuffix(makeFile(UUID.randomUUID().toString() + PubhubPacker.XML_SUFFIX)));
        Assert.assertTrue(validator.hasValidSuffix(makeFile(UUID.randomUUID().toString() + MP3_SUFFIX)));
        Assert.assertFalse(validator.hasValidSuffix(makeFile(UUID.randomUUID().toString())));
    }
    
    protected File makeFile(String path) throws IOException {
        File res = new File(TestFileUtils.getTempDir(), path);
        TestFileUtils.createFile(res, path);
        return res;
    }
}
