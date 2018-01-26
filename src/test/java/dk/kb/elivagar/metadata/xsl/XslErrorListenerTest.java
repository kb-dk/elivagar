package dk.kb.elivagar.metadata.xsl;

import java.util.UUID;

import javax.xml.transform.TransformerException;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class XslErrorListenerTest extends ExtendedTestCase {

    @Test
    public void testInstantiation() {
        XslErrorListener listener = new XslErrorListener();
        Assert.assertEquals(listener.numberOfErrors, 0);
        Assert.assertEquals(listener.numberOfFatalErrors, 0);
        Assert.assertEquals(listener.numberOfWarnings, 0);
        Assert.assertTrue(listener.errors.isEmpty());
        Assert.assertTrue(listener.fatalErrors.isEmpty());
        Assert.assertTrue(listener.warnings.isEmpty());
        Assert.assertTrue(listener.getErrors().isEmpty());
        Assert.assertTrue(listener.getFatalErrors().isEmpty());
        Assert.assertTrue(listener.getWarnings().isEmpty());
        Assert.assertFalse(listener.hasErrors());
    }
    
    @Test
    public void testFatalError() throws TransformerException {
        XslErrorListener listener = new XslErrorListener();
        TransformerException exception = new TransformerException("TEST EXCEPTION: " + UUID.randomUUID().toString());
        listener.fatalError(exception);
        Assert.assertTrue(listener.hasErrors());
        Assert.assertEquals(listener.numberOfFatalErrors, 1);
        Assert.assertEquals(listener.numberOfErrors, 0);
        Assert.assertEquals(listener.numberOfWarnings, 0);
        Assert.assertFalse(listener.fatalErrors.isEmpty());
        Assert.assertTrue(listener.errors.isEmpty());
        Assert.assertTrue(listener.warnings.isEmpty());
        Assert.assertFalse(listener.getFatalErrors().isEmpty());
        Assert.assertTrue(listener.getErrors().isEmpty());
        Assert.assertTrue(listener.getWarnings().isEmpty());
        Assert.assertTrue(listener.getFatalErrors().contains(exception.getMessageAndLocation()));
    }
    
    @Test
    public void testError() throws TransformerException {
        XslErrorListener listener = new XslErrorListener();
        TransformerException exception = new TransformerException("TEST EXCEPTION: " + UUID.randomUUID().toString());
        listener.error(exception);
        Assert.assertTrue(listener.hasErrors());
        Assert.assertEquals(listener.numberOfFatalErrors, 0);
        Assert.assertEquals(listener.numberOfErrors, 1);
        Assert.assertEquals(listener.numberOfWarnings, 0);
        Assert.assertTrue(listener.fatalErrors.isEmpty());
        Assert.assertFalse(listener.errors.isEmpty());
        Assert.assertTrue(listener.warnings.isEmpty());
        Assert.assertTrue(listener.getFatalErrors().isEmpty());
        Assert.assertFalse(listener.getErrors().isEmpty());
        Assert.assertTrue(listener.getWarnings().isEmpty());
        Assert.assertTrue(listener.getErrors().contains(exception.getMessageAndLocation()));
    }
    
    @Test
    public void testWarning() throws TransformerException {
        XslErrorListener listener = new XslErrorListener();
        TransformerException exception = new TransformerException("TEST EXCEPTION: " + UUID.randomUUID().toString());
        listener.warning(exception);
        Assert.assertTrue(listener.hasErrors());
        Assert.assertEquals(listener.numberOfFatalErrors, 0);
        Assert.assertEquals(listener.numberOfErrors, 0);
        Assert.assertEquals(listener.numberOfWarnings, 1);
        Assert.assertTrue(listener.fatalErrors.isEmpty());
        Assert.assertTrue(listener.errors.isEmpty());
        Assert.assertFalse(listener.warnings.isEmpty());
        Assert.assertTrue(listener.getFatalErrors().isEmpty());
        Assert.assertTrue(listener.getErrors().isEmpty());
        Assert.assertFalse(listener.getWarnings().isEmpty());
        Assert.assertTrue(listener.getWarnings().contains(exception.getMessageAndLocation()));
    }
    
}
