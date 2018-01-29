package dk.kb.elivagar.script;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.exception.ArgumentCheck;

/**
 * Wrapper for the script for performing the characterization of the book files from PubHub.
 * Basically wraps the execution to require the specific number of arguments (2; input and output file).
 */
public class CharacterizationScriptWrapper extends ScriptWrapper {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(CharacterizationScriptWrapper.class);

    /**
     * Constructor.
     * @param scriptFile The script.
     */
    public CharacterizationScriptWrapper(File scriptFile) {
        super(scriptFile);
    }

    /**
     * Execute the characterization script.
     * The script will be executed on the input file and the results will be placed in the output file.
     * @param inputFile The input file, which will be characterized.
     * @param outputFile The output file, where the characterization results is placed.
     */
    public void execute(File inputFile, File outputFile) {
        ArgumentCheck.checkExistsNormalFile(inputFile, "File inputFile");
        ArgumentCheck.checkNotNull(outputFile, "File outputFile");
        if(inputFile.getAbsolutePath().contains(" ") || outputFile.getAbsolutePath().contains(" ")) {
            log.warn("Could not run the characterization due to filename containing a space. "
                    + "input file: " + inputFile.getAbsolutePath() + ", output file: " + outputFile.getAbsolutePath());
            return;
        }
        callVoidScript(inputFile.getAbsolutePath(), outputFile.getAbsolutePath()); 
    }
}
