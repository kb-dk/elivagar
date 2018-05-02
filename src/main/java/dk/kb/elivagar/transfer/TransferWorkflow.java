package dk.kb.elivagar.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.config.Configuration;

/**
 * The workflow for the transfer module.
 * Handles the case when the transfer has been disabled.
 */
public class TransferWorkflow {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(TransferWorkflow.class);
    
    /** The pre-ingest transfer.*/
    protected PreIngestTransfer transfer;
    
    /**
     * Constructor.
     * @param conf The configuration.
     */
    public TransferWorkflow(Configuration conf) {
        if(conf.getTransferConfiguration() != null) {
            this.transfer = new PreIngestTransfer(conf);
        } else {
            this.transfer = null;
        }
    }
    
    /**
     * Runs the workflow.
     * It will not do anything, if the transfer configuration is disabled.
     */
    public void run() {
        if(transfer == null) {
            log.info("The transfer is disabled.");
        } else {
            log.debug("Running the pre-ingest transfer.");
            transfer.transferReadyBooks(); 
        }
    }
}
