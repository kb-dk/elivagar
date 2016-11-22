package dk.kb.elivagar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import dk.kb.elivagar.pubhub.PubhubPacker;
import dk.kb.elivagar.pubhub.PubhubRetriever;
import dk.kb.elivagar.utils.CalendarUtils;
import dk.kb.elivagar.utils.FileUtils;
import dk.kb.elivagar.utils.StringUtils;
import dk.pubhub.service.ArrayOfBook;
import dk.pubhub.service.ArrayOfBookId;
import dk.pubhub.service.ArrayOfSubject;
import dk.pubhub.service.Book;
import dk.pubhub.service.MediaServiceAsmx;
import dk.pubhub.service.MediaServiceAsmxSoap;
import dk.pubhub.service.ModifiedBookIdList;
import dk.pubhub.service.ModifiedBookList;
import dk.pubhub.service.Subject;
import dk.pubhub.service.BookId;
import dk.pubhub.service.Image;

/**
 * Class for instantiating the Elivagar workflow.
 *
 */
public class Elivagar {

    /** The license key GUID for pub-hub.*/
    protected final String licenseKeyGuid;
    
    protected final PubhubRetriever retriever;
    
    protected final PubhubPacker packer;
    
    /**
     * Constructor for the Elivagar class. 
     * @param licenseKeyGuid The license key 
     * @throws JAXBException When XML marshaling fail
     * @throws PropertyException When the Marshaller property can not be set
     * @throws IOException When creating a directory fail
     */
    public Elivagar(String licenseKeyGuid, File outputDir) {
        this.licenseKeyGuid = licenseKeyGuid;
        this.retriever = new PubhubRetriever(licenseKeyGuid);
        this.packer = new PubhubPacker(outputDir, retriever.getServiceNamespace());
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
//    
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
    
