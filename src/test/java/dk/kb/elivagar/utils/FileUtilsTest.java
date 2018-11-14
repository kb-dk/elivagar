package dk.kb.elivagar.utils;

import java.io.File;
import java.io.IOException;
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
    
    @Test
    public void testMoveFileOntoEmptyLocation() throws Exception {
        addDescription("Test the moveFile method with non-existing file as destination.");
        File orig = TestFileUtils.createTempFile(UUID.randomUUID().toString());
        File dest = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        
        String origPath = orig.getAbsolutePath();
        String destPath = dest.getAbsolutePath();
        
        Assert.assertTrue(new File(origPath).exists());
        Assert.assertFalse(new File(destPath).exists());
        
        FileUtils.moveFile(orig, dest);

        Assert.assertFalse(new File(origPath).exists());
        Assert.assertTrue(new File(destPath).exists());
    }
    
    @Test
    public void testMoveFileOverride() throws Exception {
        addDescription("Test the moveFile method with existing file as destination.");
        String content = UUID.randomUUID().toString();
        File orig = TestFileUtils.createTempFile(content);
        File dest = TestFileUtils.createTempFile(UUID.randomUUID().toString());
        
        String origPath = orig.getAbsolutePath();
        String destPath = dest.getAbsolutePath();
        
        Assert.assertTrue(new File(origPath).exists());
        Assert.assertTrue(new File(destPath).exists());
        
        FileUtils.moveFile(orig, dest);

        Assert.assertFalse(new File(origPath).exists());
        Assert.assertTrue(new File(destPath).exists());
        
        String newContent = TestFileUtils.readFile(new File(destPath));
        Assert.assertEquals(newContent, content);
    }
    
    @Test(expectedExceptions = IOException.class)
    public void testMoveFileSillyFail() throws Exception {
        File orig = Mockito.mock(File.class);
        File dest = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        
        Mockito.when(orig.isFile()).thenReturn(true);
        Mockito.when(orig.renameTo(Mockito.any(File.class))).thenReturn(false);
        
        FileUtils.moveFile(orig, dest);
    }
    
    @Test
    public void testAreFilesIdentical() throws Exception {
        addDescription("Test the areFilesIdentical method");
        addStep("Test with two files with identical content", "Must return true.");
        String content = UUID.randomUUID().toString();
        File f1 = TestFileUtils.createTempFile(content);
        File f2 = TestFileUtils.createTempFile(content);
        
        Assert.assertTrue(FileUtils.areFilesIdentical(f1, f2));
        
        addStep("Test with two files with different content", "Must return false.");
        File f3 = TestFileUtils.createTempFile(UUID.randomUUID().toString());

        Assert.assertFalse(FileUtils.areFilesIdentical(f1, f3));
    }
    
    @Test
    public void testMoveDirectoryNoExists() throws Exception {
        addDescription("Test the moveDirectory method, when the desination does not already exist.");
        String origPath = TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString();
        String destPath = TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString();
        
        File origDir = FileUtils.createDirectory(origPath);
        File destDir = new File(destPath);
        
        Assert.assertTrue(new File(origPath).exists());
        Assert.assertTrue(new File(origPath).isDirectory());
        Assert.assertFalse(new File(destPath).exists());
        Assert.assertFalse(new File(destPath).isDirectory());
        
        FileUtils.moveDirectory(origDir, destDir);
        
        Assert.assertFalse(new File(origPath).exists());
        Assert.assertFalse(new File(origPath).isDirectory());
        Assert.assertTrue(new File(destPath).exists());
        Assert.assertTrue(new File(destPath).isDirectory());
    }
    
    @Test(expectedExceptions = IOException.class)
    public void testMoveDirectoryDestIsFile() throws Exception {
        addDescription("Test the moveDirectory method, when the desination does not already exist.");
        File origDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString());
        File destDir = TestFileUtils.createTempFile(UUID.randomUUID().toString());
        
        Assert.assertTrue(origDir.isDirectory());
        Assert.assertTrue(destDir.exists());
        Assert.assertFalse(destDir.isDirectory());
        Assert.assertTrue(destDir.isFile());
        
        FileUtils.moveDirectory(origDir, destDir);
    }
}
