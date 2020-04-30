package dk.kb.elivagar.metadata;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.testutils.TestConfigurations;
import dk.kb.elivagar.testutils.TestFileUtils;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class AlmaPackerTest extends ExtendedTestCase {

    Configuration configuration;

    @BeforeClass
    public void setup() throws Exception {
        TestFileUtils.setup();
        configuration = TestConfigurations.getConfigurationForTest();
    }
    
    @AfterClass
    public void tearDown() throws Exception {
        TestFileUtils.tearDown();
    }
    
    @Test
    public void testPackAlmaMetadataForBooksWhenEmpty() throws Exception {
        addDescription("Test the packAlmaMetadataForBooks method when the folders for both book types are empty.");
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);

        AlmaPacker packer = new AlmaPacker(configuration, retriever);
        
        packer.packAlmaMetadataForBooks();

        verifyZeroInteractions(retriever);
    }
    
    @Test
    public void testPackAlmaMetadataForBooksWhenTheyAreTheSame() throws Exception {
        addDescription("Test the packAlmaMetadataForBooks method when both book types have the same folder.");
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);
        Configuration conf = mock(Configuration.class);
        
        AlmaPacker packer = new AlmaPacker(conf, retriever);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + id);        
        
        when(conf.getAudioOutputDir()).thenReturn(dir);
        when(conf.getEbookOutputDir()).thenReturn(dir);
        
        packer.packAlmaMetadataForBooks();
        
        verify(conf).getAudioOutputDir();
        verify(conf, times(2)).getEbookOutputDir();
        verifyNoMoreInteractions(conf);

        verifyZeroInteractions(retriever);
    }
    
    @Test
    public void testTraverseBooksInFolder() throws Exception {
        addDescription("Test the traverseBooksInFolder method when both.");
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);

        AlmaPacker packer = new AlmaPacker(configuration, retriever);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + id);
        File modsFile = new File(dir, dir.getName() + Constants.MODS_METADATA_SUFFIX);
        TestFileUtils.createFile(modsFile, UUID.randomUUID().toString());
        
        packer.traverseBooksInFolder(dir);

        verifyZeroInteractions(retriever);
    }
    
    @Test
    public void testTraverseBooksInFolderWhenGivenAFile() throws Exception {
        addDescription("Test the traverseBooksInFolder method when an file is given as argument.");
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);

        AlmaPacker packer = new AlmaPacker(configuration, retriever);
        
        File sillyFile = TestFileUtils.createTempFile(UUID.randomUUID().toString());
        
        packer.traverseBooksInFolder(sillyFile);

        verifyZeroInteractions(retriever);
    }
    
    @Test
    public void testPackageMetadataForBook() throws Exception {
        addDescription("Test the packageMetadataForBook method for the success scenario.");
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);

        AlmaPacker packer = new AlmaPacker(configuration, retriever);
        
        String expectedIsbn = "9788711436981";
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        final File modsTestFile = new File("src/test/resources/metadata/mods.xml");
        Assert.assertTrue(modsTestFile.isFile());
        
        TestFileUtils.copyFile(new File("src/test/resources/metadata/pubhub_metadata.xml"), new File(dir, dir.getName() + Constants.PUBHUB_METADATA_SUFFIX));
        
        File modsFile = new File(dir, dir.getName() + Constants.MODS_METADATA_SUFFIX);
        Assert.assertFalse(modsFile.exists());

        packer.packageMetadataForBook(dir);

        Assert.assertTrue(modsFile.exists());
        
        verify(retriever).retrieveMetadataForISBN(eq(expectedIsbn), any(OutputStream.class));
        verifyNoMoreInteractions(retriever);
        
    }
    
    @Test
    public void testPackageMetadataForBookModsAlreadyExists() throws Exception {
        addDescription("Test the packageMetadataForBook method for the scenario, when the MODS record already exists.");
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);

        AlmaPacker packer = new AlmaPacker(configuration, retriever);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        
        File modsFile = new File(dir, dir.getName() + Constants.MODS_METADATA_SUFFIX);
        TestFileUtils.createFile(modsFile, UUID.randomUUID().toString());
        Assert.assertTrue(modsFile.exists());
        
        packer.packageMetadataForBook(dir);

        verifyZeroInteractions(retriever);
    }
    
    @Test
    public void testPackageMetadataForBookNoIsbn() throws Exception {
        addDescription("Test the packageMetadataForBook method for the scenario, when no ISBN can be found.");
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);

        AlmaPacker packer = new AlmaPacker(configuration, retriever);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        
        packer.packageMetadataForBook(dir);

        verifyZeroInteractions(retriever);
    }

    @Test(enabled = false)
    public void testPackageMetadataForBookException() throws Exception {
        addDescription("Test the packageMetadataForBook method for the scenario, when it throws an exception.");
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);

        AlmaPacker packer = new AlmaPacker(configuration, retriever);
        
        String expectedIsbn = "9788711436981";
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        
        TestFileUtils.copyFile(new File("src/test/resources/metadata/pubhub_metadata.xml"), new File(dir, dir.getName() + Constants.PUBHUB_METADATA_SUFFIX));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                throw new IOException("FAIL TO RETRIEVE FROM ALMA!!!");
            }
        }).when(retriever).retrieveMetadataForISBN(eq(expectedIsbn), any(OutputStream.class));
        
        File modsFile = new File(dir, dir.getName() + Constants.MODS_METADATA_SUFFIX);
        Assert.assertFalse(modsFile.exists());
        
        packer.packageMetadataForBook(dir);

        Assert.assertFalse(modsFile.exists());
        
        verify(retriever).retrieveMetadataForISBN(eq(expectedIsbn), any(OutputStream.class));
        verifyNoMoreInteractions(retriever);
        
    }
    
    @Test
    public void testGetIsbnSuccess() throws Exception {
        addDescription("Test the getIsbn method, in the success case when it finds a ISBN number.");
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);

        String expectedIsbn = "9788711436981";
        AlmaPacker packer = new AlmaPacker(configuration, retriever);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        
        TestFileUtils.copyFile(new File("src/test/resources/metadata/pubhub_metadata.xml"), new File(dir, dir.getName() + Constants.PUBHUB_METADATA_SUFFIX));
        
        String isbn = packer.getIsbn(dir);
        
        Assert.assertEquals(isbn, expectedIsbn);
        
        verifyZeroInteractions(retriever);
    }
    
    @Test
    public void testGetIsbnGtinSuccess() throws Exception {
        addDescription("Test the getIsbn method, in the success case when it finds a GTIN number.");
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);

        String expectedIsbn = "9788711436981";
        AlmaPacker packer = new AlmaPacker(configuration, retriever);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        
        TestFileUtils.copyFile(new File("src/test/resources/metadata/pubhub_metadata_gtin.xml"), new File(dir, dir.getName() + Constants.PUBHUB_METADATA_SUFFIX));
        
        String isbn = packer.getIsbn(dir);
        
        Assert.assertEquals(isbn, expectedIsbn);
        
        verifyZeroInteractions(retriever);
    }
    
    @Test
    public void testGetIsbnBadIdentifierType() throws Exception {
        addDescription("Test the getIsbn method, when the identifier number is not an ISBN.");
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);

        AlmaPacker packer = new AlmaPacker(configuration, retriever);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        
        TestFileUtils.copyFile(new File("src/test/resources/metadata/pubhub_metadata_bad_type.xml"), new File(dir, dir.getName() + Constants.PUBHUB_METADATA_SUFFIX));
        
        String isbn = packer.getIsbn(dir);
        
        Assert.assertNull(isbn);
        
        verifyZeroInteractions(retriever);
    }

    @Test
    public void testGetIsbnBadFileFormat() throws Exception {
        addDescription("Test the getIsbn method, when the pubhub metadata file does not have the right format.");
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);
        
        AlmaPacker packer = new AlmaPacker(configuration, retriever);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        
        TestFileUtils.createFile(new File(dir, dir.getName() + ".xml"), UUID.randomUUID().toString());
        
        String isbn = packer.getIsbn(dir);
        
        Assert.assertNull(isbn);
        
        verifyZeroInteractions(retriever);
    }

    @Test
    public void testGetIsbnNoFile() throws Exception {
        addDescription("Test the getIsbn method, when no file exists.");
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);
        
        AlmaPacker packer = new AlmaPacker(configuration, retriever);
        
        String id = UUID.randomUUID().toString();
        
        String isbn = packer.getIsbn(new File(TestFileUtils.getTempDir(), id));
        
        Assert.assertNull(isbn);
        
        verifyZeroInteractions(retriever);
    }

    @Test
    public void testGetAlmaMetadata() throws Exception {
        addDescription("Test the getAlmaMetadata method");
        AlmaMetadataRetriever retriever = mock(AlmaMetadataRetriever.class);
        
        AlmaPacker packer = new AlmaPacker(configuration, retriever);
        String isbn = "9788711436981";

        File modsFile = new File(TestFileUtils.getTempDir(), isbn + ".mods");

        packer.getAlmaMetadata(isbn, modsFile);
        
        verify(retriever).retrieveMetadataForISBN(eq(isbn), any(OutputStream.class));
        verifyNoMoreInteractions(retriever);
    }
}
