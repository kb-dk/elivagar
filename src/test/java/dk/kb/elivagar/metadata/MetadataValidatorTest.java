package dk.kb.elivagar.metadata;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;

public class MetadataValidatorTest extends ExtendedTestCase {

    @Test
    public void testValidXml() throws Exception {
        addDescription("Tests that it can validate a proper XML file.");
        MetadataValidator validator = new MetadataValidator();
        File f = new File("src/test/resources/metadata/mods.xml");
        Assert.assertTrue(validator.isValid(f));
    }

    @Test
    public void testInvalidXml() throws Exception {
        addDescription("Tests that it fails the validation of a invalid xml file.");
        MetadataValidator validator = new MetadataValidator();
        File f = new File("src/test/resources/elivagar.yml");
        Assert.assertFalse(validator.isValid(f));
    }
}
