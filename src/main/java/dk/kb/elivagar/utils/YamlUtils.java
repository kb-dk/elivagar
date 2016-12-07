package dk.kb.elivagar.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;

import org.yaml.snakeyaml.Yaml;

/** Class for reading YAML v 1.1 configuration files. */
public class YamlUtils {
    /**
     * Load YAML settings in the given file.
     * @param ymlFile The settings file in YAML format to load
     * @return the loaded settings as a {@link LinkedHashMap}
     */
    public static LinkedHashMap<String, LinkedHashMap> loadYamlSettings(File ymlFile) {
        
        Object loadedSettings = null;
        try (InputStream input = new FileInputStream(ymlFile)){
            loadedSettings = new Yaml().load(input);
            if (!(loadedSettings instanceof LinkedHashMap)) {
                throw new IllegalArgumentException("Internal error. Unable to read settings. Excepted load method to " 
                        + "return a LinkedHashMap, but it returned a " + loadedSettings.getClass().getName() 
                        + " instead");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Internal error. Unable to read settings from file '" 
                    + ymlFile.getAbsolutePath() + "'. Reason:  ", e);
        }
        return (LinkedHashMap<String, LinkedHashMap>) loadedSettings;
    }
}
