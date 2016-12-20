package dk.kb.elivagar;

import java.io.File;
import java.io.IOException;
import java.security.Permission;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import dk.kb.elivagar.testutils.TestFileUtils;

public class ElivagarTest extends ExtendedTestCase {

    File testDir;
    File testConfFile = new File("src/test/resources/elivagar.yml");
    
    @BeforeMethod
    public void setup() throws IOException {
        testDir = TestFileUtils.createEmptyDirectory("tempDir");
        Assert.assertTrue(testConfFile.isFile());
    }
    
    @Test
    public void testInstantiation() {
        Elivagar e = new Elivagar();
        Assert.assertNotNull(e);
    }

    @Test(expectedExceptions = ExitTrappedException.class)
    public void testFailureWhenNoArgs() {
        forbidSystemExitCall() ;
        try {
            Elivagar.main(new String[0]);
        } finally {
            enableSystemExitCall() ;
        }
    }
    
    @Test(expectedExceptions = ExitTrappedException.class)
    public void testFailureWhenBadConfPath() {
        String path = UUID.randomUUID().toString();
        forbidSystemExitCall() ;
        try {
            Elivagar.main(path);
        } finally {
            enableSystemExitCall() ;
        }
    }
    
    @Test(expectedExceptions = ExitTrappedException.class)
    public void testFailureWithBadConfiguration() {
        forbidSystemExitCall() ;
        try {
            Elivagar.main(testConfFile.getAbsolutePath());
        } finally {
            enableSystemExitCall() ;
        }
    }

    @Test(expectedExceptions = ExitTrappedException.class)
    public void testFailureWithBadConfigurationAndModifyDate() {
        forbidSystemExitCall() ;
        try {
            Elivagar.main(testConfFile.getAbsolutePath(), "1");
        } finally {
            enableSystemExitCall() ;
        }
    }

    @Test
    public void testSuccessWithNoDownloads() {
        forbidSystemExitCall() ;
        try {
            Elivagar.main(testConfFile.getAbsolutePath(), "0");
        } finally {
            enableSystemExitCall() ;
        }
    }

    @Test
    public void testSuccessWithNoDownloadsNoMax() {
        forbidSystemExitCall() ;
        try {
            Elivagar.main(testConfFile.getAbsolutePath(), "0", "-1");
        } finally {
            enableSystemExitCall() ;
        }
    }
    
    @Test
    public void testSuccessWithNoDownloadsAndAMaximum() {
        forbidSystemExitCall() ;
        try {
            Elivagar.main(testConfFile.getAbsolutePath(), "0", "1");
        } finally {
            enableSystemExitCall() ;
        }
    }
    
    private static class ExitTrappedException extends SecurityException { }
    private static void forbidSystemExitCall() {
        final SecurityManager securityManager = new SecurityManager() {
            public void checkPermission( Permission permission ) {
                if( permission.getName().startsWith("exitVM") ) {
                    throw new ExitTrappedException() ;
                }
            }
        } ;
        System.setSecurityManager( securityManager ) ;
        System.out.println("Exit disabled");
    }

    private static void enableSystemExitCall() {
        System.setSecurityManager( null ) ;
    }
}
