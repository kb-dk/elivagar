package dk.kb.elivagar.statistics;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.exception.ArgumentCheck;
import dk.kb.elivagar.pubhub.validator.FileSuffixValidator;
import dk.kb.elivagar.testutils.TestConfigurations;
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
        Configuration conf = TestConfigurations.getConfigurationForTest();
        ElivagarStatistics statistics = new ElivagarStatistics(conf);
        
        Assert.assertEquals(statistics.totalCount, 0);
        Assert.assertEquals(statistics.newDirCount, 0);
        Assert.assertEquals(statistics.numberOfOtherCount, 0);
        Assert.assertTrue(statistics.numberOfFiles.isEmpty());
        Assert.assertTrue(statistics.numberOfNewFiles.isEmpty());
        
        Assert.assertEquals(statistics.totalCount, statistics.getTotalCount());
        Assert.assertEquals(statistics.newDirCount, statistics.getNewDirCount());
        Assert.assertEquals(statistics.numberOfOtherCount, statistics.getNonStandardNamedCount());
        Assert.assertEquals(statistics.numberOfFiles, statistics.getMapOfFileSuffixes());
        Assert.assertEquals(statistics.numberOfNewFiles, statistics.getMapOfNewFileSuffixes());
    }
    
    @Test
    public void testTraverseBaseDir() throws IOException {
        addDescription("Test the traverseBaseDir when it has a single empty book-directory");
        Configuration conf = TestConfigurations.getConfigurationForTest();
        ElivagarStatistics statistics = new ElivagarStatistics(conf);
        
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
        Configuration conf = TestConfigurations.getConfigurationForTest();
        ElivagarStatistics statistics = new ElivagarStatistics(conf);
        
        File badBookDir = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        TestFileUtils.createFile(badBookDir, UUID.randomUUID().toString());
        
        statistics.traverseBaseDir(badBookDir, 0);
    }
    
    @Test
    public void testCalculateStatisticsOnBookDirOnFile() throws Exception {
        addDescription("Test the calculateStatisticsOnBookDir method on a file and not a directory.");
        Configuration conf = TestConfigurations.getConfigurationForTest();
        ElivagarStatistics statistics = new ElivagarStatistics(conf);
        
        File baseDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir() + "/" + UUID.randomUUID().toString());
        File bookFile = new File(baseDir, UUID.randomUUID().toString());
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        statistics.calculateStatisticsOnBookDir(bookFile, 0);
        
        Assert.assertEquals(statistics.totalCount, 0);
    }

    @Test
    public void testCalculateStatisticsOnBookDirWithNewFile() throws Exception {
        addDescription("Test the calculateStatisticsOnBookDir method on a file and not a directory.");
        Configuration conf = TestConfigurations.getConfigurationForTest();
        ElivagarStatistics statistics = new ElivagarStatistics(conf);
        
        String suffix = UUID.randomUUID().toString();
        
        File bookDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir() + "/" + UUID.randomUUID().toString());
        
        File bookFile = new File(bookDir, bookDir.getName() + suffix);
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        statistics.calculateStatisticsOnBookDir(bookDir, 0);
        
        Assert.assertEquals(statistics.totalCount, 1);
        Assert.assertEquals(statistics.getNonStandardNamedCount(), 0);
        Assert.assertEquals(statistics.getMapOfFileSuffixes().getValue(suffix), 1);
        Assert.assertEquals(statistics.getMapOfNewFileSuffixes().getValue(suffix), 1);
    }

    @Test
    public void testCalculateStatisticsOnBookDirWithOldFile() throws Exception {
        addDescription("Test the calculateStatisticsOnBookDir method on a file and not a directory.");
        Configuration conf = TestConfigurations.getConfigurationForTest();
        ElivagarStatistics statistics = new ElivagarStatistics(conf);
        
        String suffix = UUID.randomUUID().toString();
        
        File bookDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir() + "/" + UUID.randomUUID().toString());
        
        File bookFile = new File(bookDir, bookDir.getName() + suffix);
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        statistics.calculateStatisticsOnBookDir(bookDir, Long.MAX_VALUE);
        
        Assert.assertEquals(statistics.totalCount, 1);
        Assert.assertEquals(statistics.getNonStandardNamedCount(), 0);
        Assert.assertEquals(statistics.getMapOfFileSuffixes().getValue(suffix), 1);
        Assert.assertEquals(statistics.getMapOfNewFileSuffixes().getValue(suffix), 0);
    }

    @Test
    public void testCalculateStatisticsOnBookDirWithNonStandardNamedFile() throws Exception {
        addDescription("Test the calculateStatisticsOnBookDir method when the directory has a file with an non-standard filename.");
        Configuration conf = TestConfigurations.getConfigurationForTest();
        ElivagarStatistics statistics = new ElivagarStatistics(conf);
        
        String suffix = "" + new Random().nextBoolean();
        
        File bookDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir() + "/" + UUID.randomUUID().toString());
        
        File bookFile = new File(bookDir, UUID.randomUUID().toString() + suffix);
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        statistics.calculateStatisticsOnBookDir(bookDir, Long.MAX_VALUE);
        
        Assert.assertEquals(statistics.totalCount, 1);
        Assert.assertEquals(statistics.getNonStandardNamedCount(), 1);
        Assert.assertEquals(statistics.getMapOfFileSuffixes().getValue(suffix), 0);
        Assert.assertEquals(statistics.getMapOfNewFileSuffixes().getValue(suffix), 0);
    }

    @Test
    public void testCalculateStatisticsOnBookDirWithNonSuffix() throws Exception {
        addDescription("Test the calculateStatisticsOnBookDir method when the directory has a file with an non-standard suffix.");
        Configuration conf = TestConfigurations.getConfigurationForTest();
        ElivagarStatistics statistics = new ElivagarStatistics(conf);
        
        String suffix = "suffix" + new Random().nextInt(100);
        
        File bookDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir() + "/" + UUID.randomUUID().toString());
        
        File bookFile = new File(bookDir, bookDir.getName() + suffix);
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        statistics.calculateStatisticsOnBookDir(bookDir, 0);
        
        Assert.assertEquals(statistics.totalCount, 1);
        Assert.assertEquals(statistics.getNonStandardNamedCount(), 0);
        Assert.assertEquals(statistics.getMapOfFileSuffixes().getValue(suffix), 1);
        Assert.assertEquals(statistics.getMapOfNewFileSuffixes().getValue(suffix), 1);
        
        statistics.printStatistics(System.out);
    }

    @Test
    public void testCheckNewDirectory() throws IOException {
        addDescription("Test the checkNewDirectory method");
        Configuration conf = TestConfigurations.getConfigurationForTest();
        ElivagarStatistics statistics = new ElivagarStatistics(conf);
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
