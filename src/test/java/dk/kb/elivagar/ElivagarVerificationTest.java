package dk.kb.elivagar;

import java.io.File;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import dk.kb.elivagar.testutils.TestFileUtils;

public class ElivagarVerificationTest extends ExtendedTestCase {

    @BeforeMethod
    public void setupMethod() {
        TestFileUtils.setup();
    }
    
    @AfterMethod
    public void tearDownMethod() {
        TestFileUtils.tearDown();
    }
    
    @Test
    public void testInstantiation() {
        addDescription("Test instantiation of the ElivagarVerification");
        Object o = new ElivagarVerification();
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof ElivagarVerification);
    }
    
    @Test
    public void testValidateExecutableFileSuccess() throws Exception {
        addDescription("Test the validateExecutableFile method for the success scenario when the file exists and can read and execute");
        File f = TestFileUtils.createTempFile(UUID.randomUUID().toString());
        f.setExecutable(true);
        f.setReadable(true);
        
        Boolean b = ElivagarVerification.validateExecutableFile(f.getAbsolutePath(), "configurationName");
        
        Assert.assertFalse(b);
    }
    
    @Test
    public void testValidateExecutableFileMissing() throws Exception {
        addDescription("Test the validateExecutableFile method for the failure scenario when the file does not exist");
        File f = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        Assert.assertFalse(f.exists());
        
        Boolean b = ElivagarVerification.validateExecutableFile(f.getAbsolutePath(), "configurationName");
        
        Assert.assertTrue(b);
    }
    
    @Test
    public void testValidateExecutableFileCannotRead() throws Exception {
        addDescription("Test the validateExecutableFile method for the failure scenario when the file cannot be read");
        File f = TestFileUtils.createTempFile(UUID.randomUUID().toString());
        f.setExecutable(true);
        f.setReadable(false);
        
        Boolean b = ElivagarVerification.validateExecutableFile(f.getAbsolutePath(), "configurationName");
        
        Assert.assertTrue(b);
    }
    
    @Test
    public void testValidateExecutableFileCannotExecute() throws Exception {
        addDescription("Test the validateExecutableFile method for the failure scenario when the file cannot be executed");
        File f = TestFileUtils.createTempFile(UUID.randomUUID().toString());
        f.setExecutable(false);
        f.setReadable(true);
        
        Boolean b = ElivagarVerification.validateExecutableFile(f.getAbsolutePath(), "configurationName");
        
        Assert.assertTrue(b);
    }
}
