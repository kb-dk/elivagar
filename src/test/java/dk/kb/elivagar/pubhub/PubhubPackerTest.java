package dk.kb.elivagar.pubhub;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jaccept.structure.ExtendedTestCase;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.characterization.CharacterizationHandler;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.testutils.TestConfigurations;
import dk.kb.elivagar.testutils.TestFileUtils;
import dk.pubhub.service.ArrayOfImage;
import dk.pubhub.service.Book;
import dk.pubhub.service.BookTypeEnum;
import dk.pubhub.service.Image;

public class PubhubPackerTest extends ExtendedTestCase {
    public static final String PDF_SUFFIX = ".pdf";
    public static final String EPUB_SUFFIX = ".epub";
    public static final String MP3_SUFFIX = ".mp3";

    Configuration conf;
    
    @BeforeClass
    public void setup() {
        TestFileUtils.setup();
        conf = TestConfigurations.getConfigurationForTest();
    }
    
    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }
    
    @Test
    public void testGetMarshallerForClass() throws JAXBException {
        addDescription("Test the getMarshallerForClass method");
        String serviceNamespace = "test-" + UUID.randomUUID().toString();
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        HttpClient httpClient = mock(HttpClient.class);
        PubhubPacker packer = new PubhubPacker(conf, serviceNamespace, characterizer, httpClient);
        
        Assert.assertTrue(packer.marshallers.isEmpty());
        
        Marshaller marshaller = packer.getMarshallerForClass(Book.class);
        
        Assert.assertNotNull(marshaller);
        Assert.assertFalse(packer.marshallers.isEmpty());
        Assert.assertEquals(packer.marshallers.size(), 1);

        Marshaller marshaller2 = packer.getMarshallerForClass(Book.class);
        
        Assert.assertEquals(packer.marshallers.size(), 1);
        Assert.assertEquals(marshaller, marshaller2);
        
        verifyZeroInteractions(characterizer);
        verifyZeroInteractions(httpClient);
    }
    
    @Test
    public void testPackBook() throws Exception {
        addDescription("Test the packBook method");
        String serviceNamespace = "test-" + UUID.randomUUID().toString();
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        HttpClient httpClient = mock(HttpClient.class);
        PubhubPacker packer = new PubhubPacker(conf, serviceNamespace, characterizer, httpClient);
        
        String id = UUID.randomUUID().toString();
        Book book = new Book();
        book.setBookId(id);
        book.setBookType(BookTypeEnum.EBOG);
        
        String type = "thumbnail";
        String extension = ".tiff";
        String imageUrl = "http://127.0.0.1/image" + extension;
        Image image = new Image();
        image.setValue(imageUrl);
        image.setType(type);
        
        ArrayOfImage images = new ArrayOfImage();
        images.getImage().add(image);
        book.setImages(images);

        File bookDir = new File(conf.getEbookOutputDir(), id);
        File metadataFile = new File(bookDir, id + Constants.PUBHUB_METADATA_SUFFIX);
        File imageFile = new File(bookDir, id + "_" + type + extension);
        
        Assert.assertFalse(bookDir.exists());
        Assert.assertFalse(metadataFile.exists());
        Assert.assertFalse(imageFile.exists());

        packer.packBook(book);

        Assert.assertTrue(bookDir.exists());
        Assert.assertTrue(bookDir.isDirectory());
        Assert.assertTrue(metadataFile.exists(), metadataFile.getAbsolutePath());
        Assert.assertTrue(metadataFile.isFile());
        Assert.assertTrue(imageFile.exists());
        Assert.assertTrue(imageFile.isFile());

        verifyZeroInteractions(characterizer);
        
        verify(httpClient).retrieveUrlContent(eq(imageUrl), any(OutputStream.class));
        verifyNoMoreInteractions(httpClient);
    }

    @Test
    public void testPackBookImageFailure() throws Exception {
        addDescription("Test the packBook method, when it fails to retrieve an image");
        String serviceNamespace = "test-" + UUID.randomUUID().toString();
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        HttpClient httpClient = mock(HttpClient.class);
        PubhubPacker packer = new PubhubPacker(conf, serviceNamespace, characterizer, httpClient);
        
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                throw new IOException("THIS TEST MUST FAIL");
            }
            
        }).when(httpClient).retrieveUrlContent(anyString(), any(OutputStream.class));
        
        String id = UUID.randomUUID().toString();
        Book book = new Book();
        book.setBookId(id);
        book.setBookType(BookTypeEnum.EBOG);
        
        String type = "thumbnail";
        String extension = ".tiff";
        String imageUrl = "http://127.0.0.1/image" + extension;
        Image image = new Image();
        image.setValue(imageUrl);
        image.setType(type);
        
        ArrayOfImage images = new ArrayOfImage();
        images.getImage().add(image);
        book.setImages(images);

        File bookDir = new File(conf.getEbookOutputDir(), id);
        File metadataFile = new File(bookDir, id + Constants.PUBHUB_METADATA_SUFFIX);
        File imageFile = new File(bookDir, id + "_" + type + extension);
        
        Assert.assertFalse(bookDir.exists());
        Assert.assertFalse(metadataFile.exists());
        Assert.assertFalse(imageFile.exists());

        packer.packBook(book);

        Assert.assertTrue(bookDir.exists());
        Assert.assertTrue(bookDir.isDirectory());
        Assert.assertTrue(metadataFile.exists());
        Assert.assertTrue(metadataFile.isFile());
        Assert.assertTrue(imageFile.exists());
        Assert.assertTrue(imageFile.isFile());

        verifyZeroInteractions(characterizer);
        
        verify(httpClient).retrieveUrlContent(eq(imageUrl), any(OutputStream.class));
        verifyNoMoreInteractions(httpClient);
    }
    
    @Test
    public void testPackFileForEbook() throws Exception {
        addDescription("Test the packFileForEbook method");
        String serviceNamespace = "test-" + UUID.randomUUID().toString();
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        HttpClient httpClient = mock(HttpClient.class);
        PubhubPacker packer = new PubhubPacker(conf, serviceNamespace, characterizer, httpClient);
        
        String id = UUID.randomUUID().toString();
        String extension = ".pdf";
        File bookFile = new File(TestFileUtils.getTempDir(), id + extension);
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        File linkFile = new File(conf.getEbookOutputDir(), id + "/" + id + extension);
        Assert.assertFalse(linkFile.exists());
        packer.packFileForEbook(bookFile);
        Assert.assertTrue(linkFile.exists());
        
        
        verifyZeroInteractions(httpClient);
        
        verify(characterizer).characterize(any(File.class), any(File.class));
        verifyNoMoreInteractions(characterizer);
    }

    @Test
    public void testPackFileForEbookWrongExtension() throws Exception {
        addDescription("Test the packFileForEbook method, when the file has a wrong extension");
        String serviceNamespace = "test-" + UUID.randomUUID().toString();
        CharacterizationHandler characterize = mock(CharacterizationHandler.class);
        HttpClient httpClient = mock(HttpClient.class);
        PubhubPacker packer = new PubhubPacker(conf, serviceNamespace, characterize, httpClient);
        
        String id = UUID.randomUUID().toString();
        String extension = ".octetstream";
        File bookFile = new File(TestFileUtils.getTempDir(), id + extension);
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        File linkFile = new File(conf.getEbookOutputDir(), id + "/" + id + extension);
        Assert.assertFalse(linkFile.exists());
        packer.packFileForEbook(bookFile);
        Assert.assertFalse(linkFile.exists());
        
        verifyZeroInteractions(httpClient);
        verifyZeroInteractions(characterize);
    }
    
    @Test
    public void testPackFileForEbookWhenLinkAlreadyExists() throws Exception {
        addDescription("Test the packFileForEbook method, when the link already exists");
        String serviceNamespace = "test-" + UUID.randomUUID().toString();
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        HttpClient httpClient = mock(HttpClient.class);
        PubhubPacker packer = new PubhubPacker(conf, serviceNamespace, characterizer, httpClient);
        
        String id = UUID.randomUUID().toString();
        String extension = ".pdf";
        File bookFile = new File(TestFileUtils.getTempDir(), id + extension);
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        
        File linkFile = new File(conf.getEbookOutputDir(), id + "/" + id + extension);
        TestFileUtils.createEmptyDirectory(linkFile.getParent());
        TestFileUtils.createFile(linkFile, UUID.randomUUID().toString());
        
        Assert.assertTrue(linkFile.exists());
        packer.packFileForEbook(bookFile);
        Assert.assertTrue(linkFile.exists());        
        
        verifyZeroInteractions(httpClient);
        
        verify(characterizer).characterize(any(File.class), any(File.class));
        verifyNoMoreInteractions(characterizer);
    }
    
    @Test
    public void testPackFileForAudioBook() throws Exception {
        addDescription("Test the packFileForAudio method");
        String serviceNamespace = "test-" + UUID.randomUUID().toString();
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        HttpClient httpClient = mock(HttpClient.class);
        PubhubPacker packer = new PubhubPacker(conf, serviceNamespace, characterizer, httpClient);
        
        String id = UUID.randomUUID().toString();
        String extension = MP3_SUFFIX;
        File bookFile = new File(TestFileUtils.getTempDir(), id + extension);
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        File linkFile = new File(conf.getAudioOutputDir(), id + "/" + id + extension);
        Assert.assertFalse(linkFile.exists());
        packer.packFileForAudio(bookFile);
        Assert.assertTrue(linkFile.exists());
        
        
        verifyZeroInteractions(httpClient);
        
        verify(characterizer).characterize(any(File.class), any(File.class));
        verifyNoMoreInteractions(characterizer);
    }

    @Test
    public void testPackFileForAudioWrongExtension() throws Exception {
        addDescription("Test the packFileForEbook method, when the file has a wrong extension");
        String serviceNamespace = "test-" + UUID.randomUUID().toString();
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        HttpClient httpClient = mock(HttpClient.class);
        PubhubPacker packer = new PubhubPacker(conf, serviceNamespace, characterizer, httpClient);
        
        String id = UUID.randomUUID().toString();
        String extension = ".octetstream";
        File bookFile = new File(TestFileUtils.getTempDir(), id + extension);
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        File linkFile = new File(conf.getAudioOutputDir(), id + "/" + id + extension);
        Assert.assertFalse(linkFile.exists());
        packer.packFileForAudio(bookFile);
        Assert.assertFalse(linkFile.exists());
        
        verifyZeroInteractions(httpClient);
        verifyZeroInteractions(characterizer);
    }
    
    @Test
    public void testPackFileForAudioWhenLinkAlreadyExists() throws Exception {
        addDescription("Test the packFileForEbook method, when the link already exists");
        String serviceNamespace = "test-" + UUID.randomUUID().toString();
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        HttpClient httpClient = mock(HttpClient.class);
        PubhubPacker packer = new PubhubPacker(conf, serviceNamespace, characterizer, httpClient);
        
        String id = UUID.randomUUID().toString();
        String extension = MP3_SUFFIX;
        File bookFile = new File(TestFileUtils.getTempDir(), id + extension);
        TestFileUtils.createFile(bookFile, UUID.randomUUID().toString());
        
        File linkFile = new File(conf.getAudioOutputDir(), id + "/" + id + extension);
        TestFileUtils.createEmptyDirectory(linkFile.getParent());
        TestFileUtils.createFile(linkFile, UUID.randomUUID().toString());
        
        Assert.assertTrue(linkFile.exists());
        packer.packFileForAudio(bookFile);
        Assert.assertTrue(linkFile.exists());        
        
        verifyZeroInteractions(httpClient);
        
        verify(characterizer).characterize(any(File.class), any(File.class));
        verifyNoMoreInteractions(characterizer);
    }
    
    @Test
    public void testGetBookDirForEbog() throws Exception {
        addDescription("Test the runCharacterizationIfNeeded method, when the output file is newer than the input file.");
        String serviceNamespace = "test-" + UUID.randomUUID().toString();
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        HttpClient httpClient = mock(HttpClient.class);
        PubhubPacker packer = new PubhubPacker(conf, serviceNamespace, characterizer, httpClient);

        File dir = packer.getBookDir(UUID.randomUUID().toString(), BookTypeEnum.EBOG);
        Assert.assertEquals(dir.getParent(), conf.getEbookOutputDir().getAbsolutePath());
        
        verifyZeroInteractions(characterizer);
        verifyZeroInteractions(httpClient);
    }
    
    @Test
    public void testGetBookDirForAudioBook() throws Exception {
        addDescription("Test the runCharacterizationIfNeeded method, when the output file is newer than the input file.");
        String serviceNamespace = "test-" + UUID.randomUUID().toString();
        CharacterizationHandler characterizer = mock(CharacterizationHandler.class);
        HttpClient httpClient = mock(HttpClient.class);
        PubhubPacker packer = new PubhubPacker(conf, serviceNamespace, characterizer, httpClient);

        File dir = packer.getBookDir(UUID.randomUUID().toString(), BookTypeEnum.LYDBOG);
        Assert.assertEquals(dir.getParent(), conf.getAudioOutputDir().getAbsolutePath());
        
        verifyZeroInteractions(characterizer);
        verifyZeroInteractions(httpClient);
    }
}
