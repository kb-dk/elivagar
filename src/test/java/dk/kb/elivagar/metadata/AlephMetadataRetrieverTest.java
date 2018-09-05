package dk.kb.elivagar.metadata;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.log4j.lf5.util.StreamUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.config.AlephConfiguration;
import dk.kb.elivagar.testutils.TestConfigurations;
import dk.kb.elivagar.testutils.TestFileUtils;

public class AlephMetadataRetrieverTest extends ExtendedTestCase {

    String ID = "9788711436981";
    AlephConfiguration configuration;

    @BeforeClass
    public void setup() throws IOException {
        TestFileUtils.setup();
        configuration = TestConfigurations.getAlephConfigurationForTest();
    }

    @Test(enabled = false)
    public void testCompleteRetrieval() {
        addDescription("Test a complete retrieval of metadata from Aleph based on a ISBN.");
        HttpClient httpClient = new HttpClient();

        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        retriever.retrieveMetadataForISBN(ID, baos);

        String extractedMetadata = baos.toString();

        Assert.assertTrue(extractedMetadata.contains("<subfield label=\"e\">" + ID + "</subfield>"));
    }

    @Test(enabled = false)
    public void testRealPerformAlephSearch() throws Exception {
        addDescription("Test the proper retrieval of an entry from Aleph.");
        HttpClient httpClient = new HttpClient();

        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);

        File searchResult = retriever.performAlephSearch(ID);
        String searchResults = TestFileUtils.readFile(searchResult);

