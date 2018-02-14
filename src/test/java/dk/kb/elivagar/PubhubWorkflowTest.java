package dk.kb.elivagar;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import dk.kb.elivagar.characterization.CharacterizationHandler;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.pubhub.PubhubMetadataRetriever;
import dk.kb.elivagar.pubhub.PubhubPacker;
import dk.kb.elivagar.testutils.TestConfigurations;
import dk.kb.elivagar.testutils.TestFileUtils;
import dk.kb.elivagar.utils.FileUtils;
import dk.pubhub.service.ArrayOfBook;
import dk.pubhub.service.Book;
import dk.pubhub.service.ModifiedBookList;

public class PubhubWorkflowTest extends ExtendedTestCase {

    public static final String PDF_SUFFIX = ".pdf";
    public static final String EPUB_SUFFIX = Constants.EPUB_FILE_SUFFIX;
    
    Long MILLIS_PER_YEAR = 31556908800L; // from wiki

    Configuration conf;

    @BeforeClass
    public void setup() throws Exception {
        conf = TestConfigurations.getConfigurationForTest();
    }

    @BeforeMethod
    public void setupMethod() throws Exception {
        TestFileUtils.setup();        
    }
    
    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }

    @Test
    public void testRetrieveAllBooks() throws Exception {
        addDescription("Test the retrieveAllBooks method.");
        PubhubMetadataRetriever retriever = mock(PubhubMetadataRetriever.class);
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        PubhubPacker packer = mock(PubhubPacker.class);
        PubhubWorkflow elivagarWorkflow = new PubhubWorkflow(conf, retriever, characterizer, packer);
        
        Book testBook1 = mock(Book.class);
        Book testBook2 = mock(Book.class);
        ArrayOfBook bookArray = mock(ArrayOfBook.class);
        when(retriever.downloadAllBookMetadata()).thenReturn(bookArray);
        when(bookArray.getBook()).thenReturn(Arrays.asList(testBook1, testBook2));
        
        elivagarWorkflow.retrieveAllBooks(10);
        
        verifyZeroInteractions(characterizer);
        verifyZeroInteractions(testBook1);
        verifyZeroInteractions(testBook2);
        
        verify(retriever).downloadAllBookMetadata();
        verifyNoMoreInteractions(retriever);
        
        verify(packer).packBook(eq(testBook1));
        verify(packer).packBook(eq(testBook2));
        verifyNoMoreInteractions(packer);
        
        verify(bookArray).getBook();
        verifyNoMoreInteractions(bookArray);
    }

    @Test
    public void testRetrieveAllBooksWhenCountIsTheLimit() throws Exception {
        addDescription("Test the retrieveAllBooks method when the count is the limit.");
        PubhubMetadataRetriever retriever = mock(PubhubMetadataRetriever.class);
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        PubhubPacker packer = mock(PubhubPacker.class);
        PubhubWorkflow elivagarWorkflow = new PubhubWorkflow(conf, retriever, characterizer, packer);
        
        Book testBook1 = mock(Book.class);
        Book testBook2 = mock(Book.class);
        ArrayOfBook bookArray = mock(ArrayOfBook.class);
        when(retriever.downloadAllBookMetadata()).thenReturn(bookArray);
        when(bookArray.getBook()).thenReturn(Arrays.asList(testBook1, testBook2));
        
        elivagarWorkflow.retrieveAllBooks(1);
        
        verifyZeroInteractions(characterizer);
        verifyZeroInteractions(testBook1);
        verifyZeroInteractions(testBook2);
        
        verify(retriever).downloadAllBookMetadata();
        verifyNoMoreInteractions(retriever);
        
        verify(packer).packBook(eq(testBook1));
        verifyNoMoreInteractions(packer);
        
        verify(bookArray).getBook();
        verifyNoMoreInteractions(bookArray);
    }
    
    @Test
    public void testRetrieveModifiedBooks() throws Exception {
        addDescription("Test the retrieveModifiedBooks method");
        PubhubMetadataRetriever retriever = mock(PubhubMetadataRetriever.class);
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        PubhubPacker packer = mock(PubhubPacker.class);
        PubhubWorkflow elivagarWorkflow = new PubhubWorkflow(conf, retriever, characterizer, packer);
        
        Date d = new Date(1234567890);
        Book testBook1 = mock(Book.class);
        Book testBook2 = mock(Book.class);
        ModifiedBookList bookList = mock(ModifiedBookList.class);
        ArrayOfBook bookArray = mock(ArrayOfBook.class);
        when(retriever.downloadBookMetadataAfterModifyDate(eq(d))).thenReturn(bookList);
        when(bookList.getNewAndModifiedBooks()).thenReturn(bookArray);
        when(bookArray.getBook()).thenReturn(Arrays.asList(testBook1, testBook2));
        
        elivagarWorkflow.retrieveModifiedBooks(d, 10);
        
        verifyZeroInteractions(characterizer);
        verifyZeroInteractions(testBook1);
        verifyZeroInteractions(testBook2);
        
        verify(retriever).downloadBookMetadataAfterModifyDate(eq(d));
        verifyNoMoreInteractions(retriever);
        
        verify(packer).packBook(eq(testBook1));
        verify(packer).packBook(eq(testBook2));
        verifyNoMoreInteractions(packer);
        
        verify(bookList).getNewAndModifiedBooks();
        verifyNoMoreInteractions(bookList);
        
        verify(bookArray).getBook();
        verifyNoMoreInteractions(bookArray);
    }

    @Test
    public void testRetrieveModifiedBooksWhenCountIsTheLimit() throws Exception {
        addDescription("Test the retrieveModifiedBooks method, when the maximum amount value is the limit.");
        PubhubMetadataRetriever retriever = mock(PubhubMetadataRetriever.class);
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        PubhubPacker packer = mock(PubhubPacker.class);
        PubhubWorkflow elivagarWorkflow = new PubhubWorkflow(conf, retriever, characterizer, packer);
        
        Date d = new Date(1234567890);
        Book testBook1 = mock(Book.class);
        Book testBook2 = mock(Book.class);
        ModifiedBookList bookList = mock(ModifiedBookList.class);
        ArrayOfBook bookArray = mock(ArrayOfBook.class);
        when(retriever.downloadBookMetadataAfterModifyDate(eq(d))).thenReturn(bookList);
        when(bookList.getNewAndModifiedBooks()).thenReturn(bookArray);
        when(bookArray.getBook()).thenReturn(Arrays.asList(testBook1, testBook2));
        
        elivagarWorkflow.retrieveModifiedBooks(d, 1);
        
        verifyZeroInteractions(characterizer);
        verifyZeroInteractions(testBook1);
        verifyZeroInteractions(testBook2);
        
        verify(retriever).downloadBookMetadataAfterModifyDate(eq(d));
        verifyNoMoreInteractions(retriever);
        
        verify(packer).packBook(eq(testBook1));
        verifyNoMoreInteractions(packer);
        
        verify(bookList).getNewAndModifiedBooks();
        verifyNoMoreInteractions(bookList);
        
        verify(bookArray).getBook();
        verifyNoMoreInteractions(bookArray);
    }
    
    @Test
    public void testPackingBooksWithOnlyEbooks() throws Exception {
        addDescription("Test the packFilesForBooks method, when there is only Ebooks.");
        PubhubMetadataRetriever retriever = mock(PubhubMetadataRetriever.class);
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        PubhubPacker packer = mock(PubhubPacker.class);
        PubhubWorkflow elivagarWorkflow = new PubhubWorkflow(conf, retriever, characterizer, packer);

        TestFileUtils.createEmptyDirectory(conf.getEbookFileDir().getAbsolutePath());
        
        int numberOfBooks = 10;
        for(int i = 0; i < numberOfBooks; i++) {
            File testFile = new File(conf.getEbookFileDir(), UUID.randomUUID().toString() + conf.getEbookFormats().get(0));
            TestFileUtils.createFile(testFile, UUID.randomUUID().toString());
        }
        // Add a directory.
        FileUtils.createDirectory(conf.getEbookFileDir() + "/" + UUID.randomUUID().toString());

        elivagarWorkflow.packFilesForBooks();

        Assert.assertEquals(conf.getEbookFileDir().list().length, 11);

        verifyZeroInteractions(retriever);
        verifyZeroInteractions(characterizer);
        
        verify(packer, times(10)).packFileForEbook(any(File.class));
        verifyNoMoreInteractions(packer);
    }

    @Test
    public void testPackingBooksWithOnlyAudioBooks() throws Exception {
        addDescription("Test the packFilesForBooks method, when there is only Audio books.");
        PubhubMetadataRetriever retriever = mock(PubhubMetadataRetriever.class);
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        PubhubPacker packer = mock(PubhubPacker.class);
        PubhubWorkflow elivagarWorkflow = new PubhubWorkflow(conf, retriever, characterizer, packer);

        TestFileUtils.createEmptyDirectory(conf.getAudioFileDir().getAbsolutePath());
        
        int numberOfBooks = 10;
        for(int i = 0; i < numberOfBooks; i++) {
            String id = UUID.randomUUID().toString();
            File audioDir = FileUtils.createDirectory(conf.getAudioFileDir() + "/" + id);
            File audioFileDir = FileUtils.createDirectory(audioDir.getAbsolutePath() + "/" + PubhubWorkflow.AUDIO_SUB_DIR_PATH);
            File testFile = new File(audioFileDir, id + conf.getAudioFormats().get(0));
            TestFileUtils.createFile(testFile, UUID.randomUUID().toString());
        }
        // Add a regular file.
        TestFileUtils.createFile(new File(conf.getAudioFileDir(), UUID.randomUUID().toString()), UUID.randomUUID().toString());

        elivagarWorkflow.packFilesForBooks();

        Assert.assertEquals(conf.getAudioFileDir().list().length, 11);

        verifyZeroInteractions(retriever);
        verifyZeroInteractions(characterizer);
        
        verify(packer, times(10)).packFileForAudio(any(File.class));
        verifyNoMoreInteractions(packer);
    }
    
    @Test
    public void testPackFilesForEbooksFailure() throws Exception {
        addDescription("Test the packFilesForEbooks method, when there the retriever fails to handle the Ebook.");
        PubhubMetadataRetriever retriever = mock(PubhubMetadataRetriever.class);
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        PubhubPacker packer = mock(PubhubPacker.class);
        PubhubWorkflow elivagarWorkflow = new PubhubWorkflow(conf, retriever, characterizer, packer);

        TestFileUtils.createEmptyDirectory(conf.getEbookFileDir().getAbsolutePath());
        
        File testFile = new File(conf.getEbookFileDir(), UUID.randomUUID().toString() + conf.getEbookFormats().get(0));
        TestFileUtils.createFile(testFile, UUID.randomUUID().toString());
        
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                throw new IOException("FAILURE");
            }
        }).when(packer).packFileForEbook(eq(testFile));
        
        elivagarWorkflow.packFilesForEbooks();
        

        verifyZeroInteractions(retriever);
        verifyZeroInteractions(characterizer);
        
        verify(packer).packFileForEbook(eq(testFile));
        verifyNoMoreInteractions(packer);
    }

    @Test
    public void testPackFilesForAudioBookFailure() throws Exception {
        addDescription("Test the packFilesForAudioBooks method, when there the retriever fails to handle the AudioBook.");
        PubhubMetadataRetriever retriever = mock(PubhubMetadataRetriever.class);
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        PubhubPacker packer = mock(PubhubPacker.class);
        PubhubWorkflow elivagarWorkflow = new PubhubWorkflow(conf, retriever, characterizer, packer);

        TestFileUtils.createEmptyDirectory(conf.getAudioFileDir().getAbsolutePath());
        
        String id = UUID.randomUUID().toString();
        File audioDir = FileUtils.createDirectory(conf.getAudioFileDir() + "/" + id);
        File audioFileDir = FileUtils.createDirectory(audioDir.getAbsolutePath() + "/" + PubhubWorkflow.AUDIO_SUB_DIR_PATH);
        File testFile = new File(audioFileDir, id + conf.getAudioFormats().get(0));
        TestFileUtils.createFile(testFile, UUID.randomUUID().toString());
        
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                throw new IOException("FAILURE");
            }
        }).when(packer).packFileForAudio(eq(testFile));
        
        elivagarWorkflow.packFilesForAudioBooks();
        

        verifyZeroInteractions(retriever);
        verifyZeroInteractions(characterizer);
        
        verify(packer).packFileForAudio(eq(testFile));
        verifyNoMoreInteractions(packer);
    }
    
    @Test
    public void testStatisticsWhenBooksDirsHaveBeenReplacedWithFiles() throws IOException {
        addDescription("Test the case for generating the statistics, when the book output directories have been replaced by files.");
        PubhubMetadataRetriever retriever = mock(PubhubMetadataRetriever.class);
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        PubhubPacker packer = mock(PubhubPacker.class);
        PubhubWorkflow elivagarWorkflow = new PubhubWorkflow(conf, retriever, characterizer, packer);

        PrintStream printer = mock(PrintStream.class);
        
        TestFileUtils.createFile(conf.getAudioOutputDir(), "audio");
        TestFileUtils.createFile(conf.getEbookOutputDir(), "ebook");
        
        elivagarWorkflow.makeStatistics(printer, 0L);
        
    }
    
    @Test(enabled = false)
    public void integrationTestElivagarRetrievingBooks() throws Exception {
        PubhubMetadataRetriever retriever = new PubhubMetadataRetriever(conf.getLicenseKey());
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        PubhubPacker packer = mock(PubhubPacker.class);
        PubhubWorkflow elivagarWorkflow = new PubhubWorkflow(conf, retriever, characterizer, packer);
        
        int count = 10;
        elivagarWorkflow.retrieveAllBooks(count);
        System.out.println("Marshaled all Books to individual files");

        Assert.assertEquals(conf.getEbookFileDir().list().length, count);
    }

    @Test(enabled = false)
    public void integrationTestElivagarRetrievingModifiedBooks() throws Exception {
        PubhubMetadataRetriever retriever = new PubhubMetadataRetriever(conf.getLicenseKey());
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        PubhubPacker packer = mock(PubhubPacker.class);
        PubhubWorkflow elivagarWorkflow = new PubhubWorkflow(conf, retriever, characterizer, packer);
        int count = 10;
        Date oneYearAgo = new Date(System.currentTimeMillis()-MILLIS_PER_YEAR);
        elivagarWorkflow.retrieveModifiedBooks(oneYearAgo, count);

        Assert.assertEquals(calculateNumberOfEbooksAndAudioBooks(), count);
    }

    protected int calculateNumberOfEbooksAndAudioBooks() {
        // Annoying way of calculating the number of books, but necessary, since the 'list()' might return null, if empty.
        int res = 0;
        if(conf.getEbookOutputDir().list() != null) {
            res += conf.getEbookOutputDir().list().length;
        }
        if(conf.getAudioOutputDir().list() != null) {
            res += conf.getAudioOutputDir().list().length;
        }
        return res;
    }
}
