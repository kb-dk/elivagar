package dk.kb.elivagar.metadata.xsl;

import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

public class XslUriResolverTest extends ExtendedTestCase {

    @Test(expectedExceptions = NotImplementedException.class)
    public void testXslUriResolver() throws Exception {
        XslUriResolver resolver = new XslUriResolver();
        resolver.resolve(UUID.randomUUID().toString(), UUID.randomUUID().toString());
    }
}
