package dk.kb.elivagar;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import dk.kb.elivagar.metadata.AlmaMetadataRetriever;
import org.jaccept.structure.ExtendedTestCase;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.testutils.PreventSystemExit;
import dk.kb.elivagar.testutils.TestConfigurations;
import dk.kb.elivagar.testutils.TestFileUtils;

public class AlmaExtractTest extends ExtendedTestCase {

    String ID = "9788711436981";
    File testConfFile = new File("src/test/resources/elivagar.yml");
    
    Configuration conf;
    
    @BeforeMethod
    public void setup() throws IOException {
        TestFileUtils.setup();
        conf = TestConfigurations.getConfigurationForTest();
        AlmaExtract.outputDir = TestFileUtils.getTempDir();
    }

    @AfterClass
    public void tearDown() throws Exception {
        TestFileUtils.tearDown();
    }

    @Test(expectedExceptions = PreventSystemExit.ExitTrappedException.class)
    public void testNotEnoughArguments() {
        addDescription("Test the case, when not enough argument are given.");
        try {
            PreventSystemExit.forbidSystemExitCall();
            AlmaExtract.main(new String[]{"ARG1"});
        } finally {
            PreventSystemExit.enableSystemExitCall();
        }
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testFailToConnect() {
        addDescription("Test the case, when we cannot connect to the Alma server.");
        try {
            PreventSystemExit.forbidSystemExitCall();
            AlmaExtract.main(new String[]{testConfFile.getAbsolutePath(), ID});
        } finally {
            PreventSystemExit.enableSystemExitCall();
        }        
    }
    
    @Test
    public void testRetrieveMetadataForIsbn() throws IOException{
        addDescription("Test the retrieveMetadataForIsbn method.");
        
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);
        String isbn = UUID.randomUUID().toString();
        
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                OutputStream out = (OutputStream) invocation.getArguments()[1];
                out.write("THIS IS A TEST".getBytes());
                out.flush();
                return null;
            }
        }).when(retriever).retrieveMetadataForISBN(eq(isbn), any(OutputStream.class));
        
        AlmaExtract.retrieveMetadataForIsbn(conf, retriever, isbn);
        
        verify(retriever).retrieveMetadataForISBN(eq(isbn), any(OutputStream.class));
        verifyNoMoreInteractions(retriever);
    }
}
