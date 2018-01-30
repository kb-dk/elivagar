package dk.kb.elivagar.metadata;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.log4j.lf5.util.StreamUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.config.AlephConfiguration;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.metadata.MetadataTransformer.TransformationType;
import dk.kb.elivagar.testutils.TestConfigurations;
import dk.kb.elivagar.testutils.TestFileUtils;

public class AlephPackerTest extends ExtendedTestCase {

    Configuration configuration;

    @BeforeClass
    public void setup() throws Exception {
        TestFileUtils.setup();
        configuration = TestConfigurations.getConfigurationForTest();
    }
    
    @Test
    public void testPackAlephMetadataForBooksWhenEmpty() throws Exception {
        addDescription("Test the packAlephMetadataForBooks method when the folders for both book types are empty.");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        
        AlephPacker packer = new AlephPacker(configuration, retriever, transformer);
        
        packer.packAlephMetadataForBooks();

        verifyZeroInteractions(retriever);
        verifyNoMoreInteractions(transformer);
    }
    
    @Test
    public void testPackAlephMetadataForBooksWhenTheyAreTheSame() throws Exception {
        addDescription("Test the packAlephMetadataForBooks method when both book types have the same folder.");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        Configuration conf = mock(Configuration.class);
        
        AlephPacker packer = new AlephPacker(conf, retriever, transformer);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + id);        
        
        when(conf.getAudioOutputDir()).thenReturn(dir);
        when(conf.getEbookOutputDir()).thenReturn(dir);
        
        packer.packAlephMetadataForBooks();
        
        verify(conf).getAudioOutputDir();
        verify(conf, times(2)).getEbookOutputDir();
        verifyNoMoreInteractions(conf);

