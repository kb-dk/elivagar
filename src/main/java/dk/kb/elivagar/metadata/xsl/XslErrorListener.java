package dk.kb.elivagar.metadata.xsl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an XSL error listener which can be used while transforming XML files.
 * 
 * Copied from Yggdrasil.
 */
public class XslErrorListener implements ErrorListener {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(XslErrorListener.class);

    /** Errors accumulated. */
    protected int numberOfErrors;

    /** Errors messages. */
    protected List<String> errors = new LinkedList<String>();

    /** Fatal errors accumulated. */
    protected int numberOfFatalErrors;

    /** Fatal error messages. */
    protected List<String> fatalErrors = new LinkedList<String>();

    /** Warnings accumulated. */
    protected int numberOfWarnings;

    /** Warning messages. */
    protected List<String> warnings = new LinkedList<String>();

    /**
     * Constructor.
     */
    public XslErrorListener() {
        reset();
    }
    
    /**
     * Reset accumulated errors counters.
     */
    public void reset() {
        numberOfErrors = 0;
        numberOfFatalErrors = 0;
        numberOfWarnings = 0;
        errors.clear();
        fatalErrors.clear();
        warnings.clear();
    }

    /**
     * Returns a boolean indicating whether this listener has recorded any errors.
     * @return a boolean indicating whether this listener has recorded any errors
     */
    public boolean hasErrors() {
        return numberOfErrors != 0 || numberOfFatalErrors != 0 || numberOfWarnings != 0;
    }

    @Override
    public void error(TransformerException exception) throws TransformerException {
        ++numberOfErrors;
        errors.add(exception.getMessageAndLocation());
        log.error("XLST processing error!", exception.getMessageAndLocation(), exception);
    }

    @Override
    public void fatalError(TransformerException exception) throws TransformerException {
        ++numberOfFatalErrors;
        fatalErrors.add(exception.getMessageAndLocation());
        log.error("Fatal XLST processing error!", exception.getMessageAndLocation(), exception);
    }

    @Override
    public void warning(TransformerException exception) throws TransformerException {
        ++numberOfWarnings;
        warnings.add(exception.getMessageAndLocation());
        log.warn("XLST processing warning!", exception.getMessageAndLocation(), exception);
    }

    /**
     * @return The list of fatal errors.
     */
    public List<String> getFatalErrors() {
        return new ArrayList<String>(fatalErrors);
    }
    
    /**
     * @return The list of errors.
     */
    public List<String> getErrors() {
        return new ArrayList<String>(errors);
    }
    
    /**
     * @return The list of warnings.
     */
    public List<String> getWarnings() {
        return new ArrayList<String>(warnings);
    }
}
