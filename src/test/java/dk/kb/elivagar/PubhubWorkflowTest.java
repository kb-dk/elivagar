package dk.kb.elivagar;

import java.io.File;
import java.nio.file.Files;
import java.util.Date;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.pubhub.PubhubPacker;
import dk.kb.elivagar.testutils.TestConfigurations;
import dk.kb.elivagar.testutils.TestFileUtils;

public class PubhubWorkflowTest extends ExtendedTestCase {

    public static final String PDF_SUFFIX = ".pdf";
    public static final String EPUB_SUFFIX = ".epub";
    
    Long MILLIS_PER_YEAR = 31556908800L; // from wiki

    PubhubWorkflow elivagarWorkflow;
    Configuration conf;

    @BeforeClass
    public void setup() throws Exception {
        conf = TestConfigurations.getConfigurationForTest();
        elivagarWorkflow = new PubhubWorkflow(conf);
    }

    @BeforeMethod
    public void setupMethod() throws Exception {
        TestFileUtils.setup();        
    }

    @Test(enabled = false)
    public void testElivagarRetrievingBooks() throws Exception {
        int count = 10;
        elivagarWorkflow.retrieveAllBooks(count);
        System.out.println("Marshaled all Books to individual files");

        Assert.assertEquals(conf.getEbookFileDir().list().length, count);
    }

    @Test(enabled = true)
//    @Test(enabled = false)
    public void testElivagarRetrievingModifiedBooks() throws Exception {
        int count = 10;
        Date oneYearAgo = new Date(System.currentTimeMillis()-MILLIS_PER_YEAR);
        elivagarWorkflow.retrieveModifiedBooks(oneYearAgo, count);

        Assert.assertEquals(calculateNumberOfEbooksAndAudioBooks(), count);
    }

    @Test
    public void testPackingBooksWithPdfSuffix() throws Exception {
        TestFileUtils.createEmptyDirectory(conf.getEbookFileDir().getAbsolutePath());
        TestFileUtils.createEmptyDirectory(conf.getEbookOutputDir().getAbsolutePath());
        
        int numberOfBooks = 10;
        for(int i = 0; i < numberOfBooks; i++) {
            File testFile = new File(conf.getEbookFileDir(), UUID.randomUUID().toString() + PDF_SUFFIX);
            TestFileUtils.createFile(testFile, UUID.randomUUID().toString());
        }

        elivagarWorkflow.packFilesForBooks();

        Assert.assertTrue(conf.getEbookFileDir().isDirectory());
        Assert.assertTrue(conf.getEbookOutputDir().isDirectory());
        for(File bookDir : conf.getEbookOutputDir().listFiles()) {
            for(File bookFile : bookDir.listFiles()) {
                Assert.assertTrue(Files.isSymbolicLink(bookFile.toPath()));
            }
        }

        Assert.assertEquals(calculateNumberOfEbooksAndAudioBooks(), numberOfBooks);
    }
    
    @Test
    public void testPackingBooksWithEpubSuffix() throws Exception {
        TestFileUtils.createEmptyDirectory(conf.getEbookFileDir().getAbsolutePath());
        TestFileUtils.createEmptyDirectory(conf.getEbookOutputDir().getAbsolutePath());

        int numberOfBooks = 10;
        for(int i = 0; i < numberOfBooks; i++) {
            File testFile = new File(conf.getEbookFileDir(), UUID.randomUUID().toString() + EPUB_SUFFIX);
            TestFileUtils.createFile(testFile, UUID.randomUUID().toString());
        }

        elivagarWorkflow.packFilesForBooks();

        Assert.assertTrue(conf.getEbookFileDir().isDirectory());
        Assert.assertTrue(conf.getEbookOutputDir().isDirectory());
        for(File bookDir : conf.getEbookOutputDir().listFiles()) {
            for(File bookFile : bookDir.listFiles()) {
                Assert.assertTrue(Files.isSymbolicLink(bookFile.toPath()));
            }
        }

        Assert.assertEquals(calculateNumberOfEbooksAndAudioBooks(), numberOfBooks);
    }

    @Test
    public void testPackingBooksWithIncorrectSuffix() throws Exception {
        addDescription("Test that files with non ebook suffix will not be packaged");
        TestFileUtils.createEmptyDirectory(conf.getEbookFileDir().getAbsolutePath());
        TestFileUtils.createEmptyDirectory(conf.getEbookOutputDir().getAbsolutePath());

        int numberOfBooks = 10;
        for(int i = 0; i < numberOfBooks; i++) {
            File testFile = new File(conf.getEbookFileDir(), UUID.randomUUID().toString() + Constants.PUBHUB_METADATA_SUFFIX);
            TestFileUtils.createFile(testFile, UUID.randomUUID().toString());
        }

        elivagarWorkflow.packFilesForBooks();
        
        Assert.assertEquals(calculateNumberOfEbooksAndAudioBooks(), 0);
    }
    
    protected int calculateNumberOfEbooksAndAudioBooks() {
        // Annoying way of calculating the number of books, but necessary, since the 'list()' might return null, if empty.
        int res = 0;
        if(conf.getEbookOutputDir().list() != null) {
            res += conf.getEbookOutputDir().list().length;
        }
        if(conf.getAudioOutputDir().list() != null) {
            res += conf.getAudioOutputDir().list().length;
        }
        return res;
    }
}
