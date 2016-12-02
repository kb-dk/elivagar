package dk.kb.elivagar.utils;

import java.io.File;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FileUtilsTest extends ExtendedTestCase {

    @Test
    public void testCreateDirectory() throws Exception {
        File f = FileUtils.createDirectory("tempDir");
        Assert.assertTrue(f.exists());
        Assert.assertTrue(f.isDirectory());
    }
    
    @Test
    public void testConstructor() {
        new FileUtils();
    }
}
