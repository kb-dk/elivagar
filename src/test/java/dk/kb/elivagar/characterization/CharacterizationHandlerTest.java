package dk.kb.elivagar.characterization;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
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
import dk.kb.elivagar.utils.FileUtils;

public class CharacterizationHandlerTest extends ExtendedTestCase {
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
    public void testRunFitsIfNeededWhenNoScript() throws IOException {
        addDescription("Test the runFitsIfNeeded method, when no script is given");
        FitsCharacterizer fitsCharacterizer = null;
        EpubCharacterizer epubCharacterizer = mock(EpubCharacterizer.class);
        CharacterizationHandler characterizer = new CharacterizationHandler(fitsCharacterizer, epubCharacterizer);

        File dir = FileUtils.createDirectory(new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString()).getAbsolutePath());
        File inputFile = new File(dir, UUID.randomUUID().toString());
        File outputFile = new File(dir, inputFile.getName() + Constants.FITS_METADATA_SUFFIX);

        Assert.assertFalse(outputFile.exists());

        characterizer.runFitsIfNeeded(inputFile);
        
        Assert.assertFalse(outputFile.exists());
        verifyZeroInteractions(epubCharacterizer);
    }

    @Test
    public void testRunFitsIfNeededWhenNoOutputFile() throws IOException {
        addDescription("Test the runFitsIfNeeded method, when the output file does not exist");
        FitsCharacterizer fitsCharacterizer = mock(FitsCharacterizer.class);
        EpubCharacterizer epubCharacterizer = mock(EpubCharacterizer.class);
        CharacterizationHandler characterizer = new CharacterizationHandler(fitsCharacterizer, epubCharacterizer);

        File dir = FileUtils.createDirectory(new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString()).getAbsolutePath());
        File inputFile = new File(dir, UUID.randomUUID().toString());
        File outputFile = new File(dir, inputFile.getName() + Constants.FITS_METADATA_SUFFIX);

        Assert.assertFalse(outputFile.exists());

        characterizer.runFitsIfNeeded(inputFile);
        
        verify(fitsCharacterizer).execute(eq(inputFile), eq(outputFile));
        verifyNoMoreInteractions(fitsCharacterizer);
        verifyZeroInteractions(epubCharacterizer);
    }

    @Test
    public void testRunFitsIfNeededWhenOutputFileIsOlderThanInputFile() throws Exception {
        addDescription("Test the runFitsIfNeeded method, when the output file is older than the input file.");
        FitsCharacterizer fitsCharacterizer = mock(FitsCharacterizer.class);
        EpubCharacterizer epubCharacterizer = mock(EpubCharacterizer.class);
        CharacterizationHandler characterizer = new CharacterizationHandler(fitsCharacterizer, epubCharacterizer);

        File dir = FileUtils.createDirectory(new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString()).getAbsolutePath());
        File inputFile = new File(dir, UUID.randomUUID().toString());
        File outputFile = new File(dir, inputFile.getName() + Constants.FITS_METADATA_SUFFIX);
        TestFileUtils.createFile(inputFile, UUID.randomUUID().toString());
        TestFileUtils.createFile(outputFile, UUID.randomUUID().toString());

        outputFile.setLastModified(0);
        Assert.assertTrue(outputFile.exists());

        characterizer.runFitsIfNeeded(inputFile);
        
        Assert.assertTrue(outputFile.exists());
        verify(fitsCharacterizer).execute(eq(inputFile), eq(outputFile));
        verifyNoMoreInteractions(fitsCharacterizer);
        verifyZeroInteractions(epubCharacterizer);
    }

    @Test
    public void testRunFitsIfNeededWhenOutputFileIsNewerThanInputFile() throws Exception {
        addDescription("Test the runFitsIfNeeded method, when the output file is newer than the input file.");
        FitsCharacterizer fitsCharacterizer = mock(FitsCharacterizer.class);
        EpubCharacterizer epubCharacterizer = mock(EpubCharacterizer.class);
        CharacterizationHandler characterizer = new CharacterizationHandler(fitsCharacterizer, epubCharacterizer);

        File dir = FileUtils.createDirectory(new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString()).getAbsolutePath());
        File inputFile = new File(dir, UUID.randomUUID().toString());
        File outputFile = new File(dir, inputFile.getName() + Constants.FITS_METADATA_SUFFIX);
        TestFileUtils.createFile(inputFile, UUID.randomUUID().toString());
        TestFileUtils.createFile(outputFile, UUID.randomUUID().toString());

        inputFile.setLastModified(0);
        Assert.assertTrue(outputFile.exists());

        characterizer.runFitsIfNeeded(inputFile);
        
        Assert.assertTrue(outputFile.exists());
        verifyZeroInteractions(fitsCharacterizer);
        verifyZeroInteractions(epubCharacterizer);
    }
    

    @Test
    public void testRunEpubCheckIfNeededWhenBadExtension() throws IOException {
        addDescription("Test the runEpubCheckIfNeeded method, when no script is given");
        FitsCharacterizer fitsCharacterizer = mock(FitsCharacterizer.class);
        EpubCharacterizer epubCharacterizer = mock(EpubCharacterizer.class);
        CharacterizationHandler characterizer = new CharacterizationHandler(fitsCharacterizer, epubCharacterizer);

        File dir = FileUtils.createDirectory(new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString()).getAbsolutePath());
        File inputFile = new File(dir, UUID.randomUUID().toString());
        File outputFile = new File(dir, inputFile.getName() + Constants.EPUB_METADATA_SUFFIX);

        when(epubCharacterizer.hasRequiredExtension(eq(inputFile))).thenReturn(false);

        Assert.assertFalse(outputFile.exists());

        characterizer.runEpubCheckIfNeeded(inputFile);
        
        Assert.assertFalse(outputFile.exists());
        verify(epubCharacterizer).hasRequiredExtension(eq(inputFile));
        verifyNoMoreInteractions(epubCharacterizer);
        verifyZeroInteractions(fitsCharacterizer);
    }

    @Test
    public void testRunEpubCheckIfNeededWhenNoOutputFile() throws IOException {
        addDescription("Test the runEpubCheckIfNeeded method, when the output file does not exist");
        FitsCharacterizer fitsCharacterizer = mock(FitsCharacterizer.class);
        EpubCharacterizer epubCharacterizer = mock(EpubCharacterizer.class);
        CharacterizationHandler characterizer = new CharacterizationHandler(fitsCharacterizer, epubCharacterizer);

        File dir = FileUtils.createDirectory(new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString()).getAbsolutePath());
        File inputFile = new File(dir, UUID.randomUUID().toString() + Constants.EPUB_FILE_SUFFIX);
        File outputFile = new File(dir, inputFile.getName() + Constants.EPUB_METADATA_SUFFIX);

        when(epubCharacterizer.hasRequiredExtension(eq(inputFile))).thenReturn(true);
        
        Assert.assertFalse(outputFile.exists());

        characterizer.runEpubCheckIfNeeded(inputFile);
        
        verify(epubCharacterizer).hasRequiredExtension(eq(inputFile));
        verify(epubCharacterizer).characterize(eq(inputFile), eq(outputFile));
        verifyNoMoreInteractions(epubCharacterizer);
        verifyZeroInteractions(fitsCharacterizer);
    }

    @Test
    public void testRunEpubCheckIfNeededWhenOutputFileIsOlderThanInputFile() throws Exception {
        addDescription("Test the runEpubCheckIfNeeded method, when the output file is older than the input file.");
        FitsCharacterizer fitsCharacterizer = mock(FitsCharacterizer.class);
        EpubCharacterizer epubCharacterizer = mock(EpubCharacterizer.class);
        CharacterizationHandler characterizer = new CharacterizationHandler(fitsCharacterizer, epubCharacterizer);

        File dir = FileUtils.createDirectory(new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString()).getAbsolutePath());
        File inputFile = new File(dir, UUID.randomUUID().toString() + Constants.EPUB_FILE_SUFFIX);
        File outputFile = new File(dir, inputFile.getName() + Constants.EPUB_METADATA_SUFFIX);
        TestFileUtils.createFile(inputFile, UUID.randomUUID().toString());
        TestFileUtils.createFile(outputFile, UUID.randomUUID().toString());

        when(epubCharacterizer.hasRequiredExtension(eq(inputFile))).thenReturn(true);

        outputFile.setLastModified(0);
        Assert.assertTrue(outputFile.exists());

        characterizer.runEpubCheckIfNeeded(inputFile);
        
        Assert.assertTrue(outputFile.exists());
        
        verify(epubCharacterizer).hasRequiredExtension(eq(inputFile));
        verify(epubCharacterizer).characterize(eq(inputFile), eq(outputFile));
        verifyNoMoreInteractions(epubCharacterizer);
        verifyZeroInteractions(fitsCharacterizer);
    }

    @Test
    public void testRunEpubCheckIfNeededWhenOutputFileIsNewerThanInputFile() throws Exception {
        addDescription("Test the runEpubCheckIfNeeded method, when the output file is newer than the input file.");
        FitsCharacterizer fitsCharacterizer = mock(FitsCharacterizer.class);
        EpubCharacterizer epubCharacterizer = mock(EpubCharacterizer.class);
        CharacterizationHandler characterizer = new CharacterizationHandler(fitsCharacterizer, epubCharacterizer);

        File dir = FileUtils.createDirectory(new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString()).getAbsolutePath());
        File inputFile = new File(dir, UUID.randomUUID().toString() + Constants.EPUB_FILE_SUFFIX);
        File outputFile = new File(dir, inputFile.getName() + Constants.EPUB_METADATA_SUFFIX);
        TestFileUtils.createFile(inputFile, UUID.randomUUID().toString());
        TestFileUtils.createFile(outputFile, UUID.randomUUID().toString());

        when(epubCharacterizer.hasRequiredExtension(eq(inputFile))).thenReturn(true);

        inputFile.setLastModified(0);
        Assert.assertTrue(outputFile.exists());

        characterizer.runEpubCheckIfNeeded(inputFile);
        
        Assert.assertTrue(outputFile.exists());
        verify(epubCharacterizer).hasRequiredExtension(eq(inputFile));
        verifyNoMoreInteractions(epubCharacterizer);
        verifyZeroInteractions(fitsCharacterizer);
    }

    @Test
    public void testRunEpubCheckIfNeededWhenItThrowsAnError() throws Exception {
        addDescription("Test the runEpubCheckIfNeeded method, when the output file is newer than the input file.");
        FitsCharacterizer fitsCharacterizer = mock(FitsCharacterizer.class);
        EpubCharacterizer epubCharacterizer = mock(EpubCharacterizer.class);
        CharacterizationHandler characterizer = new CharacterizationHandler(fitsCharacterizer, epubCharacterizer);

        File dir = FileUtils.createDirectory(new File(TestFileUtils.getTempDir(), UUID.randomUUID().toString()).getAbsolutePath());
        File inputFile = new File(dir, UUID.randomUUID().toString() + Constants.EPUB_FILE_SUFFIX);
        File outputFile = new File(dir, inputFile.getName() + Constants.EPUB_METADATA_SUFFIX);
        TestFileUtils.createFile(inputFile, UUID.randomUUID().toString());
        TestFileUtils.createFile(outputFile, UUID.randomUUID().toString());
        
        when(epubCharacterizer.hasRequiredExtension(eq(inputFile))).thenReturn(true);

        doAnswer(new Answer<Void>() {

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                throw new IOException("TEST EXCEPTION");
            }
        }).when(epubCharacterizer).characterize(eq(inputFile), eq(outputFile));

        outputFile.setLastModified(0);
        Assert.assertTrue(outputFile.exists());

        characterizer.runEpubCheckIfNeeded(inputFile);
        
        Assert.assertTrue(outputFile.exists());
        verify(epubCharacterizer).hasRequiredExtension(eq(inputFile));
        verify(epubCharacterizer).characterize(eq(inputFile), eq(outputFile));
        verifyNoMoreInteractions(epubCharacterizer);
        verifyZeroInteractions(fitsCharacterizer);
    }
}
