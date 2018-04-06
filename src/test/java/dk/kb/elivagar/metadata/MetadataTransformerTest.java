package dk.kb.elivagar.metadata;

import java.io.ByteArrayInputStream;
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
import dk.kb.elivagar.metadata.MetadataTransformer.TransformationType;
import dk.kb.elivagar.metadata.xsl.XslTransformer;
import dk.kb.elivagar.testutils.TestFileUtils;

public class MetadataTransformerTest extends ExtendedTestCase {

    File marcToMods;
    File alephToMarc;
    
    @BeforeClass
    public void setup() throws IOException {
        TestFileUtils.setup();
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

    @Test
    public void testTransformingAlephMetadataToMods2() throws IOException {
        addDescription("Test the transformation of Aleph metadata");
        File metadataFile = TestFileUtils.copyFileToTemp(new File("src/test/resources/metadata/9788758822280.aleph.xml"));
        MetadataTransformer transformer = new MetadataTransformer(TestFileUtils.getTempDir());
        File intermediaryMarcFile = new File(TestFileUtils.getTempDir(), "marc-" + UUID.randomUUID().toString() + ".xml");
        transformer.transformMetadata(new FileInputStream(metadataFile), new FileOutputStream(intermediaryMarcFile), MetadataTransformer.TransformationType.ALEPH_TO_MARC21);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        transformer.transformMetadata(new FileInputStream(intermediaryMarcFile), output, MetadataTransformer.TransformationType.MARC21_TO_MODS);
        
//        String content = output.toString();
//        Assert.assertTrue(content.contains("<title>Køl af</title>"));
//        Assert.assertTrue(content.contains("<subTitle> sandheder og skrøner om den globale opvarmning</subTitle>"));
//        Assert.assertTrue(content.contains("<identifier type=\"isbn\">9788711436981</identifier>"));
    }
    
    @Test
    public void testTransformationTypes() {
        addDescription("Test the different types of transformations.");
        for(MetadataTransformer.TransformationType type : MetadataTransformer.TransformationType.values()) {
            Assert.assertEquals(type, MetadataTransformer.TransformationType.valueOf(type.name()));
        }
    }

    @Test
    public void testGetTransformationFileTwice() throws Exception {
        addDescription("Test that the getTransformationFile method delivers the same transformer, when asked twice.");
        MetadataTransformer transformer = new MetadataTransformer(TestFileUtils.getTempDir());
        
        XslTransformer xslTransformer = transformer.getTransformationFile(TransformationType.ALEPH_TO_MARC21);
        Assert.assertEquals(xslTransformer, transformer.getTransformationFile(TransformationType.ALEPH_TO_MARC21));
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetTransformationFileWhenMissing() throws Exception {
        addDescription("Test the getTransformationFile method when the XSLT files are missing.");
        File emptyDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir() + "/" + UUID.randomUUID().toString());
        
        MetadataTransformer transformer = new MetadataTransformer(emptyDir);
        
        transformer.getTransformationFile(TransformationType.ALEPH_TO_MARC21);
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetTransformationFileWhenNotXsltFile() throws Exception {
        addDescription("Test the getTransformationFile method when the XSLT files are not proper XSLT files.");
        File dir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir() + "/" + UUID.randomUUID().toString());
        File xsltFile = new File(dir, TransformationType.ALEPH_TO_MARC21.scriptName);
        TestFileUtils.createFile(xsltFile, UUID.randomUUID().toString());
        
        MetadataTransformer transformer = new MetadataTransformer(dir);
        
        transformer.getTransformationFile(TransformationType.ALEPH_TO_MARC21);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testTransformMetadata() throws Exception {
        addDescription("Test trying to transform bad XML");
        MetadataTransformer transformer = new MetadataTransformer(TestFileUtils.getTempDir());
        
        ByteArrayInputStream input = new ByteArrayInputStream(UUID.randomUUID().toString().getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        transformer.transformMetadata(input, output, TransformationType.ALEPH_TO_MARC21);
    }
}
