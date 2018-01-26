package dk.kb.elivagar.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.testutils.TestFileUtils;
import dk.kb.elivagar.utils.StreamUtils;

public class CharacterizationScriptWrapperTest extends ExtendedTestCase {

    File origScript;
    File exampleScript;

    @BeforeClass
    public void setup() throws IOException {
        TestFileUtils.setup();    

        origScript = new File("src/main/resources/bin/run_fits.sh");
        exampleScript = new File(TestFileUtils.getTempDir(), origScript.getName());

        StreamUtils.copyInputStreamToOutputStream(new FileInputStream(origScript), new FileOutputStream(exampleScript));
    }
    
    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }

    @Test
    public void testScript() {
        addDescription("Test the characterization");
        File outputFile = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        
        Assert.assertTrue(exampleScript.exists());
        Assert.assertFalse(outputFile.exists());
        
        try {
            CharacterizationScriptWrapper csw = new CharacterizationScriptWrapper(origScript);
            csw.execute(exampleScript, outputFile);
        } catch(IllegalStateException e) {
            throw new SkipException("Failed to run characterization, skipping test.", e);
        }
        
        Assert.assertTrue(outputFile.exists());
        Assert.assertTrue(outputFile.length() > 0);
    }    
    
    @Test
    public void testScriptForOutFilenameContainingSpace() {
        addDescription("Test that characterization will not be run, when the output filename contains a space.");
        File outputFile = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString() + " " + UUID.randomUUID().toString());
        
        Assert.assertTrue(exampleScript.exists());
        Assert.assertFalse(outputFile.exists());
        
        try {
            CharacterizationScriptWrapper csw = new CharacterizationScriptWrapper(origScript);
            csw.execute(exampleScript, outputFile);
        } catch(IllegalStateException e) {
            throw new SkipException("Failed to run characterization, skipping test.", e);
        }
        
        Assert.assertFalse(outputFile.exists());
    }
    
    @Test
    public void testScriptForInFilenameContainingSpace() throws IOException {
        addDescription("Test that characterization will not be run, when the input filename contains a space.");
        File outputFile = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        File inputFile = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString() + " " + UUID.randomUUID().toString());
        TestFileUtils.createFile(inputFile, UUID.randomUUID().toString());
        
        Assert.assertTrue(inputFile.exists());
        Assert.assertFalse(outputFile.exists());
        
        try {
            CharacterizationScriptWrapper csw = new CharacterizationScriptWrapper(origScript);
            csw.execute(inputFile, outputFile);
        } catch(IllegalStateException e) {
            throw new SkipException("Failed to run characterization, skipping test.", e);
        }
        
        Assert.assertFalse(outputFile.exists());
    }    
}
