package dk.kb.elivagar.transfer;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.testutils.TestFileUtils;
import dk.kb.elivagar.utils.FileUtils;

public class TransferRegistryTest extends ExtendedTestCase {
    
    @BeforeClass
    public void setup() {
        TestFileUtils.setup();
    }
    
    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }
    
    @Test
    public void testInstantiation() throws IOException {
        addDescription("Test the instantiation of the registry. Is should neither have been ingested nor have any dates.");
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        
        TransferRegistry registry = new TransferRegistry(bookDir);
        
        Assert.assertFalse(registry.hasBeenIngested());
        Assert.assertNull(registry.getIngestDate());
        Assert.assertNull(registry.getLatestUpdateDate());
    }
    
    @Test
    public void testIngestDate() throws IOException {
        addDescription("Test successfully setting and getting the date for ingest.");
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        Long time = new Random().nextLong();
        Date d = new Date(time);
        
        TransferRegistry registry = new TransferRegistry(bookDir);
        Assert.assertFalse(registry.hasBeenIngested());
        
        registry.setIngestDate(d);
        Assert.assertTrue(registry.hasBeenIngested());
        Assert.assertEquals(registry.getIngestDate().getTime(), time.longValue());
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testSetIngestDateFailure() throws IOException {
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        try {
            bookDir.setWritable(false);
            TransferRegistry registry = new TransferRegistry(bookDir);
            registry.setIngestDate(new Date());
        } finally {
            bookDir.setWritable(true);
        }
    }
    
    @Test
    public void testGetIngestDateFailure() throws IOException {
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        TransferRegistry registry = new TransferRegistry(bookDir);
        try {
            registry.setIngestDate(new Date());
            
            registry.registryFile.setReadable(false);
            Assert.assertNull(registry.getIngestDate());
        } finally {
            registry.registryFile.setReadable(true);
        }
    }
    
    @Test
    public void testGetIngestDateWhenOnlyUpdate() throws IOException {
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        TransferRegistry registry = new TransferRegistry(bookDir);
        registry.setUpdateDate(new Date());

        Assert.assertNull(registry.getIngestDate());
    }

    @Test
    public void testUpdateDate() throws IOException {
        addDescription("Test successfully setting and getting the date for update.");
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        Long time = new Random().nextLong();
        Date d = new Date(time);
        
        TransferRegistry registry = new TransferRegistry(bookDir);
        Assert.assertFalse(registry.hasBeenIngested());
        
        registry.setUpdateDate(d);
        Assert.assertTrue(registry.hasBeenIngested());
        Assert.assertEquals(registry.getLatestUpdateDate().getTime(), time.longValue());
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testSetUpdateDateFailure() throws IOException {
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        try {
            TransferRegistry registry = new TransferRegistry(bookDir);
            bookDir.setWritable(false);
            registry.setUpdateDate(new Date());
        } finally {
            bookDir.setWritable(true);
        }
    }
    
    @Test
    public void testGetLatestUpdateDateWhenOnlyIngest() throws Exception {
        addDescription("Test successfully getting the date for update when it has only been ingested.");
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        Long time = new Random().nextLong();
        Date d = new Date(time);
        
        TransferRegistry registry = new TransferRegistry(bookDir);
        Assert.assertFalse(registry.hasBeenIngested());
        
        registry.setIngestDate(d);
        Assert.assertTrue(registry.hasBeenIngested());
        Assert.assertEquals(registry.getLatestUpdateDate().getTime(), time.longValue());
    }
    
    @Test
    public void testGetLatestUpdateDateFailure() throws IOException {
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        TransferRegistry registry = new TransferRegistry(bookDir);
        try {
            registry.setIngestDate(new Date());
            
            registry.registryFile.setReadable(false);
            Assert.assertNull(registry.getLatestUpdateDate());
        } finally {
            registry.registryFile.setReadable(true);
        }
    }    
}
