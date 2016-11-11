package dk.kb.elivagar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.namespace.QName;

import dk.pubhub.service.ArrayOfBook;
import dk.pubhub.service.ArrayOfBookId;
import dk.pubhub.service.Book;
import dk.pubhub.service.MediaServiceAsmx;
import dk.pubhub.service.MediaServiceAsmxSoap;

import dk.pubhub.service.BookId;

/**
 * Class for instantiating the Elivagar workflow.
 *
 */
public class Elivagar {

    // TODO Should be moved to config file 
    private static final String LICENSE_KEY_GUID = "WRITE_YOUR_LICENSE_KEY_GUID_HERE";
    
    private static String userHome = System.getProperty("user.home");

    private static String bookIdsPath = userHome + "/Tmp/BookIDs/";
    private static String booksPath = userHome + "/Tmp/Books/";


    private static final String XML_SUFFIX = ".xml";
    
    /**
     * Constructor for the Elivagar class. 
     * @param args List of arguments delivered from the commandline.
     * So far all arguments will be ignored.
     * @throws JAXBException When XML marshaling fail
     * @throws PropertyException When the Marshaller property can not be set
     * @throws IOException When creating a directory fail
     */
    public void elivagar() 
            throws PropertyException, JAXBException, IOException {
        
        // Controls for debugging
        boolean marshal_bookIDs_to_individual_files = true;
        boolean marshal_books_to_individual_files = true;
       
        Elivagar.createDirectory(bookIdsPath);
        Elivagar.createDirectory(booksPath);
        
        MediaServiceAsmx mediaServiceAsmx = new MediaServiceAsmx();
        
        QName serviceName = mediaServiceAsmx.getServiceName();
        String serviceNS = serviceName.getNamespaceURI();
        
        MediaServiceAsmxSoap mediaService = mediaServiceAsmx.getMediaServiceAsmxSoap();

        // Marshal BookIDs to individual file
        if (marshal_bookIDs_to_individual_files) {
            marshalBookIDs(mediaService, serviceNS);
            // Debugging
            System.out.println("Marshaled all BookIDs to individual files");

        }
       
        // Marshal Books to individual file
        if (marshal_books_to_individual_files) {
            marshalBooks(mediaService, serviceNS);
            // Debugging
            System.out.println("Marshaled all Books to individual files");
        }
    }

    /**
     * Marshal BookIDs from PubHub to individual files
     * @param mediaService MediaServiceAsmxSoap object 
     * @param serviceNS Namespace for service PubHub http://service.pubhub.dk/
     * @throws JAXBException When XML marshaling fail
     * @throws PropertyException When the Marshaller property can not be set
     */
    private static void marshalBookIDs(MediaServiceAsmxSoap mediaService, String serviceNS) 
            throws JAXBException, PropertyException {

        String BookID_path;
        
        // Fetch all bookIDs from PubHub
        ArrayOfBookId BookIDs = mediaService.listAllBookIds(Elivagar.LICENSE_KEY_GUID);
        // Debug printing
        System.out.println("Got all BookIDs");

        JAXBContext context = JAXBContext.newInstance( BookId.class );
        Marshaller marshaller = context.createMarshaller();
   
        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
   
        for (BookId book : BookIDs.getBookId()) {
            BookID_path = Elivagar.bookIdsPath + book.getValue().toString() + Elivagar.XML_SUFFIX;
            // Debug printing
            System.out.println(BookID_path);

            marshaller.marshal(new JAXBElement<BookId>(new QName(serviceNS, BookId.class.getSimpleName()), 
                    BookId.class, book), new File(BookID_path));
        }
    }

    /**
     * Marshal Books from PubHub to individual files
     * @param mediaService MediaServiceAsmxSoap object
     * @param serviceNS Namespace for service PubHub http://service.pubhub.dk/
     * @throws JAXBException When XML marshaling fail
     * @throws PropertyException When the Marshaller property can not be set
     */
    private static void marshalBooks(MediaServiceAsmxSoap mediaService, String serviceNS)
            throws JAXBException, PropertyException {
        
        String book_Path = null;

        // Fetch all books from PubHub
        ArrayOfBook books = mediaService.listAllBooks(Elivagar.LICENSE_KEY_GUID);
        // Debug printing
        System.out.println("Got Books");
        
        JAXBContext context = JAXBContext.newInstance(Book.class);
        Marshaller marshaller = context.createMarshaller();
   
        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

        JAXBElement<Book> rootElement = null;

        for (Book book : books.getBook()) {
            book_Path = Elivagar.booksPath + book.getBookId().toString() +  Elivagar.XML_SUFFIX;
            
            // Debug printing
            System.out.println(book_Path);

            rootElement = new JAXBElement<Book>(new QName(serviceNS, Book.class.getSimpleName()), 
                    Book.class, book);
            marshaller.marshal(rootElement, new File(book_Path));
        }
    }
    /**
     * Create or reuse directory
     * @param dirPath Path to directory
     * @throws IOException When creating a directory fail
     */
    private static void createDirectory(String dirPath) 
            throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
                Files.createDirectories(path);
        }
    }
}
    
