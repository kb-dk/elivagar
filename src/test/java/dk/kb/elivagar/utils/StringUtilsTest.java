package dk.kb.elivagar.utils;

import java.io.File;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StringUtilsTest extends ExtendedTestCase {

    @Test
    public void testSuffixForFilename() throws Exception {
        String prefix = UUID.randomUUID().toString();
        String suffix = UUID.randomUUID().toString();
        
        String path = prefix + "." + suffix;
        
        Assert.assertEquals(StringUtils.getSuffix(path), suffix);
    }
    
    @Test
    public void testSuffixForFileWithNoSuffix() throws Exception {
        File f = new File(UUID.randomUUID().toString());
        
        Assert.assertEquals(StringUtils.getSuffix(f.getPath()), "");
    }
    
    @Test
    public void testPrefixForFilename() throws Exception {
        String prefix = UUID.randomUUID().toString();
        String suffix = UUID.randomUUID().toString();
        
        String path = prefix + "." + suffix;
        
        Assert.assertEquals(StringUtils.getPrefix(path), prefix);
    }

    @Test
    public void testPrefixForFilenameWithNoPrefix() throws Exception {
        String name = UUID.randomUUID().toString();
        
        Assert.assertEquals(StringUtils.getPrefix(name), name);
    }
    
    @Test
    public void testConstructor() {
        new StringUtils();
    }

}
