package dk.kb.elivagar.metadata.xsl;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Error handler for XML.
 */
public class XmlErrorHandler implements ErrorHandler {

    /** Errors messages. */
    protected List<String> errors = new LinkedList<String>();

    /** Fatal errors messages. */
    protected List<String> fatalErrors = new LinkedList<String>();

    /** Warning messages. */
    protected List<String> warnings = new LinkedList<String>();

    /**
     * Reset accumulated errors counters.
     */
    public void reset() {
        errors.clear();
        fatalErrors.clear();
        warnings.clear();
    }

    /**
     * Returns a boolean indicating whether this handler has recorded any errors.
     * @return a boolean indicating whether this handler has recorded any errors
     */
    public boolean hasErrors() {
        return errors.size() != 0 || fatalErrors.size() != 0 || warnings.size() != 0;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        warnings.add(exception.getMessage());

    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        errors.add(exception.getMessage());
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        fatalErrors.add(exception.getMessage());
    }
}
