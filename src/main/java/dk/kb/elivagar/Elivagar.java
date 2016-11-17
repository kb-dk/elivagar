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

    private static final String XML_SUFFIX = ".xml";

    protected final String licenseKeyGuid;
    protected final MediaServiceAsmxSoap mediaService;
    protected final String serviceNS;
    
    /**
     * Constructor for the Elivagar class. 
     * @param licenseKeyGuid The license key 
     * @throws JAXBException When XML marshaling fail
     * @throws PropertyException When the Marshaller property can not be set
     * @throws IOException When creating a directory fail
     */
    public Elivagar(String licenseKeyGuid) throws PropertyException, JAXBException, IOException {
        this.licenseKeyGuid = licenseKeyGuid;
       
        MediaServiceAsmx mediaServiceAsmx = new MediaServiceAsmx();
        QName serviceName = mediaServiceAsmx.getServiceName();
        serviceNS = serviceName.getNamespaceURI();
        mediaService = mediaServiceAsmx.getMediaServiceAsmxSoap();
    }

    /**
     * Marshal BookIDs from PubHub to individual files
     * @param mediaService MediaServiceAsmxSoap object 
     * @param serviceNS Namespace for service PubHub http://service.pubhub.dk/
     * @throws JAXBException When XML marshaling fail
     * @throws PropertyException When the Marshaller property can not be set
     */
    public void marshalBookIDs(File outputDir) throws JAXBException, PropertyException {
        // Fetch all bookIDs from PubHub
        ArrayOfBookId BookIDs = mediaService.listAllBookIds(licenseKeyGuid);
        // Debug printing
        System.out.println("Got all BookIDs");

        JAXBContext context = JAXBContext.newInstance( BookId.class );
        Marshaller marshaller = context.createMarshaller();
   
        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
   
        for (BookId book : BookIDs.getBookId()) {
            File bookIdFile = new File(outputDir, book.getValue().toString() + Elivagar.XML_SUFFIX);
            // Debug printing
            System.out.println(bookIdFile.getAbsolutePath());

            marshaller.marshal(new JAXBElement<BookId>(new QName(serviceNS, BookId.class.getSimpleName()), 
                    BookId.class, book), bookIdFile);
        }
    }

    /**
     * Marshal Books from PubHub to individual files
     * @param mediaService MediaServiceAsmxSoap object
     * @param serviceNS Namespace for service PubHub http://service.pubhub.dk/
     * @throws JAXBException When XML marshaling fail
     * @throws PropertyException When the Marshaller property can not be set
     */
    public void marshalBooks(File outputDir) throws JAXBException, PropertyException {

        // Toggle filename for readability (debugging)
        boolean debugging = true;
        
        // Fetch all books from PubHub
        ArrayOfBook books = mediaService.listAllBooks(licenseKeyGuid);
        // Debug printing
        System.out.println("Got Books");
        
        JAXBContext context = JAXBContext.newInstance(Book.class);
        Marshaller marshaller = context.createMarshaller();
   
        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

        JAXBElement<Book> rootElement = null;

        for (Book book : books.getBook()) {
            File bookFile;
            if (debugging == true) {
                bookFile = new File(outputDir, book.getBookId().toString() +  Elivagar.XML_SUFFIX);
            }
            else {
                bookFile = new File(outputDir, book.getIdentifier().toString() +  Elivagar.XML_SUFFIX);
            }
            // Debug printing
            System.out.println(bookFile.getAbsolutePath());

            rootElement = new JAXBElement<Book>(new QName(serviceNS, Book.class.getSimpleName()), 
                    Book.class, book);
            marshaller.marshal(rootElement, bookFile);
        }
    }
}
    
