package dk.kb.elivagar.utils;

import java.io.File;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.testutils.TestFileUtils;

public class FileUtilsTest extends ExtendedTestCase {

    @BeforeClass
    public void setup() throws Exception {
        TestFileUtils.setupTempDir();
    }
    
    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }
    
    @Test
    public void testCreateDirectory() throws Exception {
        File f = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString());
        Assert.assertTrue(f.exists());
        Assert.assertTrue(f.isDirectory());
    }
    
    @Test
    public void testConstructor() {
        new FileUtils();
    }
    
    @Test
    public void testDeleteOnNull() throws Exception {
        FileUtils.deleteFile(null);
    }
    
    @Test
    public void testDeleteOnNonExistingFile() throws Exception {
        FileUtils.deleteFile(new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString()));
    }
    
    @Test
    public void testDeleteFile() throws Exception {   
        File testDelete = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        TestFileUtils.createFile(testDelete, UUID.randomUUID().toString());
        
        Assert.assertTrue(testDelete.exists());
        FileUtils.deleteFile(testDelete);
        Assert.assertFalse(testDelete.exists());
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testDeleteFileFailure() throws Exception {   
        File testNonDelete = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        TestFileUtils.createFile(testNonDelete, UUID.randomUUID().toString());
        try {
            TestFileUtils.getTempDir().setWritable(false);
            Assert.assertTrue(testNonDelete.exists());
            FileUtils.deleteFile(testNonDelete);
            Assert.assertTrue(testNonDelete.exists());
        } finally {
            TestFileUtils.getTempDir().setWritable(true);
        }
    }
}
