package dk.kb.elivagar.metadata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.exception.ArgumentCheck;
import dk.kb.elivagar.testutils.TestFileUtils;

public class MetadataTransformerTest extends ExtendedTestCase {

    File marcToMods;
    File alephToMarc;
    
    @BeforeClass
    public void setup() throws IOException {
        TestFileUtils.setupTempDir();
        marcToMods = TestFileUtils.copyFileToTemp(new File("src/main/resources/scripts/marcToMODS.xsl"));
        alephToMarc = TestFileUtils.copyFileToTemp(new File("src/main/resources/scripts/oaimarc2slimmarc.xsl"));
        TestFileUtils.copyFileToTemp(new File("src/main/resources/scripts/MARC21slimUtils.xsl"));
    }
    
    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }
    
    @Test
    public void testConstructor() {
        MetadataTransformer transformer = new MetadataTransformer(TestFileUtils.getTempDir());
        Assert.assertTrue(transformer.transformers.isEmpty());
    }
    
    @Test(expectedExceptions = ArgumentCheck.class)
    public void testConstructorFailure() {
        addDescription("Try instantiating the metadata transformer with a non existing file instead of a directory");
        new MetadataTransformer(new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString()));
    }
    
    @Test
    public void testTransformingAlephMetadataToMods() throws IOException {
        addDescription("Test the transformation of Aleph metadata");
        File metadataFile = TestFileUtils.copyFileToTemp(new File("src/test/resources/metadata/aleph_metadata.xml"));
        MetadataTransformer transformer = new MetadataTransformer(TestFileUtils.getTempDir());
        File intermediaryMarcFile = new File(TestFileUtils.getTempDir(), "marc-" + UUID.randomUUID().toString() + ".xml");
        transformer.transformMetadata(new FileInputStream(metadataFile), new FileOutputStream(intermediaryMarcFile), MetadataTransformer.TransformationType.ALEPH_TO_MARC21);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        transformer.transformMetadata(new FileInputStream(intermediaryMarcFile), output, MetadataTransformer.TransformationType.MARC21_TO_MODS);
        
        String content = output.toString();
        Assert.assertTrue(content.contains("<title>Køl af</title>"));
        Assert.assertTrue(content.contains("<subTitle> sandheder og skrøner om den globale opvarmning</subTitle>"));
        Assert.assertTrue(content.contains("<identifier type=\"isbn\">9788711436981</identifier>"));
    }
}
