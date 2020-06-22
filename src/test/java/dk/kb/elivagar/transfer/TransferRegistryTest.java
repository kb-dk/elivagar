package dk.kb.elivagar.transfer;

import dk.kb.elivagar.testutils.TestFileUtils;
import dk.kb.elivagar.utils.ChecksumUtils;
import dk.kb.elivagar.utils.FileUtils;
import dk.kb.elivagar.utils.StreamUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

@SuppressWarnings("ResultOfMethodCallIgnored")
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
        long time = new Random().nextLong();
        Date d = new Date(time);
        
        TransferRegistry registry = new TransferRegistry(bookDir);
        Assert.assertFalse(registry.hasBeenIngested());
        
        registry.setIngestDate(d);
        Assert.assertTrue(registry.hasBeenIngested());
        Assert.assertEquals(registry.getIngestDate().getTime(), time);
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
        long time = new Random().nextLong();
        Date d = new Date(time);
        
        TransferRegistry registry = new TransferRegistry(bookDir);
        Assert.assertFalse(registry.hasBeenIngested());
        
        registry.setUpdateDate(d);
        Assert.assertTrue(registry.hasBeenIngested());
        Assert.assertEquals(registry.getLatestUpdateDate().getTime(), time);
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
        long time = new Random().nextLong();
        Date d = new Date(time);
        
        TransferRegistry registry = new TransferRegistry(bookDir);
        Assert.assertFalse(registry.hasBeenIngested());
        
        registry.setIngestDate(d);
        Assert.assertTrue(registry.hasBeenIngested());
        Assert.assertEquals(registry.getLatestUpdateDate().getTime(), time);
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

    @Test
    public void testHasFileEntryFalseCases() throws IOException {
        addDescription("Test the hasFileEntry method for the cases, when it does not have the right parts in the registry file.");
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        TransferRegistry registry = new TransferRegistry(bookDir);

        addStep("Create book", "Not in the registry already");
        File bookFile = new File(bookDir, bookDir.getName() + ".suffix");
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());

        Assert.assertFalse(registry.hasFileEntry(bookFile));

        addStep("Add date line with no content", "Still no entry - needs date.");
        registry.writeLine(TransferRegistry.LINE_PREFIX_FILE_DATE + bookFile.getName() + TransferRegistry.LINE_FILENAME_VALUE_SEPARATOR);
        Assert.assertFalse(registry.hasFileEntry(bookFile));

        addStep("Add date line with the date", "Still no entry - missing the checksum line.");
        registry.writeLine(TransferRegistry.LINE_PREFIX_FILE_DATE + bookFile.getName() + TransferRegistry.LINE_FILENAME_VALUE_SEPARATOR + bookFile.lastModified());
        Assert.assertFalse(registry.hasFileEntry(bookFile));

        addStep("Add checksum line with no content", "Still no entry - needs checksum value.");
        registry.writeLine(TransferRegistry.LINE_PREFIX_CHECKSUM + bookFile.getName() + TransferRegistry.LINE_FILENAME_VALUE_SEPARATOR);
        Assert.assertFalse(registry.hasFileEntry(bookFile));

        addStep("Add checksum line with checksum", "Has entry.");
        registry.writeLine(TransferRegistry.LINE_PREFIX_CHECKSUM + bookFile.getName() + TransferRegistry.LINE_FILENAME_VALUE_SEPARATOR + "checksum");

        Assert.assertTrue(registry.hasFileEntry(bookFile));
    }

    @Test
    public void testHasFileEntry() throws IOException {
        addDescription("Test the hasFileEntry method");
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        TransferRegistry registry = new TransferRegistry(bookDir);

        addStep("Setup the file", "Not in the registry already");
        File bookFile = new File(bookDir, bookDir.getName() + ".suffix");
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());

        Assert.assertFalse(registry.hasFileEntry(bookFile));

        addStep("Insert the file into the registry", "Now the registry has entry");
        registry.setChecksumAndDate(bookFile);
        Assert.assertTrue(registry.hasFileEntry(bookFile));
    }

    @Test
    public void testSetChecksumAndDate() throws IOException {
        addDescription("Test the setChecksumAndDate method");
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        TransferRegistry registry = new TransferRegistry(bookDir);

        Assert.assertFalse(registry.registryFile.exists());

        addStep("Setup the file", "Not in the registry already");
        File bookFile = new File(bookDir, bookDir.getName() + ".suffix");
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());

        Assert.assertFalse(registry.hasFileEntry(bookFile));
        Assert.assertFalse(registry.registryFile.exists());

        addStep("Insert the file into the registry", "Now the registry has entry");
        registry.setChecksumAndDate(bookFile);

        Assert.assertTrue(registry.registryFile.exists());

        Assert.assertTrue(registry.hasFileEntry(bookFile));
        Assert.assertTrue(registry.getLatestEntryWithPrefix(TransferRegistry.LINE_PREFIX_CHECKSUM).contains(bookFile.getName()));
        Assert.assertTrue(registry.getLatestEntryWithPrefix(TransferRegistry.LINE_PREFIX_FILE_DATE).contains(bookFile.getName()));
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testSetChecksumAndDateFailure() throws IOException {
        addDescription("Test the setChecksumAndDate method");
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        TransferRegistry registry = new TransferRegistry(bookDir);

        Assert.assertFalse(registry.registryFile.exists());

        addStep("Insert non-existing file", "Failes");
        File bookFile = new File(bookDir, bookDir.getName() + ".suffix");
        Assert.assertFalse(bookFile.exists());
        registry.setChecksumAndDate(bookFile);
    }

    @Test
    public void testUpdateFileEntries() throws IOException {
        addDescription("Test the updateFileEntries method");
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        TransferRegistry registry = new TransferRegistry(bookDir);

        Assert.assertFalse(registry.registryFile.exists());

        addStep("Insert non-existing file", "Failes");
        File bookFile1 = new File(bookDir, bookDir.getName() + ".suffix1");
        TestFileUtils.createFile(bookFile1, UUID.randomUUID().toString());
        File bookFile2 = new File(bookDir, bookDir.getName() + ".suffix2");
        TestFileUtils.createFile(bookFile2, UUID.randomUUID().toString());

        registry.updateFileEntries(Arrays.asList(bookFile1, bookFile2));

        Assert.assertTrue(registry.registryFile.exists());

        Assert.assertTrue(registry.hasFileEntry(bookFile1));
        Assert.assertTrue(registry.hasFileEntry(bookFile2));

        String registryContent = StreamUtils.extractInputStreamAsString(new FileInputStream(registry.registryFile));
        Assert.assertTrue(registryContent.contains(bookFile1.getName()));
        Assert.assertTrue(registryContent.contains(bookFile2.getName()));
    }

    @Test
    public void testVerifyFile() throws IOException {
        addDescription("Test the verifyFile method");
        File bookDir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsoluteFile() + "/" + UUID.randomUUID().toString());
        TransferRegistry registry = new TransferRegistry(bookDir);

        addStep("Create book", "Not in the registry already");
        File bookFile = new File(bookDir, bookDir.getName() + ".suffix");
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());

        addStep("Not in registry", "No verification");
        Assert.assertFalse(registry.verifyFile(bookFile));

