package dk.kb.elivagar;

import java.io.File;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import dk.kb.elivagar.testutils.TestFileUtils;
import dk.kb.elivagar.utils.FileUtils;

public class PubhubWorkflowTest extends ExtendedTestCase {

    String licenseFilePath = System.getenv("HOME") + "/pubhub-license.txt";
    String license;

    //    String userHome = System.getProperty("user.home");
    String baseDirPath = "tempDir";
    String ebookBaseDirPath = baseDirPath + "/books";
    String audioBaseDirPath = baseDirPath + "/audio";
    String bookFilesDirPath = baseDirPath + "/bookFiles";

    File baseDir;
    File baseBookDir;
    File baseAudioDir;

    Long MILLIS_PER_YEAR = 31556908800L; // from wiki

    boolean marshal_bookIDs_to_individual_files = true;
    boolean marshal_books_to_individual_files = true;
    PubhubWorkflow elivagarWorkflow;

    @BeforeClass
    public void setup() throws Exception {
        File passwordFile = new File(licenseFilePath);
        if(!passwordFile.isFile()) {
            throw new SkipException("No license file is found at '" + licenseFilePath + ".");
        }
        license = TestFileUtils.readFile(passwordFile);

        baseDir = TestFileUtils.createEmptyDirectory(baseDirPath);
        baseBookDir = TestFileUtils.createEmptyDirectory(ebookBaseDirPath);
        baseAudioDir = TestFileUtils.createEmptyDirectory(audioBaseDirPath);

        Map<String, String> confMap = new HashMap<String, String>();
        confMap.put(Configuration.Constants.CONF_EBOOK_OUTPUT_DIR, baseBookDir.getAbsolutePath());
        confMap.put(Configuration.Constants.CONF_AUDIO_OUTPUT_DIR, baseAudioDir.getAbsolutePath());
        confMap.put(Configuration.Constants.CONF_FILE_DIR, new File(bookFilesDirPath).getAbsolutePath());
        confMap.put(Configuration.Constants.CONF_LICENSE_KEY, license);
        Configuration conf = new Configuration(confMap);
        elivagarWorkflow = new PubhubWorkflow(conf);
    }

    @BeforeMethod
    public void setupMethod() throws Exception {
        baseDir = TestFileUtils.createEmptyDirectory(baseDirPath);        
    }

    @Test(enabled = false)
    public void testElivagarRetrievingBooks() throws Exception {
        int count = 10;
        elivagarWorkflow.retrieveAllBooks(count);
        System.out.println("Marshaled all Books to individual files");

        Assert.assertEquals(baseDir.list().length, count);
    }

    @Test(enabled = false)
    public void testElivagarRetrievingBookIDs() throws Exception {
        //        int count = 10;
        //        elivagar.downloadAllBookIDs(bookIdsDir, count);
        //        System.out.println("Marshaled all BookIDs to individual files");
        //
        //        Assert.assertEquals(bookIdsDir.list().length, count);
    }

    @Test(enabled = false)
    public void testElivagarRetrievingModifiedBookIDs() throws Exception {
        //        elivagar.r
        //        int count = 10;
        //        Date oneYearAgo = new Date(System.currentTimeMillis()-MILLIS_PER_YEAR);
        //        elivagar.downloadBookIDsAfterModifyDate(modifiedBookIdsDir, oneYearAgo, count);
        //        System.out.println("Marshaled all BookIDs to individual files");

        //        Assert.assertEquals(bookIdsDir.list().length, count);
    }

    @Test(enabled = true)
    public void testElivagarRetrievingModifiedBooks() throws Exception {
        int count = 10;
        Date oneYearAgo = new Date(System.currentTimeMillis()-MILLIS_PER_YEAR);
        elivagarWorkflow.retrieveModifiedBooks(oneYearAgo, count);

        Assert.assertEquals(calculateNumberOfEbooksAndAudioBooks(), count);
    }

    @Test
    public void testPackingBooks() throws Exception {
        File bookFilesDir = FileUtils.createDirectory(bookFilesDirPath);
        Assert.assertEquals(baseDir.list().length, 1);

        int numberOfBooks = 10;
        for(int i = 0; i < numberOfBooks; i++) {
            File testFile = new File(bookFilesDir, UUID.randomUUID().toString());
            TestFileUtils.createFile(testFile, UUID.randomUUID().toString());
        }

        elivagarWorkflow.packFilesForBooks();

        for(File bookDir : baseBookDir.listFiles()) {
            if(!bookDir.equals(bookFilesDir)) {
                for(File bookFile : bookDir.listFiles()) {
                    Assert.assertTrue(Files.isSymbolicLink(bookFile.toPath()));
                }
            }
        }

        elivagarWorkflow.makeStatistics(System.out);

        Assert.assertEquals(calculateNumberOfEbooksAndAudioBooks(), numberOfBooks);
    }

    protected int calculateNumberOfEbooksAndAudioBooks() {
        // Annoying way of calculating the number of books, but necessary, since the 'list()' might return null, if empty.
        int res = 0;
        if(baseBookDir.list() != null) {
            res += baseBookDir.list().length;
        }
        if(baseAudioDir.list() != null) {
            res += baseAudioDir.list().length;
        }
        return res;
    }
}
