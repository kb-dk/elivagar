package dk.kb.elivagar;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.testutils.TestFileUtils;
import dk.kb.elivagar.utils.FileUtils;

public class ElivagarTest extends ExtendedTestCase {

    String licenseFilePath = System.getenv("HOME") + "/pubhub-license.txt";
    String license;
    
//    String userHome = System.getProperty("user.home");
    String baseDirPath = "tempDir";
    
    File baseDir;

    Long MILLIS_PER_YEAR = 31556908800L; // from wiki
    
    boolean marshal_bookIDs_to_individual_files = true;
    boolean marshal_books_to_individual_files = true;
    Elivagar elivagar;
    
    @BeforeClass
    public void setup() throws Exception {
        File passwordFile = new File(licenseFilePath);
        if(!passwordFile.isFile()) {
            throw new SkipException("No license file is found at '" + licenseFilePath + ".");
        }
        license = TestFileUtils.readFile(passwordFile);
        
        baseDir = TestFileUtils.createEmptyDirectory(baseDirPath);
        
        Map<String, String> confMap = new HashMap<String, String>();
        confMap.put(Configuration.Constants.CONF_FILE_DIR, baseDir.getAbsolutePath());
        confMap.put(Configuration.Constants.CONF_OUTPUT_DIR, baseDir.getAbsolutePath());
        confMap.put(Configuration.Constants.CONF_LICENSE_KEY, license);
        Configuration conf = new Configuration(confMap);
        elivagar = new Elivagar(conf);
    }
    
    @Test(enabled = false)
    public void testElivagarRetrievingBooks() throws Exception {
        int count = 10;
        elivagar.retrieveAllBooks(count);
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
        elivagar.retrieveModifiedBooks(oneYearAgo, count);
        System.out.println("Marshaled all BookIDs to individual files");

        Assert.assertEquals(baseDir.list().length, count);
    }
}
