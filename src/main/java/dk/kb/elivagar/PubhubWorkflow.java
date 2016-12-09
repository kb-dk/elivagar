package dk.kb.elivagar;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.pubhub.PubhubCharacterizationScriptWrapper;
import dk.kb.elivagar.pubhub.PubhubMetadataRetriever;
import dk.kb.elivagar.pubhub.PubhubPacker;
import dk.kb.elivagar.pubhub.PubhubStatistics;
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
     * Constructor for the Elivagar class. 
     * @param conf The configuration for elivagar. 
     */
    public PubhubWorkflow(Configuration conf) {
        this.conf = conf;
        this.retriever = new PubhubMetadataRetriever(conf.getLicenseKey());
        PubhubCharacterizationScriptWrapper script = null;
        if(conf.getCharacterizationScriptFile() != null) {
            script = new PubhubCharacterizationScriptWrapper(conf.getCharacterizationScriptFile()); 
        }
        this.packer = new PubhubPacker(conf.getOutputDir(), retriever.getServiceNamespace(), script);
    }
    
    /**
     * Retrieves all the books.
     * @param max The maximum number of books to retrieve.
     * @throws JAXBException If XML marshaling fail
     * @throws IOException If files cannot be created or downloaded.
     */
    public void retrieveAllBooks(long max) throws JAXBException, IOException {
        List<Book> books = retriever.downloadAllBooks().getBook();
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
        List<Book> books = retriever.downloadBooksAfterModifyDate(earliestDate).getNewAndModifiedBooks().getBook();
        for(int i = 0; i < books.size() && i < max; i++) {
            Book book = books.get(i);
            packer.packBook(book);
        }
    }
    
    /**
     * Packs the files for the books into the right
     * It is asserted, that the book files has the name of the 
     */
    public void packFilesForBooks() {
        for(File fileForBook : conf.getFileDir().listFiles()) {
            try {
                if(fileForBook.isFile()) {
                    packer.packFileForBook(fileForBook);
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
     * Makes and prints the statistics for the base directory.
     * @param printer The stream where the output is written.
     */
    public void makeStatistics(PrintStream printer) {
        PubhubStatistics statistics = new PubhubStatistics(conf.outputDir);
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