        verifyZeroInteractions(retriever);
        verifyNoMoreInteractions(transformer);
    }
    
    @Test
    public void testTraverseBooksInFolder() throws Exception {
        addDescription("Test the traverseBooksInFolder method when both.");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        
        AlephPacker packer = new AlephPacker(configuration, retriever, transformer);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/" + id);
        File modsFile = new File(dir, dir.getName() + Constants.MODS_METADATA_SUFFIX);
        TestFileUtils.createFile(modsFile, UUID.randomUUID().toString());
        
        packer.traverseBooksInFolder(dir);

        verifyZeroInteractions(retriever);
        verifyNoMoreInteractions(transformer);
    }
    
    @Test
    public void testTraverseBooksInFolderWhenGivenAFile() throws Exception {
        addDescription("Test the traverseBooksInFolder method when an file is given as argument.");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        
        AlephPacker packer = new AlephPacker(configuration, retriever, transformer);
        
        File sillyFile = TestFileUtils.createTempFile(UUID.randomUUID().toString());
        
        packer.traverseBooksInFolder(sillyFile);

        verifyZeroInteractions(retriever);
        verifyNoMoreInteractions(transformer);
    }
    
    @Test
    public void testPackageMetadataForBook() throws Exception {
        addDescription("Test the packageMetadataForBook method for the success scenario.");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        
        AlephPacker packer = new AlephPacker(configuration, retriever, transformer);
        
        String expectedIsbn = "9788711436981";
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        
        TestFileUtils.copyFile(new File("src/test/resources/metadata/pubhub_metadata.xml"), new File(dir, dir.getName() + ".xml"));
        
        File modsFile = new File(dir, dir.getName() + Constants.MODS_METADATA_SUFFIX);
        Assert.assertFalse(modsFile.exists());
        
        packer.packageMetadataForBook(dir);

        Assert.assertTrue(modsFile.exists());
        
        verify(retriever).retrieveMetadataForISBN(eq(expectedIsbn), any(OutputStream.class));
        verifyNoMoreInteractions(retriever);
        
        verify(transformer).transformMetadata(any(InputStream.class), any(OutputStream.class), eq(TransformationType.ALEPH_TO_MARC21));
        verify(transformer).transformMetadata(any(InputStream.class), any(OutputStream.class), eq(TransformationType.MARC21_TO_MODS));
        verifyNoMoreInteractions(transformer);
    }
    
    @Test
    public void testPackageMetadataForBookModsAlreadyExists() throws Exception {
        addDescription("Test the packageMetadataForBook method for the scenario, when the MODS record already exists.");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        
        AlephPacker packer = new AlephPacker(configuration, retriever, transformer);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        
        File modsFile = new File(dir, dir.getName() + Constants.MODS_METADATA_SUFFIX);
        TestFileUtils.createFile(modsFile, UUID.randomUUID().toString());
        Assert.assertTrue(modsFile.exists());
        
        packer.packageMetadataForBook(dir);

        verifyZeroInteractions(retriever);
        verifyZeroInteractions(transformer);
    }
    
    @Test
    public void testPackageMetadataForBookNoIsbn() throws Exception {
        addDescription("Test the packageMetadataForBook method for the scenario, when no ISBN can be found.");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        
        AlephPacker packer = new AlephPacker(configuration, retriever, transformer);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        
        packer.packageMetadataForBook(dir);

        verifyZeroInteractions(retriever);
        verifyZeroInteractions(transformer);
    }
    
    @Test
    public void testPackageMetadataForBookException() throws Exception {
        addDescription("Test the packageMetadataForBook method for the scenario, when it throws an exception.");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        
        AlephPacker packer = new AlephPacker(configuration, retriever, transformer);
        
        String expectedIsbn = "9788711436981";
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        
        TestFileUtils.copyFile(new File("src/test/resources/metadata/pubhub_metadata.xml"), new File(dir, dir.getName() + ".xml"));

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                throw new IOException("FAIL TO RETRIEVE FROM ALEPH!!!");
            }
        }).when(retriever).retrieveMetadataForISBN(eq(expectedIsbn), any(OutputStream.class));
        
        File modsFile = new File(dir, dir.getName() + Constants.MODS_METADATA_SUFFIX);
        Assert.assertFalse(modsFile.exists());
        
        packer.packageMetadataForBook(dir);

        Assert.assertFalse(modsFile.exists());
        
        verify(retriever).retrieveMetadataForISBN(eq(expectedIsbn), any(OutputStream.class));
        verifyNoMoreInteractions(retriever);
        
        verifyZeroInteractions(transformer);
    }
    
    @Test
    public void testGetIsbnSuccess() throws Exception {
        addDescription("Test the getIsbn method, in the success case when it finds a ISBN number.");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        
        String expectedIsbn = "9788711436981";
        AlephPacker packer = new AlephPacker(configuration, retriever, transformer);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        
        TestFileUtils.copyFile(new File("src/test/resources/metadata/pubhub_metadata.xml"), new File(dir, dir.getName() + ".xml"));
        
        String isbn = packer.getIsbn(dir);
        
        Assert.assertEquals(isbn, expectedIsbn);
        
        verifyZeroInteractions(retriever);
        verifyZeroInteractions(transformer);
    }
    
    @Test
    public void testGetIsbnBadIdentifierType() throws Exception {
        addDescription("Test the getIsbn method, when the identifier number is not an ISBN.");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        
        AlephPacker packer = new AlephPacker(configuration, retriever, transformer);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        
        TestFileUtils.copyFile(new File("src/test/resources/metadata/pubhub_metadata_bad_type.xml"), new File(dir, dir.getName() + ".xml"));
        
        String isbn = packer.getIsbn(dir);
        
        Assert.assertNull(isbn);
        
        verifyZeroInteractions(retriever);
        verifyZeroInteractions(transformer);
    }

    @Test
    public void testGetIsbnBadFileFormat() throws Exception {
        addDescription("Test the getIsbn method, when the pubhub metadata file does not have the right format.");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        
        AlephPacker packer = new AlephPacker(configuration, retriever, transformer);
        
        String id = UUID.randomUUID().toString();
        File dir = TestFileUtils.createEmptyDirectory(new File(TestFileUtils.getTempDir(), id).getAbsolutePath());
        
        TestFileUtils.createFile(new File(dir, dir.getName() + ".xml"), UUID.randomUUID().toString());
        
        String isbn = packer.getIsbn(dir);
        
        Assert.assertNull(isbn);
        
        verifyZeroInteractions(retriever);
        verifyZeroInteractions(transformer);
    }

    @Test
    public void testGetIsbnNoFile() throws Exception {
        addDescription("Test the getIsbn method, when no file exists.");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        
        AlephPacker packer = new AlephPacker(configuration, retriever, transformer);
        
        String id = UUID.randomUUID().toString();
        
        String isbn = packer.getIsbn(new File(TestFileUtils.getTempDir(), id));
        
        Assert.assertNull(isbn);
        
        verifyZeroInteractions(retriever);
        verifyZeroInteractions(transformer);
    }

    @Test
    public void testGetAlephMetadata() throws Exception {
        addDescription("Test the getAlephMetadata method");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        
        AlephPacker packer = new AlephPacker(configuration, retriever, transformer);
        String isbn = "9788711436981";

        File marcOutput = packer.getAlephMetadata(isbn);
        
        Assert.assertEquals(marcOutput.getParentFile().getAbsolutePath(), configuration.getAlephConfiguration().getTempDir().getAbsolutePath());
        Assert.assertTrue(marcOutput.getName().startsWith(isbn));
        Assert.assertTrue(marcOutput.getName().endsWith(Constants.ALEPH_METADATA_SUFFIX));
        
        verify(retriever).retrieveMetadataForISBN(eq(isbn), any(OutputStream.class));
        verifyNoMoreInteractions(retriever);
        
        verifyZeroInteractions(transformer);
    }


    @Test
    public void testTransformAlephMetadataToMarc() throws Exception {
        addDescription("Test the transformAlephMetadataToMarc method");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        
        AlephPacker packer = new AlephPacker(configuration, retriever, transformer);
        String isbn = "9788711436981";

        File alephMetadata = TestFileUtils.createTempFile(UUID.randomUUID().toString());
        File marcOutput = packer.transformAlephMetadataToMarc(alephMetadata, isbn);
        
        Assert.assertEquals(marcOutput.getParentFile().getAbsolutePath(), configuration.getAlephConfiguration().getTempDir().getAbsolutePath());
        Assert.assertTrue(marcOutput.getName().startsWith(isbn));
        Assert.assertTrue(marcOutput.getName().endsWith(Constants.MARC_METADATA_SUFFIX));
        
        verifyZeroInteractions(retriever);
        
        verify(transformer).transformMetadata(any(InputStream.class), any(OutputStream.class), eq(MetadataTransformer.TransformationType.ALEPH_TO_MARC21));
        verifyNoMoreInteractions(transformer);
    }

    @Test
    public void testTransformMarcToMods() throws Exception {
        addDescription("Test the transformMarcToMods method");
        AlephMetadataRetriever retriever = mock(AlephMetadataRetriever.class);
        MetadataTransformer transformer = mock(MetadataTransformer.class);
        
        AlephPacker packer = new AlephPacker(configuration, retriever, transformer);
        
        File marcMetadata = TestFileUtils.createTempFile(UUID.randomUUID().toString());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        packer.transformMarcToMods(marcMetadata, output);
        
        verifyZeroInteractions(retriever);
        
        verify(transformer).transformMetadata(any(InputStream.class), any(OutputStream.class), eq(MetadataTransformer.TransformationType.MARC21_TO_MODS));
        verifyNoMoreInteractions(transformer);
    }
}
