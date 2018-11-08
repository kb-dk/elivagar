package dk.kb.elivagar.metadata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import dk.kb.elivagar.exception.ArgumentCheck;
import dk.kb.elivagar.metadata.xsl.XslErrorListener;
import dk.kb.elivagar.metadata.xsl.XslTransformer;
import dk.kb.elivagar.metadata.xsl.XslUriResolver;

/**
 * Metadata transformer.
 */
public class MetadataTransformer {
    
    /**
     * Different types of transformations currently supported by Elivagar.
     */
    public enum TransformationType {
        /** The transformation from the OAI-MARC format, which is delivered from Aleph, into MARC 21.*/
        ALEPH_TO_MARC21("oaimarc2slimmarc.xsl"),
        /** The transformation from MARC 21 into MODS.*/
        MARC21_TO_MODS("marcToMODS.xsl"),
        /** The MODS cleanup transformation. Removes empty fields.*/
        MODS_CLEANUP("eliminate_empty_eles.xsl");
        
        /** The name of the associated transformation file.*/
        protected final String scriptName;
        /**
         * Constructor.
         * @param scriptName The name of the transformation file for this transformation.
         */
        TransformationType(String scriptName) {
            this.scriptName = scriptName;
        }
    }
    
    /** The mapping between transformation types and their transformers.*/
    Map<TransformationType, XslTransformer> transformers = new HashMap<TransformationType, XslTransformer>();
    
    /** The directory containing the transformation scripts.*/
    protected final File xsltDir;
    
    /**
     * Constructor.
     * @param xsltDir The transformation directory.
     */
    public MetadataTransformer(File xsltDir) {
        ArgumentCheck.checkExistsDirectory(xsltDir, "File xsltDir");
        this.xsltDir = xsltDir;
    }
    
    /**
     * Transform a given piece of metadata according to the given type of transformation.
     * @param input The input stream with the metadata to transform.
     * @param output The output stream where the metadata transformation result is delivered.
     * @param type The type of transformation to perform.
     */
    public void transformMetadata(InputStream input, OutputStream output, TransformationType type) {
        ArgumentCheck.checkNotNull(input, "InputStream input");
        ArgumentCheck.checkNotNull(output, "OutputStream output");
        ArgumentCheck.checkNotNull(type, "TransformationType type");
        try {
            XslTransformer transformer = getTransformationFile(type);
            
            URIResolver uriResolver = new XslUriResolver();
            XslErrorListener errorListener = new XslErrorListener();
            
            Source source = new StreamSource(input);
            byte[] bytes = transformer.transform(source, uriResolver, errorListener);
            
            output.write(bytes);
            output.flush();
            
            if(errorListener.hasErrors()) {
                throw new IllegalStateException("Failed transformation: fatal errors: "
                        + errorListener.getFatalErrors() + ", and other errors: " + errorListener.getErrors()
                        + ", and warnings: " + errorListener.getWarnings());
            }
        } catch (TransformerException e) {
            throw new IllegalStateException("Could not perform the transformation of the metadata", e);
        } catch (IOException e) {
            throw new IllegalStateException("Could not deliver the transformed metadata to the output stream.", e);
        }
    }
    
    /**
     * Retrieves the transformer for the given type of transformation.
     * It will only instantiate each type of transformation once; and then reuse them.
     * @param type The type of transformation.
     * @return The transformer.
     */
    protected XslTransformer getTransformationFile(TransformationType type) {
        if(!transformers.containsKey(type)) {
            try {
                File res = new File(xsltDir, type.scriptName);
                if(!res.isFile()) {
                    throw new IllegalStateException("Could not find the transformation file: " + res.getAbsolutePath());
                }
                transformers.put(type, XslTransformer.getTransformer(res));
            } catch (TransformerConfigurationException e) {
                throw new IllegalStateException("Could not create a transformation", e);
            }
        }
        
        return transformers.get(type);    
    }
}
