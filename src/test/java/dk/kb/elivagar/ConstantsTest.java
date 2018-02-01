package dk.kb.elivagar;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ConstantsTest extends ExtendedTestCase {

    @Test
    public void testConstructor() {
        Constants c = new Constants();
        Assert.assertNotNull(c);
    }
}
