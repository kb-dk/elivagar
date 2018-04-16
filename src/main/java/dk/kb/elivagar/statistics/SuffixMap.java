package dk.kb.elivagar.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Container format for the results of a suffix count. Maps between the suffixes and the times they have been encountered.
 */
public class SuffixMap {
    /** The map between suffixes and the number of times they have been encountered.*/
    protected Map<String, Integer> suffixCount;
    
    /**
     * Constructor.
     */
    public SuffixMap() {
        suffixCount = new HashMap<String, Integer>();
    }
    
    /**
     * Adds a suffix to the count.
     * If the suffix already exists as a key in the SuffixMap, then the value is incremented.
     * Otherwise the suffix is added with count 1.
     * @param suffix The suffix to add.
     */
    public void addSuffix(String suffix) {
        int count = 1;
        if(suffixCount.containsKey(suffix)) {
            count = suffixCount.get(suffix) + 1;
        }
        suffixCount.put(suffix, count);
    }
    
    /**
     * @return Whether or not the map is empty.
     */
    public boolean isEmpty() {
        return suffixCount.isEmpty();
    }
    
    /**
     * Retrieves the value of a given key. Returns a 0 if the key is not found.
     * @param key The key to find the value for. 
     * @return The number of encounters of the given suffix. Returns 0 if the suffix has not yet been encountered.
     */
    public int getValue(String key) {
        if(suffixCount.containsKey(key)) {
            return suffixCount.get(key);
        } else {
            return 0;
        }
    }
    
    /**
     * Retrieves the combined count of the values of all the keys.
     * @param keys The keys whose values will be summed.
     * @return The combined sum of the value for all the given keys.
     */
    public int getMultiKeyCount(Collection<String> keys) {
        int res = 0;
        for(String key : keys) {
            if(suffixCount.containsKey(key)) {
                res += suffixCount.get(key);
            }
        }
        return res;
    }
    
    /**
     * Retrieves the count of all the other suffixes, than the ones given keys.
     * @param keys The keys to ignore when calculating all the remaining keys.
     * @return The combined count of the suffices, which are not amongst the keys.
     */
    public int getCountExcludingKeys(Collection<String> keys) {
        int res = 0;
        for(Map.Entry<String, Integer> keyValue : suffixCount.entrySet()) {
            if(!keys.contains(keyValue.getKey())) {
                res += keyValue.getValue();
            }
        }
        return res;
    }
    
    /**
     * Retrieves all the keys, which have not been accounted for.
     * Thus all the found keys, which are not part of the given key-set.
     * @param expectedKeys The keys which are expected to be found.
     * @return The missing keys.
     */
    public Collection<String> getMissingKeys(Collection<String> expectedKeys) {
        List<String> res = new ArrayList<String>();
        for(String key : suffixCount.keySet()) {
            if(!expectedKeys.contains(key)) {
                res.add(key);
            }
        }
        return  res;
    }
}
