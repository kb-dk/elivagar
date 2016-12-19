package dk.kb.elivagar.pubhub;

import java.io.File;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import dk.kb.elivagar.Configuration;
import dk.kb.elivagar.script.CharacterizationScriptWrapper;

public class PubhubPackerTest extends ExtendedTestCase {

    String serviceNamespace = "" + UUID.randomUUID().toString();
    CharacterizationScriptWrapper script = null;

    @Test
    public void testEbookSuffix() throws Exception {
        Configuration conf = Mockito.mock(Configuration.class);
        PubhubPacker pp = new PubhubPacker(conf, serviceNamespace, script);

        Assert.assertTrue(pp.hasEbookFileSuffix(new File(UUID.randomUUID().toString() + PubhubPacker.EPUB_SUFFIX)));
        Assert.assertFalse(pp.hasEbookFileSuffix(new File(UUID.randomUUID().toString() + PubhubPacker.FITS_SUFFIX)));
        Assert.assertTrue(pp.hasEbookFileSuffix(new File(UUID.randomUUID().toString() + PubhubPacker.PDF_SUFFIX)));
        Assert.assertFalse(pp.hasEbookFileSuffix(new File(UUID.randomUUID().toString() + PubhubPacker.XML_SUFFIX)));
        Assert.assertFalse(pp.hasEbookFileSuffix(new File(UUID.randomUUID().toString())));
    }
}
