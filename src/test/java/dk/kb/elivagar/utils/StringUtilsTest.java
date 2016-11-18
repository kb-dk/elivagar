package dk.kb.elivagar.utils;

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
}
