package dk.kb.elivagar.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import dk.kb.elivagar.exception.ArgumentCheck;

public class StreamUtilsTest extends ExtendedTestCase {

    @Test
    public void testExtractLines() throws Exception {
        
        String line1 = UUID.randomUUID().toString();
        String line2 = UUID.randomUUID().toString();
        String tekst = line1 + "\n" + line2;
        
        ByteArrayInputStream is = new ByteArrayInputStream(tekst.getBytes());
        List<String> lines = StreamUtils.extractInputStreamAsLines(is);
        Assert.assertEquals(lines.size(), 2);
        Assert.assertEquals(lines.get(0), line1);
        Assert.assertEquals(lines.get(1), line2);
    }
    
    // THIS DOES NOT CREATE AN IOEXCEPTION!!!
    @Test(expectedExceptions = IOException.class, enabled = false)
    public void testExtractLinesFailure() throws Exception {
        StreamUtils.extractInputStreamAsLines(new ByteArrayInputStream(new byte[0]));
    }
    
    @Test
    public void testExtractString() throws Exception {
        String line1 = UUID.randomUUID().toString();
        String line2 = UUID.randomUUID().toString();
        String tekst = line1 + "\n" + line2;
        
        ByteArrayInputStream is = new ByteArrayInputStream(tekst.getBytes());
        String lines = StreamUtils.extractInputStreamAsString(is);
        Assert.assertEquals(lines, tekst + "\n");
    }

    @Test
    public void testCopyStream() throws Exception {
        String line1 = UUID.randomUUID().toString();
        String line2 = UUID.randomUUID().toString();
        String tekst = line1 + "\n" + line2;
        
        ByteArrayInputStream is = new ByteArrayInputStream(tekst.getBytes());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamUtils.copyInputStreamToOutputStream(is, os);
        Assert.assertEquals(os.size(), tekst.length());
        Assert.assertEquals(os.toString(), tekst);
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testCopyStreamFailureInputStreamNull() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        StreamUtils.copyInputStreamToOutputStream(null, os);
    }
    
    @Test(expectedExceptions = ArgumentCheck.class)
    public void testCopyStreamFailureOutputStreamNull() throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(new byte[0]);
        StreamUtils.copyInputStreamToOutputStream(is, null);
    }
    
    @Test
    public void testConstructor() {
        new StreamUtils();
    }
}
