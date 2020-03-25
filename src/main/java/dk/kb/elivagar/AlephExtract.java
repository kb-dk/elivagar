package dk.kb.elivagar;

import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.metadata.AlmaMetadataRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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

    protected static File outputDir = new File(".");

    /**
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
            
            AlmaMetadataRetriever almaMetadataRetriever = new AlmaMetadataRetriever(conf, httpClient);
            for(int i = 1; i < args.length; i++) {
                String isbn = args[i];
                try {
                    retrieveMetadataForIsbn(conf, almaMetadataRetriever, isbn);
                } catch (IOException e) {
                    log.warn("Issue occured when retrieving the metadata for ISBN: '" + isbn + "'", e);
                }
            }
        } catch (Exception e ) {
            throw new IllegalStateException("Failure to retrieve Aleph metadata and transforming it to MODS.", e);
        }
    }
    
    /**
     * Retrieves the different kinds of metadata for given ISBN number.
     * @param conf The configuration.
     * @param almaMetadataRetriever The Alma metadata retriever.
     * @param isbn The ISBN number of the record to retrieve the metadata for.
     */
    protected static void retrieveMetadataForIsbn(Configuration conf, AlmaMetadataRetriever almaMetadataRetriever,
                                                  String isbn) throws IOException {
        log.info("Retrieving the metadata for ISBN: '" + isbn + "'");
        File modsMetadataFile = new File(outputDir, isbn + ".mods.xml");
        try (OutputStream out = new FileOutputStream(modsMetadataFile)) {
            almaMetadataRetriever.retrieveMetadataForISBN(isbn, out);
        }
        log.info("Metadata for ISBN '" + isbn + "' can be found at: " + modsMetadataFile.getAbsolutePath());
    }
}
