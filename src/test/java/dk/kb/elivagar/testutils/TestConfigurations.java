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

import dk.kb.elivagar.config.AlephConfiguration;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.utils.StreamUtils;

/**
 * To test against a proper system, you need the two files: pubhub-license.txt and aleph-conf.txt
 * in the root of the project folder.
 * 
 * Test pubhub-license.txt must have a single line with the given license.
 * 
 * The aleph-conf.txt must be in the following format:
 * aleph-url: $TEST_ALEPH_URL
 * base: $TEST_BASE
 */
public class TestConfigurations {

    public static final String licenseFilePath = "pubhub-license.txt";
    public static final String alephFilePath = "aleph-conf.txt";


    public static AlephConfiguration getAlephConfigurationForTest() throws FileNotFoundException, IOException {
        Map<String, String> res = getAlephMap();
        return new AlephConfiguration(res.get(Configuration.CONF_ALEPH_URL), 
                res.get(Configuration.CONF_ALEPH_BASE), TestFileUtils.getTempDir());
    }

    protected static Map<String, String> getAlephMap() throws FileNotFoundException, IOException {
        File alephConf = new File(alephFilePath);
        if(!alephConf.isFile()) {
            throw new SkipException("No aleph configuration file for test setup at: " + alephConf.getAbsolutePath());
        }
        List<String> alephConfLines = StreamUtils.extractInputStreamAsLines(new FileInputStream(alephConf));

        if(alephConfLines.size() < 2 || !alephConfLines.get(0).startsWith("aleph-url: ") || !alephConfLines.get(1).startsWith("base: ")) {
            throw new SkipException("Bad content for the aleph conf file: " + alephConfLines);
        }

        String url = alephConfLines.get(0).replace("aleph-url: ", "");
        String base = alephConfLines.get(1).replace("base: ", "");

        Map<String, String> res = new HashMap<String, String>();
        res.put(Configuration.CONF_ALEPH_URL, url);
        res.put(Configuration.CONF_ALEPH_BASE, base);
        res.put(Configuration.CONF_ALEPH_TEMP_DIR, TestFileUtils.getTempDir().getAbsolutePath());

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
            
            confMap.put(Configuration.CONF_ALEPH_ROOT, getAlephMap());

            return new Configuration(confMap);
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }
}