//        addStep("Add date line with no date", "No verification");
//        registry.writeLine(TransferRegistry.LINE_PREFIX_FILE_DATE + bookFile.getName() + TransferRegistry.LINE_FILENAME_VALUE_SEPARATOR);
//        Assert.assertFalse(registry.verifyFile(bookFile));
//
//        addStep("Add date line with the correct date", "Verification!!!");
        registry.writeLine(TransferRegistry.LINE_PREFIX_FILE_DATE + bookFile.getName() + TransferRegistry.LINE_FILENAME_VALUE_SEPARATOR + bookFile.lastModified());
//        Assert.assertTrue(registry.verifyFile(bookFile));
//
//        addStep("Add date line with the incorrect date", "No verification - needs also checksum");
//        registry.writeLine(TransferRegistry.LINE_PREFIX_FILE_DATE + bookFile.getName() + TransferRegistry.LINE_FILENAME_VALUE_SEPARATOR + "1234567890000");
//        Assert.assertFalse(registry.verifyFile(bookFile));
//
        addStep("Add checksum line with no data", "No verification");
        registry.writeLine(TransferRegistry.LINE_PREFIX_CHECKSUM + bookFile.getName() + TransferRegistry.LINE_FILENAME_VALUE_SEPARATOR);
        Assert.assertFalse(registry.verifyFile(bookFile));

        addStep("Add checksum line with wrong checksum", "No verification");
        registry.writeLine(TransferRegistry.LINE_PREFIX_CHECKSUM + bookFile.getName() + TransferRegistry.LINE_FILENAME_VALUE_SEPARATOR + "checksum");
        Assert.assertFalse(registry.verifyFile(bookFile));

        addStep("Add checksum line with the correct checksum", "Verification!!!");
        String checksum = ChecksumUtils.generateMD5Checksum(new FileInputStream(bookFile));
        registry.writeLine(TransferRegistry.LINE_PREFIX_CHECKSUM + bookFile.getName() + TransferRegistry.LINE_FILENAME_VALUE_SEPARATOR + checksum);
        Assert.assertTrue(registry.verifyFile(bookFile));
    }

}
