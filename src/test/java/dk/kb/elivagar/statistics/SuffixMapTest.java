package dk.kb.elivagar.statistics;

import java.util.Arrays;
import java.util.UUID;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import dk.kb.elivagar.statistics.SuffixMap;

public class SuffixMapTest extends ExtendedTestCase {

    @Test
    public void testConstructor() {
        addDescription("Test the initial conditions of the class.");
        SuffixMap suffixMap = new SuffixMap();
        Assert.assertTrue(suffixMap.suffixCount.isEmpty());
        Assert.assertTrue(suffixMap.isEmpty());
    }
    
    @Test
    public void testAddSuffixAndGetValue() {
        addDescription("Test the functionality of the addSuffix and getValue methods.");
        SuffixMap suffixMap = new SuffixMap();
        String suffix = UUID.randomUUID().toString();

        Assert.assertTrue(suffixMap.suffixCount.isEmpty());
        Assert.assertEquals(suffixMap.getValue(suffix), 0);

        addStep("Add the suffix", "Should contain the suffix");
        suffixMap.addSuffix(suffix);
        
        Assert.assertFalse(suffixMap.suffixCount.isEmpty());
        Assert.assertEquals(suffixMap.getValue(suffix), 1);

        addStep("Add the suffix again multiple times", "The count for the suffix should increase equally.");
        int count = (int) (Math.round(Math.random()) % 100) + 25;
        for(int i = 1; i < count; i++) {
            suffixMap.addSuffix(suffix);
        }

        Assert.assertFalse(suffixMap.suffixCount.isEmpty());
        Assert.assertEquals(suffixMap.getValue(suffix), count);        
    }
    
    @Test
    public void testGetMultiKeyCount() {
        addDescription("Test the getMultiKeyCount method");
        SuffixMap suffixMap = new SuffixMap();
        
        addStep("Make two suffices with random number of entries, and a third suffix with no entries.", "The sum of the two suffices with entries");
        String suffix1 = UUID.randomUUID().toString();
        String suffix2 = UUID.randomUUID().toString();
        String suffix3 = UUID.randomUUID().toString();
        
        int count1 = (int) (Math.round(Math.random()) % 100) + 25;
        for(int i = 0; i < count1; i++) {
            suffixMap.addSuffix(suffix1);
        }
        
        int count2 = (int) (Math.round(Math.random()) % 100) + 25;
        for(int i = 0; i < count2; i++) {
            suffixMap.addSuffix(suffix2);
        }
        
        int total = suffixMap.getMultiKeyCount(Arrays.asList(suffix1, suffix2, suffix3));
        Assert.assertEquals(total, count1+count2);
    }
    
    @Test
    public void testGetCountExcludingKeys() {
        addDescription("Test the getCountExcludingKeys method");
        SuffixMap suffixMap = new SuffixMap();
        
        addStep("Make three suffices with random number of entries.", "Check all 6 combination of one or two suffices being excluded.");
        String suffix1 = UUID.randomUUID().toString();
        String suffix2 = UUID.randomUUID().toString();
        String suffix3 = UUID.randomUUID().toString();
        
        int count1 = (int) (Math.round(Math.random()) % 100) + 25;
        for(int i = 0; i < count1; i++) {
            suffixMap.addSuffix(suffix1);
        }
        
        int count2 = (int) (Math.round(Math.random()) % 100) + 25;
        for(int i = 0; i < count2; i++) {
            suffixMap.addSuffix(suffix2);
        }

        int count3 = (int) (Math.round(Math.random()) % 100) + 25;
        for(int i = 0; i < count3; i++) {
            suffixMap.addSuffix(suffix3);
        }
        
        int sumWithoutSuffix1 = suffixMap.getCountExcludingKeys(Arrays.asList(suffix1));
        Assert.assertEquals(sumWithoutSuffix1, count2+count3);

        int sumWithoutSuffix2 = suffixMap.getCountExcludingKeys(Arrays.asList(suffix2));
        Assert.assertEquals(sumWithoutSuffix2, count1+count3);

        int sumWithoutSuffix3 = suffixMap.getCountExcludingKeys(Arrays.asList(suffix3));
        Assert.assertEquals(sumWithoutSuffix3, count1+count2);
        
        int sumWithoutSuffix1OrSuffix2 = suffixMap.getCountExcludingKeys(Arrays.asList(suffix1, suffix2));
        Assert.assertEquals(sumWithoutSuffix1OrSuffix2, count3);

        int sumWithoutSuffix1OrSuffix3 = suffixMap.getCountExcludingKeys(Arrays.asList(suffix1, suffix3));
        Assert.assertEquals(sumWithoutSuffix1OrSuffix3, count2);
        
        int sumWithoutSuffix2OrSuffix3 = suffixMap.getCountExcludingKeys(Arrays.asList(suffix2, suffix3));
        Assert.assertEquals(sumWithoutSuffix2OrSuffix3, count1);
    }
}
