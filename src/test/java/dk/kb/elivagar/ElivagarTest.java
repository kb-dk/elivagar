package dk.kb.elivagar;

import java.io.File;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.testutils.TestFileUtils;
import dk.kb.elivagar.utils.FileUtils;

public class ElivagarTest extends ExtendedTestCase {

    String licenseFilePath = System.getenv("HOME") + "/pubhub-license.txt";
    String license;
    
    String userHome = System.getProperty("user.home");
    String bookIdsPath = userHome + "/Tmp/BookIDs/";
    String booksPath = userHome + "/Tmp/Books/";
    
    File bookIdsDir;
    File booksDir;

    boolean marshal_bookIDs_to_individual_files = true;
    boolean marshal_books_to_individual_files = true;
    
    @BeforeClass
    public void setup() throws Exception {
        File passwordFile = new File(licenseFilePath);
        if(!passwordFile.isFile()) {
            throw new SkipException("No license file is found at '" + licenseFilePath + ".");
        }
        license = TestFileUtils.readFile(passwordFile);
        
        bookIdsDir = FileUtils.createDirectory(bookIdsPath);
        booksDir = FileUtils.createDirectory(booksPath);
    }
    
    @Test
    public void testElivagar() throws Exception {
        // Debugging
        System.out.print("Test begin");

        Elivagar elivagar = new Elivagar(license);
//        elivagar.elivagar();
        
        // Marshal BookIDs to individual file
        if (marshal_bookIDs_to_individual_files) {
            elivagar.marshalBookIDs(bookIdsDir);
            // Debugging
            System.out.println("Marshaled all BookIDs to individual files");
        }
       
        // Marshal Books to individual file
        if (marshal_books_to_individual_files) {
            elivagar.marshalBooks(booksDir);
            // Debugging
            System.out.println("Marshaled all Books to individual files");
        }

        // Debugging
        System.out.print("Test end");
    }
}
