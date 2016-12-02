package dk.kb.elivagar;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.bind.PropertyException;

import dk.kb.elivagar.pubhub.PubhubPacker;
import dk.kb.elivagar.pubhub.PubhubMetadataRetriever;
import dk.pubhub.service.Book;

/**
 * Class for instantiating the Elivagar workflow.
 *
 */
public class Elivagar {

    /** The configuration for pubhub.*/
    protected final Configuration conf;
    
    /** The retriever for Pubhub.*/
    protected final PubhubMetadataRetriever retriever;
    /** The packer of the Pubhub data.*/
    protected final PubhubPacker packer;
    
    /**
     * Constructor for the Elivagar class. 
     * @param conf The configuration for elivagar. 
     * @throws JAXBException When XML marshaling fail
     * @throws PropertyException When the Marshaller property can not be set
     * @throws IOException When creating a directory fail
     */
    public Elivagar(Configuration conf) {
        this.conf = conf;
        this.retriever = new PubhubMetadataRetriever(conf.getLicenseKey());
        this.packer = new PubhubPacker(conf.getOutputDir(), retriever.getServiceNamespace());
    }
    
    /**
     * Retrieves all the books.
     * @param max The maximum number of books to retrieve.
     * @throws JAXBException If XML marshaling fail
     * @throws IOException If files cannot be created or downloaded.
     */
    public void retrieveAllBooks(int max) throws JAXBException, IOException {
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
    public void retrieveModifiedBooks(Date earliestDate, int max) throws JAXBException, IOException {
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
                    // TODO: log this non-file (e.g. a directory).
                }
            } catch (IOException e) {
                // TODO: log this
            }
        }
    }
    
//    /**
//     * Prints the book ids.
//     * @param bookIDs The book id elements to print.
//     * @param outputDir The directory where the book ids should be marshalled into files.
//     * @param count The maximum number of book ids to extract.
//     * @throws JAXBException If the xml marshalling fails.
//     * @throws PropertyException If the properties of the marshallaer cannot be set.
//     */
//    protected void printBookIDs(List<BookId> bookIDs, File outputDir, int count) throws JAXBException, PropertyException {
//
//        JAXBContext context = JAXBContext.newInstance( BookId.class );
//        Marshaller marshaller = context.createMarshaller();
//   
//        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
//   
//        for(int i = 0; i < bookIDs.size() && i < count; i++) {
//            BookId book = bookIDs.get(i);
//            File bookIdFile = new File(outputDir, book.getValue().toString() + Elivagar.XML_SUFFIX);
//            // Debug printing
//            System.out.println(bookIdFile.getAbsolutePath());
//
//            marshaller.marshal(new JAXBElement<BookId>(new QName(serviceNS, BookId.class.getSimpleName()), 
//                    BookId.class, book), bookIdFile);
//        }
//    }
}
    
