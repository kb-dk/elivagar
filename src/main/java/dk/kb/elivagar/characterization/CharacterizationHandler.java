package dk.kb.elivagar.characterization;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.Constants;

/**
 * The characterization handler for performing different kinds of characterization.
 * Currently supports both FITS and EpubCheck characterization.
 */
public class CharacterizationHandler {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(CharacterizationHandler.class);

    /** The FITS characterization. May be null, if no script exists.*/
    protected FitsCharacterizer fitsCharacterizer;
    /** The epub characterization. */
    protected final EpubCheckerCharacterizer epubCharacterizer;
    
    /**
     * Constructor.
     * @param fitsScript The script for characterizing the book files. May be null, for no characterization.
     * @param epubCharacterizer The characterizer for epubs.
     */
    public CharacterizationHandler(FitsCharacterizer fitsScript, EpubCheckerCharacterizer epubCharacterizer) {
        this.fitsCharacterizer = fitsScript;
        this.epubCharacterizer = epubCharacterizer;
    }
    
    /**
     * Perform all the different kinds of characterization, if they are needed.
     * @param inputFile The file to characterize.
     * @param outputDir The directory, where the characterization output file should be placed.
     */
    public void characterize(File inputFile, File outputDir) {
        log.debug("Characterizing the file '" + inputFile.getAbsolutePath() + "'.");
        runFitsIfNeeded(inputFile, outputDir);
        runEpubCheckIfNeeded(inputFile, outputDir);
    }
    
    /**
     * Check and do the epubcheck chacracterization if it is needed.
     * @param inputFile The file to characterize, if it is needed.
     * @param outputDir The directory, where the characterization output file should be placed.
     */
    protected void runEpubCheckIfNeeded(File inputFile, File outputDir) {
        if(!epubCharacterizer.hasRequiredExtension(inputFile)) {
            log.debug("Not an epub file, thus not running epubcheck characterization.");
            return;
        }
        
        File outputFile = new File(outputDir, inputFile.getName().toLowerCase() 
                + Constants.EPUBCHECK_METADATA_SUFFIX);
        
        if(shouldCharacterize(outputFile, inputFile)) {
            try {
                epubCharacterizer.characterize(inputFile, outputFile);
            } catch (Throwable e) {
                log.warn("Failure when trying to characterize the epub file: " + inputFile.getAbsolutePath(), e);
                log.info("Trying to cleanup memory. Then continue.");
                System.gc();
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
     * @param outputDir The directory, where the characterization output file should be placed.
     */
    protected void runFitsIfNeeded(File inputFile, File outputDir) {
        if(fitsCharacterizer == null) {
            log.debug("FITS is turned off.");
            return;
        }
        File characterizationOutputFile = new File(outputDir, inputFile.getName().toLowerCase() 
                + Constants.FITS_METADATA_SUFFIX);
        if(shouldCharacterize(characterizationOutputFile, inputFile)) {
            try { 
                fitsCharacterizer.performCharacterization(inputFile, characterizationOutputFile);
            } catch (Throwable e) {
                log.warn("Failure when trying to do the FITS characterization for the file: "
                        + inputFile.getAbsolutePath(), e);
            }
        } else {
            log.trace("FITS output file is newer that the file to characterize. Not characterizing again.");
        }
    }
    
    /**
     * Checks whether the given type of characterization should be performed.
     * If the output file does not exist, or if it is older than the input file, then a characterization is needed.
     * Or if the output file is of size zero.
     * @param outputFile The output file.
     * @param inputFile The input file.
     * @return Whether a new characterization is needed.
     */
    protected boolean shouldCharacterize(File outputFile, File inputFile) {
        return !outputFile.exists() || outputFile.lastModified() < inputFile.lastModified() 
                || outputFile.length() == 0;
    }
}
