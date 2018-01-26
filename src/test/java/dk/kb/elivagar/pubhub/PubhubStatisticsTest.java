package dk.kb.elivagar.pubhub;

import static org.mockito.Mockito.*;

import java.io.File;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.pubhub.validator.FileSuffixValidator;
import dk.kb.elivagar.testutils.TestFileUtils;

public class PubhubStatisticsTest extends ExtendedTestCase {

    @BeforeClass
    public void setup() {
        TestFileUtils.setup();
    }
    
    @Test
    public void testCalculateStatistics() throws Exception {
        addDescription("Test the calculateStatistics method, when the base directory has a single file.");
        File baseDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString());
        FileSuffixValidator validator = mock(FileSuffixValidator.class);
        PubhubStatistics statistics = new PubhubStatistics(baseDir, validator);
        
        File badBookDir = new File(baseDir, UUID.randomUUID().toString());
        TestFileUtils.createFile(badBookDir, UUID.randomUUID().toString());
        
        Assert.assertEquals(statistics.count, 0);
        statistics.calculateStatistics();
        Assert.assertEquals(statistics.count, 0);
        
        verifyZeroInteractions(validator);
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testCalculateStatisticsBadBaseDir() throws Exception {
        addDescription("Test the calculateStatistics method, when the base directory is a single file.");
        File baseDir = new File(TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString());
        FileSuffixValidator validator = mock(FileSuffixValidator.class);
        PubhubStatistics statistics = new PubhubStatistics(baseDir, validator);
        
        TestFileUtils.createFile(baseDir, UUID.randomUUID().toString());
        
        Assert.assertEquals(statistics.count, 0);
        statistics.calculateStatistics();
    }
    
    @Test
    public void testCalculateStatisticsOnBookDirOnProperFile() throws Exception {
        addDescription("Test the calculateStatisticsOnBookDir method on a proper file.");
        File baseDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString());
        FileSuffixValidator validator = mock(FileSuffixValidator.class);
        PubhubStatistics statistics = new PubhubStatistics(baseDir, validator);
        
        File badBookDir = new File(baseDir, UUID.randomUUID().toString());
        TestFileUtils.createFile(badBookDir, UUID.randomUUID().toString());
        
        Assert.assertEquals(statistics.count, 0);
        statistics.calculateStatisticsOnBookDir(badBookDir);
        Assert.assertEquals(statistics.count, 0);
    }
    
    @Test
    public void testCalculateStatisticsOnBookDirBothFiles() throws Exception {
        addDescription("Test the calculateStatisticsOnBookDir method on a directory with both metadata and content file.");
        File baseDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString());
        FileSuffixValidator validator = mock(FileSuffixValidator.class);
        PubhubStatistics statistics = new PubhubStatistics(baseDir, validator);
        
        String id = UUID.randomUUID().toString();
        File bookDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/" + id);
        
        File bookFile = new File(bookDir, id + ".pdf");
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        File metadataFile = new File(bookDir, id + ".xml");
        TestFileUtils.createFile(metadataFile, UUID.randomUUID().toString());
        
        when(validator.hasValidSuffix(eq(bookFile))).thenReturn(true);
        
        Assert.assertEquals(statistics.count, 0);
        
        statistics.calculateStatisticsOnBookDir(bookDir);
        
        Assert.assertEquals(statistics.getTotalCount(), 1);
        Assert.assertEquals(statistics.getBothDataCount(), 1);
        Assert.assertEquals(statistics.getNeitherDataCount(), 0);
        Assert.assertEquals(statistics.getOnlyBookFileCount(), 0);
        Assert.assertEquals(statistics.getOnlyMetadataCount(), 0);
        
        verify(validator).hasValidSuffix(eq(bookFile));
        verifyNoMoreInteractions(validator);
    }
    
    @Test
    public void testCalculateStatisticsOnBookDirNeitherFiles() throws Exception {
        addDescription("Test the calculateStatisticsOnBookDir method on a directory with neither metadata nor content file.");
        File baseDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString());
        FileSuffixValidator validator = mock(FileSuffixValidator.class);
        PubhubStatistics statistics = new PubhubStatistics(baseDir, validator);
        
        String id = UUID.randomUUID().toString();
        File bookDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/" + id);
        
        Assert.assertEquals(statistics.count, 0);
        
        statistics.calculateStatisticsOnBookDir(bookDir);
        
        Assert.assertEquals(statistics.getTotalCount(), 1);
        Assert.assertEquals(statistics.getBothDataCount(), 0);
        Assert.assertEquals(statistics.getNeitherDataCount(), 1);
        Assert.assertEquals(statistics.getOnlyBookFileCount(), 0);
        Assert.assertEquals(statistics.getOnlyMetadataCount(), 0);

        verifyZeroInteractions(validator);
    }
    
    @Test
    public void testCalculateStatisticsOnBookDirOnlyBookFile() throws Exception {
        addDescription("Test the calculateStatisticsOnBookDir method on a directory with only the content file.");
        File baseDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString());
        FileSuffixValidator validator = mock(FileSuffixValidator.class);
        PubhubStatistics statistics = new PubhubStatistics(baseDir, validator);
        
        String id = UUID.randomUUID().toString();
        File bookDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/" + id);
        
        File bookFile = new File(bookDir, id + ".pdf");
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        when(validator.hasValidSuffix(eq(bookFile))).thenReturn(true);
        
        Assert.assertEquals(statistics.count, 0);
        
        statistics.calculateStatisticsOnBookDir(bookDir);
        
        Assert.assertEquals(statistics.getTotalCount(), 1);
        Assert.assertEquals(statistics.getBothDataCount(), 0);
        Assert.assertEquals(statistics.getNeitherDataCount(), 0);
        Assert.assertEquals(statistics.getOnlyBookFileCount(), 1);
        Assert.assertEquals(statistics.getOnlyMetadataCount(), 0);
        
        verify(validator).hasValidSuffix(eq(bookFile));
        verifyNoMoreInteractions(validator);
    }
    
    @Test
    public void testCalculateStatisticsOnBookDirOnlyMetadataFile() throws Exception {
        addDescription("Test the calculateStatisticsOnBookDir method on a directory with it has metadata and content file, but the content file has a bad suffix.");
        File baseDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + UUID.randomUUID().toString());
        FileSuffixValidator validator = mock(FileSuffixValidator.class);
        PubhubStatistics statistics = new PubhubStatistics(baseDir, validator);
        
        String id = UUID.randomUUID().toString();
        File bookDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/" + id);

        File bookFile = new File(bookDir, id + ".pdf");
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        File metadataFile = new File(bookDir, id + ".xml");
        TestFileUtils.createFile(metadataFile, UUID.randomUUID().toString());
        
        when(validator.hasValidSuffix(eq(bookFile))).thenReturn(false);
        
        Assert.assertEquals(statistics.count, 0);
        
        statistics.calculateStatisticsOnBookDir(bookDir);
        
        Assert.assertEquals(statistics.getTotalCount(), 1);
        Assert.assertEquals(statistics.getBothDataCount(), 0);
        Assert.assertEquals(statistics.getNeitherDataCount(), 0);
        Assert.assertEquals(statistics.getOnlyBookFileCount(), 0);
        Assert.assertEquals(statistics.getOnlyMetadataCount(), 1);

        verify(validator).hasValidSuffix(eq(bookFile));
        verifyNoMoreInteractions(validator);

    }
    
}
