package dk.kb.elivagar.statistics;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.exception.ArgumentCheck;
import dk.kb.elivagar.pubhub.validator.FileSuffixValidator;
import dk.kb.elivagar.testutils.TestFileUtils;
import dk.kb.elivagar.utils.FileUtils;

public class ElivagarStatisticsTest extends ExtendedTestCase {

    @BeforeClass
    public void setup() {
        TestFileUtils.setup();
    }
    
    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }
    
    @Test
    public void testInstantiation() {
        addDescription("Test the instantiation");
        ElivagarStatistics statistics = new ElivagarStatistics();
        
        Assert.assertEquals(statistics.totalCount, 0);
        Assert.assertEquals(statistics.newDirCount, 0);
        Assert.assertEquals(statistics.numberOfOtherCount, 0);
        Assert.assertTrue(statistics.numberOfFiles.isEmpty());
        Assert.assertTrue(statistics.numberOfNewFiles.isEmpty());
        
        Assert.assertEquals(statistics.totalCount, statistics.getTotalCount());
        Assert.assertEquals(statistics.newDirCount, statistics.getNewDirCount());
        Assert.assertEquals(statistics.numberOfOtherCount, statistics.getNonStandardNamedCount());
        Assert.assertEquals(statistics.numberOfFiles, statistics.getMapOfFileSuffices());
        Assert.assertEquals(statistics.numberOfNewFiles, statistics.getMapOfNewFileSuffices());
    }
    
    @Test
    public void testTraverseBaseDir() throws IOException {
        addDescription("Test the traverseBaseDir when it has a single empty book-directory");
        ElivagarStatistics statistics = new ElivagarStatistics();
        
        File baseDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString());
        TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/" + UUID.randomUUID().toString());
        
        statistics.traverseBaseDir(baseDir, Long.MAX_VALUE);
        
        Assert.assertEquals(statistics.totalCount, 1);
        Assert.assertEquals(statistics.newDirCount, 0);
        Assert.assertEquals(statistics.numberOfOtherCount, 0);
        Assert.assertTrue(statistics.numberOfFiles.isEmpty());
        Assert.assertTrue(statistics.numberOfNewFiles.isEmpty());
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testTraverseBaseDirFailureOnFile() throws Exception {
        addDescription("Test the calculateStatistics method, when the base directory is a file and not a directory.");
        ElivagarStatistics statistics = new ElivagarStatistics();
        
        File badBookDir = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        TestFileUtils.createFile(badBookDir, UUID.randomUUID().toString());
        
        statistics.traverseBaseDir(badBookDir, 0);
    }
    
    @Test
    public void testCalculateStatisticsOnBookDirOnFile() throws Exception {
        addDescription("Test the calculateStatisticsOnBookDir method on a file and not a directory.");
        ElivagarStatistics statistics = new ElivagarStatistics();
        
        File baseDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir() + "/" + UUID.randomUUID().toString());
        File bookFile = new File(baseDir, UUID.randomUUID().toString());
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        statistics.calculateStatisticsOnBookDir(bookFile, 0);
        
        Assert.assertEquals(statistics.totalCount, 0);
    }

    @Test
    public void testCalculateStatisticsOnBookDirWithNewFile() throws Exception {
        addDescription("Test the calculateStatisticsOnBookDir method on a file and not a directory.");
        ElivagarStatistics statistics = new ElivagarStatistics();
        
        String suffix = UUID.randomUUID().toString();
        
        File bookDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir() + "/" + UUID.randomUUID().toString());
        
        File bookFile = new File(bookDir, bookDir.getName() + suffix);
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        statistics.calculateStatisticsOnBookDir(bookDir, 0);
        
        Assert.assertEquals(statistics.totalCount, 1);
        Assert.assertEquals(statistics.getNonStandardNamedCount(), 0);
        Assert.assertEquals(statistics.getMapOfFileSuffices().getValue(suffix), 1);
        Assert.assertEquals(statistics.getMapOfNewFileSuffices().getValue(suffix), 1);
    }

    @Test
    public void testCalculateStatisticsOnBookDirWithOldFile() throws Exception {
        addDescription("Test the calculateStatisticsOnBookDir method on a file and not a directory.");
        ElivagarStatistics statistics = new ElivagarStatistics();
        
        String suffix = UUID.randomUUID().toString();
        
        File bookDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir() + "/" + UUID.randomUUID().toString());
        
        File bookFile = new File(bookDir, bookDir.getName() + suffix);
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        statistics.calculateStatisticsOnBookDir(bookDir, Long.MAX_VALUE);
        
        Assert.assertEquals(statistics.totalCount, 1);
        Assert.assertEquals(statistics.getNonStandardNamedCount(), 0);
        Assert.assertEquals(statistics.getMapOfFileSuffices().getValue(suffix), 1);
        Assert.assertEquals(statistics.getMapOfNewFileSuffices().getValue(suffix), 0);
    }

    @Test
    public void testCalculateStatisticsOnBookDirWithNonStandardNamedFile() throws Exception {
        addDescription("Test the calculateStatisticsOnBookDir method on a file and not a directory.");
        ElivagarStatistics statistics = new ElivagarStatistics();
        
        String suffix = UUID.randomUUID().toString();
        
        File bookDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir() + "/" + UUID.randomUUID().toString());
        
        File bookFile = new File(bookDir, UUID.randomUUID().toString() + suffix);
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        statistics.calculateStatisticsOnBookDir(bookDir, Long.MAX_VALUE);
        
        Assert.assertEquals(statistics.totalCount, 1);
        Assert.assertEquals(statistics.getNonStandardNamedCount(), 1);
        Assert.assertEquals(statistics.getMapOfFileSuffices().getValue(suffix), 0);
        Assert.assertEquals(statistics.getMapOfNewFileSuffices().getValue(suffix), 0);
    }

    @Test
    public void testCheckNewDirectory() throws IOException {
        addDescription("Test the checkNewDirectory method");
        ElivagarStatistics statistics = new ElivagarStatistics();
        File dir = FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString());
        
        addStep("Test with the date in the future", "The directory should not be counted.");
        Assert.assertEquals(statistics.getNewDirCount(), 0);
        statistics.checkNewDirectory(dir, Long.MAX_VALUE);
        Assert.assertEquals(statistics.getNewDirCount(), 0);
        
        addStep("Test with the date of epoch", "The directory should be counted.");
        Assert.assertEquals(statistics.getNewDirCount(), 0);
        statistics.checkNewDirectory(dir, 0);
        Assert.assertEquals(statistics.getNewDirCount(), 1);
    }
}
