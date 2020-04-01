package dk.kb.elivagar.testutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.SkipException;

import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.utils.FileUtils;
import dk.kb.elivagar.utils.StreamUtils;

/**
 * To test against a proper system, you need the two files: pubhub-license.txt in the root of the project folder.
 * 
 * Test pubhub-license.txt must have a single line with the given license.
 */
public class TestConfigurations {

    public static final String licenseFilePath = "pubhub-license.txt";

    protected static Map<String, Object> getTransferMap() throws Exception {
        
        Map<String, Object> res = new HashMap<String, Object>();
        res.put(Configuration.CONF_TRANSFER_EBOOK_INGEST_PATH, FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/ebook/ingest").getAbsolutePath());
        res.put(Configuration.CONF_TRANSFER_EBOOK_UPDATE_CONTENT_PATH, FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/ebook/content").getAbsolutePath());
        res.put(Configuration.CONF_TRANSFER_EBOOK_UPDATE_METADATA_PATH, FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/ebook/metadata").getAbsolutePath());
        res.put(Configuration.CONF_TRANSFER_AUDIO_INGEST_PATH, FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/audio/ingest").getAbsolutePath());
        res.put(Configuration.CONF_TRANSFER_AUDIO_UPDATE_CONTENT_PATH, FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/audio/content").getAbsolutePath());
        res.put(Configuration.CONF_TRANSFER_AUDIO_UPDATE_METADATA_PATH, FileUtils.createDirectory(TestFileUtils.getTempDir().getAbsolutePath() + "/audio/metadata").getAbsolutePath());
        res.put(Configuration.CONF_TRANSFER_RETAIN_CREATE_DATE, new Integer(3600000));
        res.put(Configuration.CONF_TRANSFER_RETAIN_MODIFY_DATE, new Integer(60000));
        res.put(Configuration.CONF_TRANSFER_RETAIN_PUBLICATION_DATE, new Integer(0));
        res.put(Configuration.CONF_TRANSFER_REQUIRED_FORMATS, Arrays.asList("fits.xml", "mods.xml"));

        return res;
    }

    public static Configuration getConfigurationForTest(){
        File passwordFile = new File(licenseFilePath);
        if(!passwordFile.isFile()) {
            throw new SkipException("No license file is found at '" + licenseFilePath + ".");
        }
        try {
            String license = TestFileUtils.readFile(passwordFile);

            File baseDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath());
            File baseBookMetadataDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/books_metadata");
            File baseBookFileDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/books_files");
            File baseAudioMetadataDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/audio_metadata");
            File baseAudioFileDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/audio_files");
            File statisticsDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/statistic");

            Map<String, Object> confMap = new HashMap<String, Object>();
            confMap.put(Configuration.CONF_EBOOK_OUTPUT_DIR, baseBookMetadataDir.getAbsolutePath());
            confMap.put(Configuration.CONF_AUDIO_OUTPUT_DIR, baseAudioMetadataDir.getAbsolutePath());
            confMap.put(Configuration.CONF_EBOOK_FILE_DIR, baseBookFileDir.getAbsolutePath());
            confMap.put(Configuration.CONF_AUDIO_FILE_DIR, baseAudioFileDir.getAbsolutePath());
            confMap.put(Configuration.CONF_LICENSE_KEY, license);
            confMap.put(Configuration.CONF_AUDIO_FORMATS, Arrays.asList("mp3"));
            confMap.put(Configuration.CONF_EBOOK_FORMATS, Arrays.asList("pdf", "epub"));
            confMap.put(Configuration.CONF_XSLT_DIR, TestFileUtils.getTempDir().getAbsolutePath());
            confMap.put(Configuration.CONF_STATISTIC_DIR, statisticsDir.getAbsolutePath());
            confMap.put(Configuration.CONF_ALMA_SRU_SEARCH, "https://kbdk-kgl.alma.exlibrisgroup.com/view/sru/45KBDK_KGL?version=1.2&operation=searchRetrieve&");
            
            confMap.put(Configuration.CONF_TRANSFER_ROOT, getTransferMap());

            return new Configuration(confMap);
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }
    

    public static Configuration getConfigurationForTestWithoutTransfer(){
        File passwordFile = new File(licenseFilePath);
        if(!passwordFile.isFile()) {
            throw new SkipException("No license file is found at '" + licenseFilePath + ".");
        }
        try {
            String license = TestFileUtils.readFile(passwordFile);

            File baseDir = TestFileUtils.createEmptyDirectory(TestFileUtils.getTempDir().getAbsolutePath());
            File baseBookMetadataDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/books_metadata");
            File baseBookFileDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/books_files");
            File baseAudioMetadataDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/audio_metadata");
            File baseAudioFileDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/audio_files");
            File statisticsDir = TestFileUtils.createEmptyDirectory(baseDir.getAbsolutePath() + "/statistic");

            Map<String, Object> confMap = new HashMap<String, Object>();
            confMap.put(Configuration.CONF_EBOOK_OUTPUT_DIR, baseBookMetadataDir.getAbsolutePath());
            confMap.put(Configuration.CONF_AUDIO_OUTPUT_DIR, baseAudioMetadataDir.getAbsolutePath());
            confMap.put(Configuration.CONF_EBOOK_FILE_DIR, baseBookFileDir.getAbsolutePath());
            confMap.put(Configuration.CONF_AUDIO_FILE_DIR, baseAudioFileDir.getAbsolutePath());
            confMap.put(Configuration.CONF_LICENSE_KEY, license);
            confMap.put(Configuration.CONF_AUDIO_FORMATS, Arrays.asList("mp3"));
            confMap.put(Configuration.CONF_EBOOK_FORMATS, Arrays.asList("pdf", "epub"));
            confMap.put(Configuration.CONF_XSLT_DIR, TestFileUtils.getTempDir().getAbsolutePath());
            confMap.put(Configuration.CONF_STATISTIC_DIR, statisticsDir.getAbsolutePath());
            confMap.put(Configuration.CONF_ALMA_SRU_SEARCH, "https://kbdk-kgl.alma.exlibrisgroup.com/view/sru/45KBDK_KGL?version=1.2&operation=searchRetrieve&");

            return new Configuration(confMap);
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }
}
