package dk.kb.elivagar.utils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class YamlUtilsTest extends ExtendedTestCase {

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

}
