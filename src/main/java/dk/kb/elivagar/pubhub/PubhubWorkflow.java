package dk.kb.elivagar.pubhub;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.characterization.CharacterizationHandler;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.exception.ArgumentCheck;
import dk.kb.elivagar.statistics.ElivagarStatistics;
import dk.pubhub.service.Book;

/**
 * Workflow for the pubhub.
 */
public class PubhubWorkflow {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(PubhubWorkflow.class);

    /** The sub directory path from audio book dir to the folder with the actual file.*/
    protected static final String AUDIO_SUB_DIR_PATH = ""; //"Full/Mp3/" changed delivery dir

    /** The configuration for pubhub.*/
    protected final Configuration conf;

    /** The retriever for Pubhub.*/
    protected final PubhubMetadataRetriever retriever;
    /** The packer of the Pubhub data.*/
    protected final PubhubPacker packer;
    /** The characterizer for performing the different kinds of characterization.*/
    protected final CharacterizationHandler characterizer;
    
    /**
     * Constructor. 
     * @param conf The elivagar configuration. 
     * @param retriever The retriever of metadata from PubHub
     * @param characterizer The characterization handler.
     * @param packer The PubhubPacker.
     */
    public PubhubWorkflow(Configuration conf, PubhubMetadataRetriever retriever, 
            CharacterizationHandler characterizer, PubhubPacker packer) {
        ArgumentCheck.checkNotNull(conf, "Configuration conf");
        ArgumentCheck.checkNotNull(retriever, "PubhubMetadataRetriever retriever");
        ArgumentCheck.checkNotNull(characterizer, "CharacterizationHandler characterizer");

        this.conf = conf;
        this.retriever = retriever;
        this.characterizer = characterizer;
        this.packer = packer;
    }

    /**
     * Retrieves all the books.
     * @param max The maximum number of books to retrieve.
     * @throws JAXBException If XML marshalling fail.
     * @throws IOException If files cannot be created or downloaded.
     */
    public void retrieveAllBooks(long max) throws JAXBException, IOException {
        List<Book> books = retriever.downloadAllBookMetadata().getBook();
        for(int i = 0; i < books.size() && i < max; i++) {
            Book book = books.get(i);
            packer.packBook(book);
        }
    }

    /**
     * Retrieves the books which have been modified after a given date.
     * Though with a given maximum number of books to retrieve.
     * @param earliestDate The earliest modify date for the book.
     * @param max The maximum number of books to retrieve.
     * @throws JAXBException If XML marshaling fail
     * @throws IOException If files cannot be created or downloaded.
     */
    public void retrieveModifiedBooks(Date earliestDate, long max) throws JAXBException, IOException {
        ArgumentCheck.checkNotNull(earliestDate, "Date earliestDate");
        
        List<Book> books = retriever.downloadBookMetadataAfterModifyDate(
                earliestDate).getNewAndModifiedBooks().getBook();
        for(int i = 0; i < books.size() && i < max; i++) {
            Book book = books.get(i);
            packer.packBook(book);
        }
    }

    /**
     * Instantiates the packaging of both ebooks and audio books.
     */
    public void packFilesForBooks() {
        packFilesForEbooks();
        packFilesForAudioBooks();
    }

    /**
     * Packs the files for the ebooks into their right folder.
     * It is asserted, that the book files is named with the id as the prefix.
     */
    protected void packFilesForEbooks() {
        File[] eBooks = conf.getEbookFileDir().listFiles();
        if(eBooks == null) {
            log.info("No ebook files to package. We are done.");
        } else {
            for(File fileForBook : eBooks) {
                try {
                    if(fileForBook.isFile()) {
                        packer.packFileForEbook(fileForBook);
                    } else {
                        log.warn("Cannot package directory: " + fileForBook.getAbsolutePath());
                    }
                } catch (IOException e) {
                    log.error("Failed to package the file '" + fileForBook.getAbsolutePath() + "' for a book. "
                            + "Trying to continue with next book file.", e);
                }
            }
        }
    }

    /**
     * Packs the files for the audio books into their right folder.
     * The audio books are placed in a sub-directory with the following structure:
     * $AUDIO_BOOK_BASE_DIR / ${ID} / Full / Mp3 / ${ID} . mp3
     * 
     * It is asserted, that the book files is named with the id as the prefix.
     */
    protected void packFilesForAudioBooks() {
        File[] audioBooks = conf.getAudioFileDir().listFiles();
        if(audioBooks == null) {
            log.info("No audio files to package. We are done.");
        } else {
            for(File audioBookBaseDir : audioBooks) {
                String id = audioBookBaseDir.getName();
                File audioBookFileDir = new File(audioBookBaseDir, AUDIO_SUB_DIR_PATH);
                File[] audioBookFiles = audioBookFileDir.listFiles();
                if(audioBookFiles == null) {
                    log.warn("Cannot handle non-existing Audio-book file: " 
                            + audioBookFileDir.getAbsolutePath());
                } else {
                    for(File audioBookFile : audioBookFiles) {
                        try {
                            if(!audioBookFile.getName().startsWith(id)) {
                                log.info("Ignoring the file '" + audioBookFile.getAbsolutePath() + " since it does "
                                        + "not comply with the format '{ID}/" + AUDIO_SUB_DIR_PATH + "{ID}.{suffix}");
                            } else {
                                if(audioBookFile.isFile()) {
                                    packer.packFileForAudio(audioBookFile);
                                } else {
                                    log.warn("Cannot handle directory: " 
                                            + audioBookFile.getAbsolutePath());
                                }
                            }
                        } catch (IOException e) {
                            log.error("Failed to package the file '" + audioBookBaseDir.getAbsolutePath() 
                                    + "' for a audio book. Trying to continue with next audio book file.", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Makes and prints the statistics for the both the ebook directory and the audio directory.
     * @param printer The print stream where the output is written.
     * @param date The earliest date for marking a file or directory as 'new'.
     * Used to identify the new object found in the current run of the workflow.
     */
    public void makeStatistics(PrintStream printer, long date) {
        ArgumentCheck.checkNotNull(printer, "PrintStream printer");
        
        ElivagarStatistics statistics = new ElivagarStatistics(conf);
        if(conf.getEbookOutputDir().list() != null) {
            statistics.traverseBaseDir(conf.getEbookOutputDir(), date);
        } else {
            printer.println("No ebooks to make statistics upon.");
        }
        if(conf.getAudioOutputDir().list() != null) {
            statistics.traverseBaseDir(conf.getAudioOutputDir(), date);
        } else {
            printer.println("No audio books to make statistics upon.");
        }
        statistics.printStatistics(printer);
    }
}
