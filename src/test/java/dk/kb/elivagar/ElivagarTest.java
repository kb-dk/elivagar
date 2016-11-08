package dk.kb.elivagar;

import junit.framework.TestCase;

public class ElivagarTest extends TestCase {

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testElivagar() throws Exception {
        // Debugging
        System.out.print("Test begin");

        Elivagar elivagar = new Elivagar();
        elivagar.elivagar();
        
        // Debugging
        System.out.print("Test end");
    }
}
