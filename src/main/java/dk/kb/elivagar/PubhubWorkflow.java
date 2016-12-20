package dk.kb.elivagar;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.pubhub.PubhubMetadataRetriever;
import dk.kb.elivagar.pubhub.PubhubPacker;
import dk.kb.elivagar.pubhub.PubhubStatistics;
import dk.kb.elivagar.script.CharacterizationScriptWrapper;
import dk.pubhub.service.Book;

/**
 * Workflow for the pubhub.
 */
public class PubhubWorkflow {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(PubhubWorkflow.class);

    /** The configuration for pubhub.*/
    protected final Configuration conf;
    
    /** The retriever for Pubhub.*/
    protected final PubhubMetadataRetriever retriever;
    /** The packer of the Pubhub data.*/
    protected final PubhubPacker packer;
    
    /**
     * Constructor. 
     * @param conf The elivagar configuration. 
     */
    public PubhubWorkflow(Configuration conf) {
        this.conf = conf;
        this.retriever = new PubhubMetadataRetriever(conf.getLicenseKey());
        CharacterizationScriptWrapper script = null;
        if(conf.getCharacterizationScriptFile() != null) {
            script = new CharacterizationScriptWrapper(conf.getCharacterizationScriptFile()); 
        }
        this.packer = new PubhubPacker(conf, retriever.getServiceNamespace(), script);
    }
    
    /**
     * Retrieves all the books.
     * @param max The maximum number of books to retrieve.
     * @throws JAXBException If XML marshaling fail
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
        List<Book> books = retriever.downloadBookMetadataAfterModifyDate(
                earliestDate).getNewAndModifiedBooks().getBook();
        for(int i = 0; i < books.size() && i < max; i++) {
            Book book = books.get(i);
            packer.packBook(book);
        }
    }
    
    /**
     * Packs the files for the books into the right folder.
     * It is asserted, that the book files is named with the id as the suffix.
     */
    public void packFilesForBooks() {
        if(conf.getFileDir().listFiles() == null) {
            log.info("No book files to package.");
            return;
        }
        for(File fileForBook : conf.getFileDir().listFiles()) {
            try {
                if(fileForBook.isFile()) {
                    packer.packFileForEbook(fileForBook);
                } else {
                    log.trace("Cannot package directory: " + fileForBook.getAbsolutePath());
                }
            } catch (IOException e) {
                log.error("Failed to package the file '" + fileForBook.getAbsolutePath() + "' for a book. "
                        + "Trying to continue with next book file.", e);
            }
        }
    }
    
    /**
     * Makes and prints the statistics for the both the ebook directory and the audio directory.
     * @param printer The print stream where the output is written.
     */
    public void makeStatistics(PrintStream printer) {
        if(conf.getEbookOutputDir().list() != null) {
            makeStatisticsForDirectory(printer, conf.getEbookOutputDir());
        } else {
            printer.println("No ebooks to make statistics upon.");
        }
        if(conf.getAudioOutputDir().list() != null) {
            makeStatisticsForDirectory(printer, conf.getAudioOutputDir());
        } else {
            printer.println("No ebooks to make statistics upon.");
        }
    }
    
    /**
     * Calculates the statistics on the books in the given directory.
     * @param printer The print stream where the output is written.
     * @param dir The directory to calculate the statistics upon.
     */
    protected void makeStatisticsForDirectory(PrintStream printer, File dir) {
        PubhubStatistics statistics = new PubhubStatistics(dir);
        printer.println("Calculating the statistics for directory: " + dir.getAbsolutePath());
        statistics.calculateStatistics();
        printer.println("Number of book directories traversed: " + statistics.getTotalCount());
        printer.println("Number of book directories with both book file and metadata file: '" 
                + statistics.getBothDataCount() + "'");
        printer.println("Number of book directories with only book file: '" 
                + statistics.getOnlyBookFileCount() + "'");
        printer.println("Number of book directories with only metadata file: '" 
                + statistics.getOnlyMetadataCount() + "'");
        printer.println("Number of book directories with neither book file nor metadata file: '" 
                + statistics.getNeitherDataCount() + "'");
    }    
}
