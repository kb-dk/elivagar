package dk.kb.elivagar.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ChecksumUtilsTest extends ExtendedTestCase {

    @Test
    public void testInstantiation() {
        addDescription("Test the instantiation of the class.");
        ChecksumUtils o = new ChecksumUtils();
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof ChecksumUtils);
    }
    
    @Test
    public void testGenerateMD5Checksum() throws Exception {
        addDescription("Tests the digest of MD5 checksum method");
        
        addStep("Test with no text", "Should give empty checksum");
        InputStream data1 = new ByteArrayInputStream(new byte[0]);
        Assert.assertEquals(ChecksumUtils.generateMD5Checksum(data1), 
                "d41d8cd98f00b204e9800998ecf8427e");
        
        addStep("Test with text ", "Should give different checksums");
        String message = "The quick brown fox jumps over the lazy dog";
        InputStream data2 = new ByteArrayInputStream(message.getBytes());
        Assert.assertEquals(ChecksumUtils.generateMD5Checksum(data2),
                "9e107d9d372bb6826bd81d3542a419d6");
    }
}
