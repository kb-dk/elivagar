package dk.kb.elivagar.characterization;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.Constants;

/**
 * The characterizer for performing the different kinds of characterization.
 */
public class CharacterizationHandler {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(CharacterizationHandler.class);

    /** The FITS characterization. May be null, if no script exists.*/
    protected FitsCharacterizer fitsCharacterizer;
    /** The epub characterization. */
    protected final EpubCharacterizer epubCharacterizer;
    
    /**
     * Constructor.
     * @param fitsScript The script for characterizing the book files. May be null, for no characterization.
     * @param epubCharacterizer The characterizer for epubs.
     */
    public CharacterizationHandler(FitsCharacterizer fitsScript, EpubCharacterizer epubCharacterizer) {
        this.fitsCharacterizer = fitsScript;
        this.epubCharacterizer = epubCharacterizer;
    }
    
    /**
     * Perform all the different kinds of characterization, if they are needed.
     * @param inputFile The file to characterize.
     */
    public void characterize(File inputFile) {
        log.debug("Characterizing the file '" + inputFile.getAbsolutePath() + "'.");
        runFitsIfNeeded(inputFile);
        runEpubCheckIfNeeded(inputFile);
    }
    
    /**
     * Check and perform the epub check chacracterization if it is needed.
     * @param inputFile The file to characterize, if it is needed.
     */
    protected void runEpubCheckIfNeeded(File inputFile) {
        if(!epubCharacterizer.hasRequiredExtension(inputFile)) {
            log.debug("Not an epub file, thus not running epub-check characterization.");
            return;
        }
        
        File outputFile = new File(inputFile.getParentFile(), inputFile.getName().toLowerCase() 
                + Constants.EPUB_METADATA_SUFFIX);
        
        if(shouldPerformCharacterization(outputFile, inputFile)) {
            try {
                epubCharacterizer.characterize(inputFile, outputFile);
            } catch (IOException e) {
                log.warn("Failure when trying to characterize the epub file: " + inputFile.getAbsolutePath(), e);
            }
        } else {
            log.trace("No need to characterizing the epub file: " + inputFile.getAbsolutePath());
        }
    }
    
    /**
     * Runs the characterization if the prerequisites for characterization are met.
     * The prerequisites are, that a characterization scripts was defined in the configuration, 
     * that either the file has not yet been characterized, 
     * or that the file is newer that the output characterization file. 
     * @param inputFile The file to have characterized.
     * @param outputFile The file where the output of the characterization should be placed.
     */
    protected void runFitsIfNeeded(File inputFile) {
        if(fitsCharacterizer != null) {
            File characterizationOutputFile = new File(inputFile.getParentFile(), inputFile.getName().toLowerCase() 
                    + Constants.FITS_METADATA_SUFFIX);
            if(shouldPerformCharacterization(characterizationOutputFile, inputFile)) {
                // 2 args; 1 for input file path and 1 for output file path.
                fitsCharacterizer.execute(inputFile, characterizationOutputFile);                    
            } else {
                log.trace("FITS output file is newer that the file to characterize. Not characterizing again.");
            }
        } else {
            log.trace("FITS is turned off.");
        }
    }
    
    /**
     * Checks whether the given type of characterization should be performed.
     * If the output file does not exist, or if it is older than the input file, 
     * then a the characterization should be performed.
     * @param outputFile The output file.
     * @param inputFile The input file.
     * @return Whether a new characterization should be performed.
     */
    protected boolean shouldPerformCharacterization(File outputFile, File inputFile) {
        return !outputFile.exists() || outputFile.lastModified() < inputFile.lastModified();
    }
}
