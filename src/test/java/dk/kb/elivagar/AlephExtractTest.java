package dk.kb.elivagar;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.metadata.AlephMetadataRetriever;
import dk.kb.elivagar.metadata.MetadataTransformer;
import dk.kb.elivagar.testutils.PreventSystemExit;
import dk.kb.elivagar.testutils.TestConfigurations;
import dk.kb.elivagar.testutils.TestFileUtils;
import dk.kb.elivagar.utils.StreamUtils;

public class AlephExtractTest extends ExtendedTestCase {

    String ID = "9788711436981";
    File testConfFile = new File("src/test/resources/elivagar.yml");
    
    Configuration conf;
    
    @BeforeMethod
    public void setup() throws IOException {
        TestFileUtils.setup();
        conf = TestConfigurations.getConfigurationForTest();
    }

    @Test(expectedExceptions = PreventSystemExit.ExitTrappedException.class)
    public void testNotEnoughArguments() {
        addDescription("Test the case, when not enough argument are given.");
        try {
            PreventSystemExit.forbidSystemExitCall();
            AlephExtract.main(new String[]{"ARG1"});
        } finally {
            PreventSystemExit.enableSystemExitCall();
        }
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testFailToConnect() {
        addDescription("Test the case, when we cannot connect to the Aleph server.");
        try {
            PreventSystemExit.forbidSystemExitCall();
            AlephExtract.main(new String[]{testConfFile.getAbsolutePath(), ID});
        } finally {
            PreventSystemExit.enableSystemExitCall();
        }        
    }
    
    @Test
    public void testRetrieveMetadataForIsbn() throws IOException{
        addDescription("Test the retrieveMetadataForIsbn method.");
        
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
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
        
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                InputStream in = (InputStream) invocation.getArguments()[0];
                OutputStream out = (OutputStream) invocation.getArguments()[1];
                StreamUtils.copyInputStreamToOutputStream(in, out);
                return null;
            }
        }).when(transformer).transformMetadata(any(InputStream.class), any(OutputStream.class), eq(MetadataTransformer.TransformationType.ALEPH_TO_MARC21));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                InputStream in = (InputStream) invocation.getArguments()[0];
                OutputStream out = (OutputStream) invocation.getArguments()[1];
                StreamUtils.copyInputStreamToOutputStream(in, out);
                return null;
            }
        }).when(transformer).transformMetadata(any(InputStream.class), any(OutputStream.class), eq(MetadataTransformer.TransformationType.MARC21_TO_MODS));
        
        AlephExtract.retrieveMetadataForIsbn(conf, transformer, retriever, isbn);
        
        verify(transformer).transformMetadata(any(InputStream.class), any(OutputStream.class), eq(MetadataTransformer.TransformationType.ALEPH_TO_MARC21));
        verify(transformer).transformMetadata(any(InputStream.class), any(OutputStream.class), eq(MetadataTransformer.TransformationType.MARC21_TO_MODS));
        verifyNoMoreInteractions(transformer);
        
        verify(retriever).retrieveMetadataForISBN(eq(isbn), any(OutputStream.class));
        verifyNoMoreInteractions(retriever);
    }
}
