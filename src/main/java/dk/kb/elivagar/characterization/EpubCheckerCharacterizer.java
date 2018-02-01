package dk.kb.elivagar.characterization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.epubcheck.api.EpubCheck;
import com.adobe.epubcheck.api.Report;
import com.adobe.epubcheck.util.ReportingLevel;
import com.adobe.epubcheck.util.XmlReportImpl;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.exception.ArgumentCheck;

/**
 * The characterization tool for performing the EpubChecker characterization.
 */
public class EpubCheckerCharacterizer {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(EpubCheckerCharacterizer.class);

    /**
     * Constructor. 
     */
    public EpubCheckerCharacterizer() {}
    
    /**
     * Checks whether or not the given file has the required file extension.
     * @param inputFile The file to have its suffix validated.
     * @return Whether or not the file has a epub suffix.
     */
    protected boolean hasRequiredExtension(File inputFile) {
        return inputFile.getName().endsWith(Constants.EPUB_FILE_SUFFIX);
    }
    
    /**
     * Perform the characterization of the given file.
     * @param inputFile The file to characterize.
     * @param outputFile The output file where the results of the characterization is located.
     * @throws IOException If it somehow fails.
     */
    protected void characterize(File inputFile, File outputFile) throws IOException {
        ArgumentCheck.checkExistsNormalFile(inputFile, "File inputFile");
        ArgumentCheck.checkNotNull(outputFile, "File outputFile");
        log.debug("Characterizing the epub file: " + inputFile.getAbsolutePath());
        try (PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            Report report = new XmlReportImpl(printWriter, inputFile.getName(), EpubCheck.version());
            report.setReportingLevel(ReportingLevel.Info);
            report.initialize();
            EpubCheck epubChecker = new EpubCheck(inputFile, report);
            boolean valid = epubChecker.validate();
            report.generate();
            if(valid) {
                log.debug("The file '" + inputFile.getAbsolutePath() + "' is a valid epub file");
            } else {
                log.warn("The file '" + inputFile.getAbsolutePath() + "' is not a valid epub file");
            }
        }
    }
}
