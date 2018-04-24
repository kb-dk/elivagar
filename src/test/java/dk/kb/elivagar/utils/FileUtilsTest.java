package dk.kb.elivagar.utils;

import java.io.File;
import java.nio.file.Files;
import java.util.Collection;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.testutils.TestFileUtils;

public class FileUtilsTest extends ExtendedTestCase {

    @BeforeClass
    public void setup() throws Exception {
        TestFileUtils.setup();
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
    
    @Test
    public void testCopyDirectory() throws Exception {
        addDescription("Test the copy directory method.");
        File dir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString());
        File origDir = FileUtils.createDirectory(dir.getAbsolutePath() + "/" + UUID.randomUUID().toString());
        File destinationDir = FileUtils.createDirectory(dir.getAbsolutePath() + "/" + UUID.randomUUID().toString());
        
        File origFile = new File(origDir, UUID.randomUUID().toString());
        TestFileUtils.createFile(origFile, UUID.randomUUID().toString());
        
        File contentForLink = new File(dir, UUID.randomUUID().toString());
        TestFileUtils.createFile(contentForLink, UUID.randomUUID().toString());
        File linkFile = new File(origDir, contentForLink.getName());
        Files.createSymbolicLink(linkFile.toPath(), contentForLink.toPath());
        
        File destinationFile = new File(destinationDir, origFile.getName());
        File destinationLink = new File(destinationDir, linkFile.getName());
        
        Assert.assertEquals(origDir.list().length, 2);
        Assert.assertEquals(destinationDir.list().length, 0);
        Assert.assertTrue(origFile.isFile());
        Assert.assertTrue(Files.isSymbolicLink(linkFile.toPath()));
        
        
        FileUtils.copyDirectory(origDir, destinationDir);
        
        Assert.assertEquals(origDir.list().length, 2);
        Assert.assertEquals(destinationDir.list().length, 2);
        Assert.assertTrue(origFile.isFile());
        Assert.assertTrue(Files.isSymbolicLink(linkFile.toPath()));

        Assert.assertTrue(destinationFile.exists());
        Assert.assertTrue(destinationFile.isFile());
        Assert.assertTrue(destinationLink.isFile());
        Assert.assertFalse(Files.isSymbolicLink(destinationLink.toPath()));
        Assert.assertTrue(destinationLink.exists());
    }

    @Test
    public void testCopyDirectoryWithNewDirectory() throws Exception {
        addDescription("Test that copy directory method will create a new directory at the destination, if it does not already exists.");
        File dir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString());
        File origDir = FileUtils.createDirectory(dir.getAbsolutePath() + "/" + UUID.randomUUID().toString());
        File destinationDir = new File(dir.getAbsolutePath(), UUID.randomUUID().toString());
        
        Assert.assertTrue(origDir.exists());
        Assert.assertTrue(origDir.isDirectory());
        Assert.assertFalse(destinationDir.exists());
        
        FileUtils.copyDirectory(origDir, destinationDir);
        
        Assert.assertTrue(destinationDir.exists());
        Assert.assertTrue(destinationDir.isDirectory());
    }
    
    @Test
    public void testGetFilesInDirectory() throws Exception {
        addDescription("Test the getFilesInDirectory method");
        File dir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString());
        File testFile = new File(dir, UUID.randomUUID().toString());
        TestFileUtils.createFile(testFile, UUID.randomUUID().toString());
        
        Collection<File> files = FileUtils.getFilesInDirectory(dir);
        Assert.assertEquals(files.size(), 1);
        Assert.assertEquals(files.iterator().next(), testFile);
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testGetFilesInDirectoryFailure() throws Exception {
        addDescription("Test the getFilesInDirectory method failure, when the list is null");
        File dir = Mockito.mock(File.class);
        Mockito.when(dir.isDirectory()).thenReturn(true);
        Mockito.when(dir.listFiles()).thenReturn(null);
        
        FileUtils.getFilesInDirectory(dir);
    }
}
