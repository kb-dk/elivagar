package dk.kb.elivagar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.metadata.AlephMetadataRetriever;
import dk.kb.elivagar.metadata.MetadataTransformer;

/**
 * Extracts the metadata from Aleph and transform it into MARC and MODS.
 * 
 * Usage:
 * dk.kb.elivagar.AlephExtract /PATH/TO/elivagar.yml [ISBN]+
 * 
 */
public class AlephExtract {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(AlephExtract.class);

    /**
     * 
     * @param args The arguments.
     */
    public static void main(String[] args) {
        if(args.length < 2) {
            System.err.println("Needs at least two arguments: ");
            System.err.println(" * The configuration file.");
            System.err.println(" * List of ISBN of books to retrieve.");
            System.exit(-1);
        }
        String confPath = args[0];
        File confFile = new File(confPath);
        
        try {
            Configuration conf = Configuration.createFromYAMLFile(confFile);
            HttpClient httpClient = new HttpClient();
            
            MetadataTransformer transformer = new MetadataTransformer(conf.getXsltFileDir());
            AlephMetadataRetriever metadataRetriever = new AlephMetadataRetriever(conf.getAlephConfiguration(), 
                    httpClient);
            for(int i = 1; i < args.length; i++) {
                retrieveMetadataForIsbn(conf, transformer, metadataRetriever, args[i]);
            }
        } catch (Exception e ) {
            throw new IllegalStateException("Failure to retrieve Aleph metadata and transforming it to MODS.", e);
        }
    }
    
    /**
     * Retrieves the different kinds of metadata for given ISBN number.
     * @param conf The configuration.
     * @param transformer The metadata transformer.
     * @param retriever The Aleph metadata retriever.
     * @param isbn The ISBN number of the record to retrieve the metadata for.
     */
    protected static void retrieveMetadataForIsbn(Configuration conf, MetadataTransformer transformer, 
            AlephMetadataRetriever retriever, String isbn) {
        log.info("Retrieving the metadata for ISBN: '" + isbn + "'");
        try {
            File alephMetadataFile = new File(conf.getAlephConfiguration().getTempDir(), isbn + ".aleph.xml");
            try (OutputStream out = new FileOutputStream(alephMetadataFile)) {
                retriever.retrieveMetadataForISBN(isbn, out);
            }
            
            File marcMetadataFile = new File(conf.getAlephConfiguration().getTempDir(), isbn + ".marc.xml");
            try (InputStream in = new FileInputStream(alephMetadataFile); 
                    OutputStream out = new FileOutputStream(marcMetadataFile)) {
                transformer.transformMetadata(in, out, MetadataTransformer.TransformationType.ALEPH_TO_MARC21);
            }

            File modsMetadataFile = new File(conf.getAlephConfiguration().getTempDir(), isbn + ".mods.xml");
            try (InputStream in = new FileInputStream(marcMetadataFile); 
                    OutputStream out = new FileOutputStream(modsMetadataFile)) {
                transformer.transformMetadata(in, out, MetadataTransformer.TransformationType.MARC21_TO_MODS);
            }
            log.info("Metadata for ISBN '" + isbn + "' can be found at: " + conf.getAlephConfiguration().getTempDir());
        } catch (IOException e) {
            log.warn("Issue occured when retrieving the metadata for ISBN: '" + isbn + "'", e);
        }
    }
}