        Assert.assertTrue(searchResults.contains("<no_records>000000001</no_records>"));
        Assert.assertTrue(searchResults.contains("<no_entries>000000001</no_entries>"));
    }
    
    @Test
    public void testRetrieveMetadataForISBN() throws IOException {
        addDescription("Test the retrieveMetadataForISBN method.");
        HttpClient httpClient = mock(HttpClient.class);
        
        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);
        
        doAnswer(new Answer<Void>() {
            boolean first = true;
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                File searchResult;
                if(first) {
                    first = false;
                    searchResult = TestFileUtils.copyFileToTemp(new File("src/test/resources/metadata/aleph_search.xml"));
                } else {
                    searchResult = TestFileUtils.copyFileToTemp(new File("src/test/resources/metadata/aleph_metadata.xml"));                    
                }
                OutputStream out = (OutputStream) invocation.getArguments()[1];
                StreamUtils.copy(new FileInputStream(searchResult), out);
                return null;
            }
        }).when(httpClient).retrieveUrlContent(anyString(), any(OutputStream.class));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        retriever.retrieveMetadataForISBN(ID, out);
        
        verify(httpClient, times(2)).retrieveUrlContent(anyString(), any(OutputStream.class));
        verifyNoMoreInteractions(httpClient);
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testRetrieveMetadataForISBNFailure() throws IOException {
        addDescription("Test the retrieveMetadataForISBN method when it fails to get search results.");
        HttpClient httpClient = mock(HttpClient.class);
        
        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);
        
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                File searchResult = TestFileUtils.copyFileToTemp(new File("src/test/resources/metadata/aleph_search_failure.xml"));
                OutputStream out = (OutputStream) invocation.getArguments()[1];
                StreamUtils.copy(new FileInputStream(searchResult), out);
                return null;
            }
        }).when(httpClient).retrieveUrlContent(anyString(), any(OutputStream.class));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        retriever.retrieveMetadataForISBN(ID, out);
    }
    
    @Test
    public void testDownloadAlephMetadata() throws IOException {
        addDescription("Test the downloadAlephMetadata method.");
        HttpClient httpClient = mock(HttpClient.class);
        String setNumber = "956754";
        
        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);
        
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(httpClient).retrieveUrlContent(anyString(), any(OutputStream.class));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        retriever.downloadAlephMetadata(setNumber, out);
        
        verify(httpClient).retrieveUrlContent(anyString(), any(OutputStream.class));
        verifyNoMoreInteractions(httpClient);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testDownloadAlephMetadataFailure() throws IOException {
        addDescription("Test the downloadAlephMetadata method when it fails.");
        HttpClient httpClient = mock(HttpClient.class);
        String setNumber = "956754";
        
        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);
        
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                throw new IOException("THIS TEST MUST FAIL!!!");
            }
        }).when(httpClient).retrieveUrlContent(anyString(), any(OutputStream.class));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        retriever.downloadAlephMetadata(setNumber, out);
    }

    @Test
    public void testGetAlephSetNumber() throws IOException {
        addDescription("Test the getAlephSetNumber method.");
        HttpClient httpClient = mock(HttpClient.class);
        String expectedSetNumber = "956754";

        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);
        
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                File searchResult = TestFileUtils.copyFileToTemp(new File("src/test/resources/metadata/aleph_search.xml"));
                OutputStream out = (OutputStream) invocation.getArguments()[1];
                StreamUtils.copy(new FileInputStream(searchResult), out);
                return null;
            }
        }).when(httpClient).retrieveUrlContent(anyString(), any(OutputStream.class));

        String setNumber = retriever.getAlephSetNumber(ID);
        Assert.assertEquals(setNumber, expectedSetNumber);
        
        verify(httpClient).retrieveUrlContent(anyString(), any(OutputStream.class));
        verifyNoMoreInteractions(httpClient);
    }

    @Test
    public void testPerformAlephSearch() throws Exception {
        addDescription("Test the retrieval of search results from Aleph.");
        HttpClient httpClient = mock(HttpClient.class);

        final String id = UUID.randomUUID().toString();

        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                String url = (String) invocation.getArguments()[0];
                Assert.assertTrue(url.contains(id));
                Assert.assertTrue(url.contains(configuration.getServerUrl()));
                Assert.assertTrue(url.contains(configuration.getBase()));
                Assert.assertTrue(url.contains(AlephMetadataRetriever.OPERATION_FIND));
                return null;
            }
        }).when(httpClient).retrieveUrlContent(anyString(), any(OutputStream.class));

        retriever.performAlephSearch(id);
        
        verify(httpClient).retrieveUrlContent(anyString(), any(OutputStream.class));
        verifyNoMoreInteractions(httpClient);
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testPerformAlephSearchFailure() throws Exception {
        addDescription("Test when the aleph search throws an IOException.");
        HttpClient httpClient = mock(HttpClient.class);

        String id = UUID.randomUUID().toString();

        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws IOException {
                throw new IOException("THIS TEST MUST FAIL");
            }
        }).when(httpClient).retrieveUrlContent(anyString(), any(OutputStream.class));

        retriever.performAlephSearch(id);
    }

    @Test
    public void testFindSetNumberInFileSuccess() throws Exception {
        addDescription("Test the findSetNumberInFile method, when it succesfully finds a SetNumber");
        HttpClient httpClient = mock(HttpClient.class);
        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);
        
        File f = TestFileUtils.copyFileToTemp(new File("src/test/resources/metadata/aleph_search.xml"));
        String expectedSetNumber = "956754";
        
        String setNumber = retriever.findSetNumberInFile(f);
        
        Assert.assertEquals(setNumber, expectedSetNumber);
        verifyZeroInteractions(httpClient);
    }
    
    @Test
    public void testFindSetNumberInFileMultiResults() throws Exception {
        addDescription("Test the findSetNumberInFile method, when finds multiple results");
        HttpClient httpClient = mock(HttpClient.class);
        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);
        
        File f = TestFileUtils.copyFileToTemp(new File("src/test/resources/metadata/aleph_search2.xml"));
        String expectedSetNumber = "956754";
        
        String setNumber = retriever.findSetNumberInFile(f);
        
        Assert.assertEquals(setNumber, expectedSetNumber);
        verifyZeroInteractions(httpClient);
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testFindSetNumberInFileErrorFromAleph() throws Exception {
        addDescription("Test the findSetNumberInFile method, when Aleph delivers an error");
        HttpClient httpClient = mock(HttpClient.class);
        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);
        
        File f = TestFileUtils.copyFileToTemp(new File("src/test/resources/metadata/aleph_search_failure.xml"));
        
        retriever.findSetNumberInFile(f);
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testFindSetNumberInFileNoResults() throws Exception {
        addDescription("Test the findSetNumberInFile method, when Aleph delivers no results");
        HttpClient httpClient = mock(HttpClient.class);
        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);
        
        File f = TestFileUtils.copyFileToTemp(new File("src/test/resources/metadata/aleph_search_no_entries.xml"));
        
        retriever.findSetNumberInFile(f);
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testFindSetNumberInFileIOException() throws Exception {
        addDescription("Test the findSetNumberInFile method, when given a non-existing file");
        HttpClient httpClient = mock(HttpClient.class);
        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);
        
        File f = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        retriever.findSetNumberInFile(f);
    }
    
    @Test(expectedExceptions = IllegalStateException.class)
    public void testFindSetNumberInFileParserException() throws Exception {
        addDescription("Test the findSetNumberInFile method, when given a file, which is not XML");
        HttpClient httpClient = mock(HttpClient.class);
        AlephMetadataRetriever retriever = new AlephMetadataRetriever(configuration, httpClient);
        
        File f = new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString());
        TestFileUtils.createFile(f, UUID.randomUUID().toString());
        retriever.findSetNumberInFile(f);
    }
}
