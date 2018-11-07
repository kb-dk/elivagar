package dk.kb.elivagar.utils;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import dk.kb.elivagar.exception.ArgumentCheck;

public class Base16UtilsTest extends ExtendedTestCase {
    private final String DECODED_CHECKSUM = "ff5aca7ae8c80c9a3aeaf9173e4dfd27";
    private final byte[] ENCODED_CHECKSUM = new byte[]{-1,90,-54,122,-24,-56,12,-102,58,-22,-7,23,62,77,-3,39};

    @Test
    public void testInstantiation() {
        addDescription("Test the instantiation of the class.");
        Base16Utils o = new Base16Utils();
        Assert.assertNotNull(o);
        Assert.assertTrue(o instanceof Base16Utils);
    }

    @Test
    public void encodeChecksum() throws Exception {
        addDescription("Validating the encoding of the checksums.");
        addStep("Encode the checksum and validate", "It should match the precalculated constant.");
        byte[] encodedChecksum = Base16Utils.encodeBase16(DECODED_CHECKSUM);

        Assert.assertEquals(encodedChecksum.length, ENCODED_CHECKSUM.length, 
                "The size of the encoded checksum differs from the expected.");

        for(int i = 0; i < encodedChecksum.length; i++){
            Assert.assertEquals(encodedChecksum[i], ENCODED_CHECKSUM[i]);
        }
    }

    @Test
    public void decodeChecksum() throws Exception {
        addDescription("Validating the decoding of the checksums.");
        addStep("Decode the checksum and validate.", "It should match the precalculated constant.");
        String decodedChecksum = Base16Utils.decodeBase16(ENCODED_CHECKSUM);
        Assert.assertEquals(decodedChecksum, DECODED_CHECKSUM);
    }

    @Test
    public void badArgumentTest() throws Exception {
        addDescription("Test bad arguments");
        Assert.assertNull(Base16Utils.decodeBase16(null));

        try {
            Base16Utils.encodeBase16(null);
            Assert.fail("Should throw an exception");
        } catch (ArgumentCheck e) {
            // expected
        }

        addStep("Test with a odd number of characters.", "Should throw an exception");
        try {
            Base16Utils.encodeBase16("123");
            Assert.fail("Should throw an exception");
        } catch (ArgumentCheck e) {
            // expected
        }
    }
}
