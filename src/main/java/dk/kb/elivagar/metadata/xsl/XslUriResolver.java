package dk.kb.elivagar.metadata.xsl;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements an XSL URI resolver which can be used to resolve external XSL files.
 * 
 * Copied from Yggdrasil.
 */
public class XslUriResolver implements URIResolver {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(XslUriResolver.class);

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        log.info("URIResolver: href=" + href + " - base=" + base);
        throw new NotImplementedException("XslUriResolver.resolve(String href, String base)");
    }
}
