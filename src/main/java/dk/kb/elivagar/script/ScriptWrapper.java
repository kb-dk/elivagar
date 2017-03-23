package dk.kb.elivagar.script;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.utils.StreamUtils;

/**
 * Class for executing an external bash script.
 */
public class ScriptWrapper {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(ScriptWrapper.class);

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
     * @param args The argument(s) for the script.
     */
    protected void callVoidScript(String ... args) {
        StringBuffer command = new StringBuffer();
        command.append("bash ");
        command.append(scriptFile.getAbsolutePath());
        for(String arg : args) {
            command.append(" " + arg);
        }
        try {
            log.info("Executing commandline: " + command.toString());
            Process p = Runtime.getRuntime().exec(command.toString());
            int success = p.waitFor();
            if(success != 0) {
                String errMsg = "Failed to run the script.\nErrors:\n" 
                        + StreamUtils.extractInputStreamAsString(p.getErrorStream()) + "Output:\n"
                        + StreamUtils.extractInputStreamAsString(p.getInputStream());
                throw new IllegalStateException(errMsg);
            } else {
                log.debug("Successful execution of script. Received the following output:\nErrors:\n" 
                        + StreamUtils.extractInputStreamAsString(p.getErrorStream()) + "Output:\n"
                        + StreamUtils.extractInputStreamAsString(p.getInputStream()));
            }
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("Failure during execution of command: '" + command + "'", e);
        }
    }
}
