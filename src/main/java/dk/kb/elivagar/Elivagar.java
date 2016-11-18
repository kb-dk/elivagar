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

import dk.kb.elivagar.utils.CalendarUtils;
import dk.kb.elivagar.utils.FileUtils;
import dk.kb.elivagar.utils.StringUtils;
import dk.pubhub.service.ArrayOfBook;
import dk.pubhub.service.ArrayOfBookId;
import dk.pubhub.service.Book;
import dk.pubhub.service.MediaServiceAsmx;
import dk.pubhub.service.MediaServiceAsmxSoap;
import dk.pubhub.service.ModifiedBookIdList;
import dk.pubhub.service.ModifiedBookList;
import dk.pubhub.service.BookId;
import dk.pubhub.service.Image;

/**
 * Class for instantiating the Elivagar workflow.
 *
 */
public class Elivagar {
    /** The suffix of XML files.*/
    protected static final String XML_SUFFIX = ".xml";

    /** The license key GUID for pub-hub.*/
    protected final String licenseKeyGuid;
    /** The media service for using the SOAP API of pubhub.*/
    protected final MediaServiceAsmxSoap mediaService;
    /** The namespace for the service.*/
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
    public void downloadAllBookIDs(File outputDir, int count) throws JAXBException, PropertyException {
        // Fetch all bookIDs from PubHub
        ArrayOfBookId bookIDs = mediaService.listAllBookIds(licenseKeyGuid);
        // Debug printing
        System.out.println("Got all BookIDs");
        printBookIDs(bookIDs.getBookId(), outputDir, count);
    }
    
    public void downloadBookIDsAfterModifyDate(File outputDir, Date earliestDate, int count) 
            throws JAXBException, PropertyException {
        
        XMLGregorianCalendar xmlDate = CalendarUtils.getXmlGregorianCalendar(earliestDate);
        ModifiedBookIdList modifiedBookIds =  mediaService.listModifiedBookIds(licenseKeyGuid, xmlDate);
        System.out.println("Retrieved IDs for modified books. Printing out the new and modified ones, "
                + "but not the removed books");
        printBookIDs(modifiedBookIds.getNewAndModifiedBooks().getBookId(), outputDir, count);

//        mediaService.
    }

    /**
     * Marshal all the Books from PubHub to individual files.
     * This is the entire metadata for each book.
     * @param outputDir The directory where the output  
     * @param count
     * @throws JAXBException
     * @throws PropertyException
     */
    public void downloadAllBooks(File outputDir, int count) throws JAXBException, PropertyException, IOException {        
        System.out.println("Downloading all books.");
        ArrayOfBook books = mediaService.listAllBooks(licenseKeyGuid);
        
        printBook(books.getBook(), outputDir, count);
    }
    
    /**
     * 
     * @param outputDir
     * @param earliestDate
     * @param count
     * @throws JAXBException
     * @throws PropertyException
     * @throws IOException
     */
    public void downloadBooksAfterModifyDate(File outputDir, Date earliestDate, int count) throws JAXBException, PropertyException, IOException {
        System.out.println("Downloading books modified after date '" + earliestDate + "'.");
        XMLGregorianCalendar xmlDate = CalendarUtils.getXmlGregorianCalendar(earliestDate);
        ModifiedBookList modifiedBooks = mediaService.listModifiedBooks(licenseKeyGuid, xmlDate);
        
        printBook(modifiedBooks.getNewAndModifiedBooks().getBook(), outputDir, count);
    }
    
    /**
     * 
     * @param books
     * @param baseDir
     * @param count
     * @throws JAXBException
     * @throws PropertyException
     * @throws IOException
     */
    protected void printBook(List<Book> books, File baseDir, int count) throws JAXBException, PropertyException, IOException {
        JAXBContext context = JAXBContext.newInstance(Book.class);
        Marshaller marshaller = context.createMarshaller();
   
        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        JAXBElement<Book> rootElement = null;

        HttpClient httpClient = new HttpClient();
        
        for(int i = 0; i < books.size() && i < count; i++) {
            Book book = books.get(i);
            File bookDir = FileUtils.createDirectory(baseDir.getAbsolutePath() + "/" + book.getBookId() + "/");
            File bookFile = new File(bookDir, book.getBookId() +  Elivagar.XML_SUFFIX);
            // Debug printing
            System.out.println(bookFile.getAbsolutePath());

            rootElement = new JAXBElement<Book>(new QName(serviceNS, Book.class.getSimpleName()), 
                    Book.class, book);
            marshaller.marshal(rootElement, bookFile);
            
            for(Image image : book.getImages().getImage()) {
                String suffix = StringUtils.getSuffix(image.getValue());
                File imageFile = new File(bookDir, book.getBookId() + "_" + image.getType() + "." + suffix);
                
                try (OutputStream os = new FileOutputStream(imageFile)) {
                    httpClient.performDownload(os, new URL(image.getValue()));
                }
            }
        }
    }
    
    /**
     * Prints the book ids.
     * @param bookIDs The book id elements to print.
     * @param outputDir The directory where the book ids should be marshalled into files.
     * @param count The maximum number of book ids to extract.
     * @throws JAXBException If the xml marshalling fails.
     * @throws PropertyException If the properties of the marshallaer cannot be set.
     */
    protected void printBookIDs(List<BookId> bookIDs, File outputDir, int count) throws JAXBException, PropertyException {

        JAXBContext context = JAXBContext.newInstance( BookId.class );
        Marshaller marshaller = context.createMarshaller();
   
        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
   
        for(int i = 0; i < bookIDs.size() && i < count; i++) {
            BookId book = bookIDs.get(i);
            File bookIdFile = new File(outputDir, book.getValue().toString() + Elivagar.XML_SUFFIX);
            // Debug printing
            System.out.println(bookIdFile.getAbsolutePath());

            marshaller.marshal(new JAXBElement<BookId>(new QName(serviceNS, BookId.class.getSimpleName()), 
                    BookId.class, book), bookIdFile);
        }
    }
}
    
