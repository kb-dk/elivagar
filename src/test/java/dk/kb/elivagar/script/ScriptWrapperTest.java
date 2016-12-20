package dk.kb.elivagar.script;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.script.ScriptWrapper;
import dk.kb.elivagar.utils.FileUtils;
import dk.kb.elivagar.utils.StreamUtils;

public class ScriptWrapperTest extends ExtendedTestCase {


    File tempDir;
    File exampleScript;

    @BeforeClass
    public void setup() throws IOException {
        tempDir = FileUtils.createDirectory("tempDir");    

        File origScript = new File("src/main/resources/example_script.sh");
        exampleScript = new File(tempDir, origScript.getName());

        StreamUtils.copyInputStreamToOutputStream(new FileInputStream(origScript), new FileOutputStream(exampleScript));

    }

    @Test
    public void testScript() {
        File testScript = new File("src/test/resources/test-script.sh");
        Assert.assertTrue(testScript.isFile());

        ScriptWrapper sw = new ScriptWrapper(testScript);
        sw.callVoidScript("THIS IS THE ARGUMENT!!!");
    }

    @Test
    public void testScriptWithArguments() throws IOException {
        String name = "download_" + UUID.randomUUID().toString();
        ScriptWrapper sw = new ScriptWrapper(exampleScript);
        sw.callVoidScript(name, tempDir.getAbsolutePath());
        Assert.assertTrue(new File(tempDir, name).isFile());
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testScriptWithNoArguments() throws IOException {
        ScriptWrapper sw = new ScriptWrapper(exampleScript);
        sw.callVoidScript(new String[0]);
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testFailureWhenBadScriptPath() throws IOException {
        File testScript = new File(UUID.randomUUID().toString());
        new ScriptWrapper(testScript);
    }
}
