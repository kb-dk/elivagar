package dk.kb.elivagar.utils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import dk.kb.elivagar.testutils.TestFileUtils;

public class YamlUtilsTest extends ExtendedTestCase {

    @BeforeClass
    public void setup() throws Exception {
        TestFileUtils.setupTempDir();
    }
    
    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }
    
    @Test
    public void testLoadingMapFromYamlFile() throws Exception {
        addDescription("Tests the loading of a YAML file as a map.");
        File yamlFile = new File("src/test/resources/elivagar.yml");
        LinkedHashMap<String, LinkedHashMap> map = YamlUtils.loadYamlSettings(yamlFile);
        Assert.assertNotNull(map);
        Assert.assertFalse(map.isEmpty());
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testLoadMissingFile() throws Exception {
        addDescription("Fail when loading a file, which does not exist.");
        YamlUtils.loadYamlSettings(new File(UUID.randomUUID().toString()));
    }
    
    @Test
    public void testConstructor() {
        addDescription("Test constructor.");
        new YamlUtils();
    }
    
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testLoadingNonYamlFile() throws Exception{
        addDescription("Test a non-Yaml file.");
        File yamlFile = TestFileUtils.createTempFile(UUID.randomUUID().toString());
        YamlUtils.loadYamlSettings(yamlFile);
    }
}
