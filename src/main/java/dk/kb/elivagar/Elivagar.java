package dk.kb.elivagar;

import java.io.File;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for instantiating the Elivagar workflow.
 * Takes the following arguments:
 * <ul>
 *   <li>Configuration file </li>
 *   <li>Metadata modify date (OPTIONAL)</li>
 *   <ul>
 *     <li>Must be in number of milliseconds ago.</li>
 *     <li>Use -1 (or less) for all books, or 0 for no books.</li>
 *   </ul>
 *   <li>Max downloads (OPTIONAL)</li>
 *   <ul>
 *     <li>Use -1 for all books</li>
 *   </ul>
 * </ul>
 * 
 * The two last options only deals with the metadata retrieval/packaging. 
 * All the book files will be packed.
 */
public class Elivagar {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(Elivagar.class);

    /**
     * Main method.
     * Requires at least the one argument for the configuration.
     * @param args The arguments.
     */
    public static void main(String ... args) {
        if(args.length < 1) {
            System.err.println("Needs at least one argument; the configuration file.");
            System.exit(-1);
        }
        String confPath = args[0];
        File confFile = new File(confPath); 
        if(!confFile.isFile()) {
            System.err.println("The configuration file '" + confFile.getAbsolutePath() + "' is not a valid file.");
            System.exit(-1);
        }
        log.debug("[ARG1] Using configuration file: " + confPath);
        long modifyDate = -1;
        if(args.length > 1) {
            modifyDate = Long.parseLong(args[1]);
            if(modifyDate == 0L) {
                log.debug("[ARG2] Not extracting any metadata for books.");
            } else {
                log.debug("[ARG2] Only extracting metadata for books, which has been modified within the last '"
                        + modifyDate + "' milliseconds.");
            }
        } else {
            log.debug("[ARG2] No modify time limit for the books.");
        }
        long maxDownloads = -1;
        if(args.length > 2) {
            maxDownloads = Long.parseLong(args[2]);
        }
        if(maxDownloads > 0) {
            log.debug("[ARG3] Maximum packages the metadata for '" + maxDownloads + "' books.");
        } else {
            maxDownloads = Long.MAX_VALUE;
            log.debug("[ARG3] Downloading and packaging the metadata of as many books as possible.");
        }

        try {
            Configuration conf = Configuration.createFromYAMLFile(confFile);
            PubhubWorkflow workflow = new PubhubWorkflow(conf);

            if(modifyDate < 0) {
                workflow.retrieveAllBooks(maxDownloads);
            } else if(modifyDate > 0) {
                Date d = new Date(System.currentTimeMillis() - modifyDate);
                workflow.retrieveModifiedBooks(d, maxDownloads);
            } else {
                log.debug("No data retrieval.");
            }
            workflow.packFilesForBooks();
            workflow.makeStatistics(System.out);
        } catch (Exception e) {
            log.error("Failure to run the workflow. \nThe waters of Elivagar must have frozen over!", e);
            System.exit(-1);
        }
    }
}

