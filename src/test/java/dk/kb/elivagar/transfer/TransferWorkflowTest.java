package dk.kb.elivagar.transfer;

import org.jaccept.structure.ExtendedTestCase;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.config.TransferConfiguration;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class TransferWorkflowTest extends ExtendedTestCase {

    @Test
    public void testWithNoTransferConfiguration() {
        addDescription("Test the transfer workflow, when the transfer workflow is missing (null).");
        Configuration conf = Mockito.mock(Configuration.class);
        
        Mockito.when(conf.getTransferConfiguration()).thenReturn(null);
        
        TransferWorkflow workflow = new TransferWorkflow(conf);
        
        Assert.assertNull(workflow.transfer);
        
        workflow.run();
        
        Mockito.verify(conf).getTransferConfiguration();
        Mockito.verifyNoMoreInteractions(conf);
    }

    @Test
    public void testWithTransferConfiguration() {
        addDescription("Test the transfer workflow, when the transfer workflow is missing (null).");
        Configuration conf = Mockito.mock(Configuration.class);
        TransferConfiguration transferConf = Mockito.mock(TransferConfiguration.class);
        Mockito.when(conf.getTransferConfiguration()).thenReturn(transferConf);
        
        TransferWorkflow workflow = new TransferWorkflow(conf);
        
        Assert.assertNotNull(workflow.transfer);
        
        addStep("Set a mock PreIngestTransfer to test the running", "Calls the transferReadyBooks method");
        
        PreIngestTransfer transfer = Mockito.mock(PreIngestTransfer.class);
        workflow.transfer = transfer;
        
        workflow.run();
        
        Mockito.verify(transfer).transferReadyBooks();
        Mockito.verifyNoMoreInteractions(transfer);
        
        Mockito.verify(conf).getTransferConfiguration();
        Mockito.verifyNoMoreInteractions(conf);
    }
}
