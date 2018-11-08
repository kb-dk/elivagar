package dk.kb.elivagar;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import dk.kb.elivagar.testutils.PreventSystemExit;
import dk.kb.elivagar.testutils.TestFileUtils;
import dk.kb.elivagar.utils.FileUtils;

public class ElivagarTest extends ExtendedTestCase {

    File testDir;
    File testConfFile = new File("src/test/resources/elivagar.yml");
    
    @BeforeMethod
    public void setup() throws IOException {
        testDir = TestFileUtils.createEmptyDirectory("tempDir");
        Assert.assertTrue(testConfFile.isFile());
        
        FileUtils.createDirectory("tempDir/transfer/ebook/ingest");
        FileUtils.createDirectory("tempDir/transfer/ebook/content");
        FileUtils.createDirectory("tempDir/transfer/ebook/metadata");
        FileUtils.createDirectory("tempDir/transfer/audio/ingest");
        FileUtils.createDirectory("tempDir/transfer/audio/content");
        FileUtils.createDirectory("tempDir/transfer/audio/metadata");
    }
    
    @Test
    public void testInstantiation() {
        Elivagar e = new Elivagar();
        Assert.assertNotNull(e);
    }

    @Test(expectedExceptions = PreventSystemExit.ExitTrappedException.class)
    public void testFailureWhenNoArgs() {
        PreventSystemExit.forbidSystemExitCall() ;
        try {
            Elivagar.main(new String[0]);
        } finally {
            PreventSystemExit.enableSystemExitCall() ;
        }
    }
    
    @Test(expectedExceptions = PreventSystemExit.ExitTrappedException.class)
    public void testFailureWhenBadConfPath() {
        String path = UUID.randomUUID().toString();
        PreventSystemExit.forbidSystemExitCall() ;
        try {
            Elivagar.main(path);
        } finally {
            PreventSystemExit.enableSystemExitCall() ;
        }
    }
    
    @Test(expectedExceptions = PreventSystemExit.ExitTrappedException.class)
    public void testFailureWithBadConfiguration() {
        PreventSystemExit.forbidSystemExitCall() ;
        try {
            Elivagar.main(testConfFile.getAbsolutePath());
        } finally {
            PreventSystemExit.enableSystemExitCall() ;
        }
    }

    @Test(expectedExceptions = PreventSystemExit.ExitTrappedException.class)
    public void testFailureWithBadConfigurationAndModifyDate() {
        PreventSystemExit.forbidSystemExitCall() ;
        try {
            Elivagar.main(testConfFile.getAbsolutePath(), "1");
        } finally {
            PreventSystemExit.enableSystemExitCall() ;
        }
    }

    @Test
    public void testSuccessWithNoDownloads() {
        PreventSystemExit.forbidSystemExitCall() ;
        try {
            Elivagar.main(testConfFile.getAbsolutePath(), "0");
        } finally {
            PreventSystemExit.enableSystemExitCall() ;
        }
    }

    @Test
    public void testSuccessWithNoDownloadsNoMax() {
        PreventSystemExit.forbidSystemExitCall() ;
        try {
            Elivagar.main(testConfFile.getAbsolutePath(), "0", "-1");
        } finally {
            PreventSystemExit.enableSystemExitCall() ;
        }
    }
    
    @Test
    public void testSuccessWithNoDownloadsAndAMaximum() {
        PreventSystemExit.forbidSystemExitCall() ;
        try {
            Elivagar.main(testConfFile.getAbsolutePath(), "0", "1");
        } finally {
            PreventSystemExit.enableSystemExitCall() ;
        }
    }

}
