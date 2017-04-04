package dk.kb.elivagar.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.script.ScriptWrapper;
import dk.kb.elivagar.utils.FileUtils;
import dk.kb.elivagar.utils.StreamUtils;

public class CharacterizationScriptWrapperTest extends ExtendedTestCase {


    File tempDir;
    File exampleScript;

    @BeforeClass
    public void setup() throws IOException {
        tempDir = FileUtils.createDirectory("tempDir");    

        File origScript = new File("src/main/resources/bin/run_fits.sh");
        exampleScript = new File(tempDir, origScript.getName());

        StreamUtils.copyInputStreamToOutputStream(new FileInputStream(origScript), new FileOutputStream(exampleScript));
    }

    @Test
    public void testScript() {
        File outputFile = new File(tempDir, UUID.randomUUID().toString());
        
        System.err.println("Path: " + exampleScript.getAbsolutePath());
        Assert.assertTrue(exampleScript.exists());
        Assert.assertFalse(outputFile.exists());
        
        try {
            CharacterizationScriptWrapper csw = new CharacterizationScriptWrapper(exampleScript);
            csw.execute(exampleScript, outputFile);
        } catch(IllegalStateException e) {
            throw new SkipException("Failed to run characterization, skipping test.", e);
        }
        
        Assert.assertTrue(outputFile.exists());
        Assert.assertTrue(outputFile.length() > 0);
    }    
}
