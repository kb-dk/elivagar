package dk.kb.elivagar;

import java.io.File;
import java.util.Date;

/**
 * Class for instantiating the Elivagar workflow.
 *
 */
public class Elivagar {

    /**
     * Main method.
     * Requires at least the arguments of the 
     * @param args
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
        long modifyDate = -1;
        if(args.length > 1) {
            modifyDate = Long.parseLong(args[1]);
        }
        long maxDownloads = -1;
        if(args.length > 2) {
            maxDownloads = Long.parseLong(args[2]);
        }
        
        try {
            Configuration conf = Configuration.createFromYAMLFile(confFile);
            PubhubWorkflow workflow = new PubhubWorkflow(conf);
            
            if(modifyDate < 0) {
                workflow.retrieveAllBooks(maxDownloads);
            } else {
                Date d = new Date(System.currentTimeMillis() - modifyDate);
                workflow.retrieveModifiedBooks(d, maxDownloads);
            }
        } catch (Exception e) {
            System.err.println("Failure to run the workflow. \nThe waters of Elivagar must hav run over!");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
    
