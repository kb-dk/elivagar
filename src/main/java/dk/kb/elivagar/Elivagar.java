package dk.kb.elivagar;

import dk.kb.elivagar.characterization.CharacterizationHandler;
import dk.kb.elivagar.characterization.EpubCheckerCharacterizer;
import dk.kb.elivagar.characterization.FitsCharacterizer;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.metadata.AlmaPacker;
import dk.kb.elivagar.metadata.AlmaMetadataRetriever;
import dk.kb.elivagar.pubhub.PubhubMetadataRetriever;
import dk.kb.elivagar.pubhub.PubhubPacker;
import dk.kb.elivagar.pubhub.PubhubWorkflow;
import dk.kb.elivagar.transfer.TransferWorkflow;
import dk.kb.elivagar.utils.CalendarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

/**
 * Class for instantiating the Elivagar workflow.
 * Takes the following arguments:
 * <ul>
 *   <li>Configuration file </li>
 *   <li>Metadata modify date (OPTIONAL)</li>
 *   <li>
 *     <ul>
 *       <li>Must be in number of milliseconds ago.</li>
 *       <li>Use -1 (or less) for all books, or 0 for no books.</li>
 *     </ul>
 *   </li>
 *   <li>Max downloads (OPTIONAL)</li>
 *   <li>
 *     <ul>
 *       <li>Use non-positive integer for all books</li>
 *     </ul>
 *   </li>
 * </ul>
 * 
 * The two last options only deals with the metadata retrieval/packaging. 
 * All the book files will be packed.
 */
public class Elivagar {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(Elivagar.class);
    
    /** One minute in milliseconds.*/
    protected static final long ONE_MINUTE_IN_MILLIS = 60000L;
    
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
            long beginDate = System.currentTimeMillis() - ONE_MINUTE_IN_MILLIS;
            Configuration conf = Configuration.createFromYAMLFile(confFile);
            PubhubMetadataRetriever retriever = new PubhubMetadataRetriever(conf.getLicenseKey());
            FitsCharacterizer fitsCharacterizer = null;
            if(conf.getCharacterizationScriptFile() != null) {
                fitsCharacterizer = new FitsCharacterizer(conf.getCharacterizationScriptFile()); 
            }
            EpubCheckerCharacterizer epubCharacterizer = new EpubCheckerCharacterizer();
            CharacterizationHandler characterizer = new CharacterizationHandler(fitsCharacterizer, epubCharacterizer);
            HttpClient httpClient = new HttpClient();
            PubhubPacker packer = new PubhubPacker(conf, retriever.getServiceNamespace(), characterizer, httpClient);

            PubhubWorkflow pubhubWorkflow = new PubhubWorkflow(conf, retriever, characterizer, packer);
            
            AlmaMetadataRetriever almaMetadataRetriever = new AlmaMetadataRetriever(conf, new HttpClient());
            AlmaPacker alephWorkflow = new AlmaPacker(conf, almaMetadataRetriever);

            TransferWorkflow transferWorkflow = new TransferWorkflow(conf);
            
            if(modifyDate < 0) {
                pubhubWorkflow.retrieveAllBooks(maxDownloads);
            } else if(modifyDate > 0) {
                Date d = new Date(System.currentTimeMillis() - modifyDate);
                pubhubWorkflow.retrieveModifiedBooks(d, maxDownloads);
            } else {
                log.debug("No data retrieval.");
            }
            pubhubWorkflow.packFilesForBooks();
            alephWorkflow.packAlmaMetadataForBooks();
            transferWorkflow.run();
            
            File statisticsFile = new File(conf.getStatisticsDir(), 
                    CalendarUtils.getDateAsString(new Date()) + ".xml");
            try (PrintStream ps = new PrintStream(statisticsFile)) {
                pubhubWorkflow.makeStatistics(ps, beginDate);                
            }
            log.info("Finished! Written statistics at " + statisticsFile.getAbsolutePath());
        } catch (IOException | JAXBException | RuntimeException e) {
            log.error("Failure to run the workflow. \nThe waters of Elivagar must have frozen over!", e);
            System.exit(1);
        }
    }
}

