package dk.kb.elivagar.script;

import java.io.File;

/**
 * Wrapper for the script for performing the characterization of the book files from PubHub.
 * Basically wraps the execution to require the specific number of arguments (2; input and output file).
 */
public class CharacterizationScriptWrapper extends ScriptWrapper {
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
        callVoidScript("\"" + inputFile.getAbsolutePath() + "\"", "\"" + outputFile.getAbsolutePath() + "\""); 
    }
}
