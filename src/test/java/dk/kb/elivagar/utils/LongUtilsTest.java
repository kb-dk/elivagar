package dk.kb.elivagar.utils;

import java.util.Random;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LongUtilsTest extends ExtendedTestCase {

    @Test
    public void testInstantiation() {
        LongUtils l = new LongUtils();
        Assert.assertNotNull(l);
    }
    
    @Test
    public void testGetLongWithLong() throws Exception {
        addDescription("Test the getLong method with a Long object");
        
        Long o = new Random().nextLong();
        Assert.assertEquals(o.longValue(), LongUtils.getLong(o).longValue());
    }
    
    @Test
    public void testGetLongWithInt() throws Exception {
        addDescription("Test the getLong method with a Integer object");
        
        Integer o = new Random().nextInt();
        Assert.assertEquals(o.longValue(), LongUtils.getLong(o).longValue());
    }

    @Test
    public void testGetLongWithString() throws Exception {
        addDescription("Test the getLong method with a String object");
        
        Long l = new Random().nextLong();
        String o = "" + l;
        Assert.assertEquals(l.longValue(), LongUtils.getLong(o).longValue());
    }

}
