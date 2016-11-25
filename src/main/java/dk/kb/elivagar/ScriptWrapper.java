package dk.kb.elivagar;

import java.io.File;
import java.io.IOException;

import dk.kb.elivagar.utils.StreamUtils;

/**
 * Class for executing an external bash script.
 */
public class ScriptWrapper {

    /** The file with the script to script.*/
    protected final File scriptFile;
    
    /**
     * Constructor.
     * @param scriptFile The script for to be called.
     */
    public ScriptWrapper(File scriptFile) {
        if(!scriptFile.isFile()) {
            throw new IllegalStateException("The file '" + scriptFile.getAbsolutePath() + "' is not valid.");
        }
        this.scriptFile = scriptFile;
    }
    
    /**
     * Calls the script with the given argument.
     * @param arg The argument(s) for the script.
     */
    public void callVoidScript(String ... args) {
        try {
            String command = "bash " + scriptFile.getAbsolutePath();
            for(String arg : args) {
                command += " " + arg;
            }
            Process p = Runtime.getRuntime().exec(command);
            int success = p.waitFor();
            if(success != 0) {
                String errMsg = "Failed to run the script.\nErrors:\n" 
                        + StreamUtils.extractInputStreamAsString(p.getErrorStream()) + "Output:\n"
                        + StreamUtils.extractInputStreamAsString(p.getInputStream());
                throw new IllegalStateException(errMsg);
            }
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Failure during execution", e);
        }
    }
}
