package dk.kb.elivagar.pubhub;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.utils.FileUtils;
import dk.kb.elivagar.utils.StringUtils;
import dk.pubhub.service.Book;
import dk.pubhub.service.Image;

/**
 * Class for packing the data from PubHub.
 * 
 * Each individual book will be placed in its own directory, where the book metadata is placed in an XML file, 
 * along with all images (frontpage, thumbnail, etc.) for book, and the 
 */
public class PubhubPacker {
    /** The base directory where the data should be placed.*/
    protected final File baseDir;
    /** The namespace of marshalled xml.*/
    protected final String namespace;
    /** The HTTP client for retrieving the extra files for the book, e.g. the images.*/
    protected final HttpClient httpClient;
    /** Map between marshallers and their the classes they marshall.*/
    protected final Map<String, Marshaller> marshallers;
    
    /**
     * Constructor.
     * @param baseDir 
     * @param serviceNamespace
     */
    public PubhubPacker(File baseDir, String serviceNamespace) {
        this.baseDir = baseDir;
        this.namespace = serviceNamespace;
        this.marshallers = new HashMap<String, Marshaller>();
        this.httpClient = new HttpClient();
    }
    
    /**
     * Retrieves the marshaller for the given class.
     * This is made to reuse marshallers for each class.
     * @param c The class to marshal.
     * @return The marshaller for the class.
     * @throws JAXBException If a marshaller for the class cannot be created.
     */
    protected Marshaller getMarshallerForClass(Class c) throws JAXBException {
        if(!marshallers.containsKey(c.getSimpleName())) {
            JAXBContext context = JAXBContext.newInstance(Book.class);
            Marshaller marshaller = context.createMarshaller();
       
            marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
            marshallers.put(c.getSimpleName(), marshaller);
        }
        return marshallers.get(c.getSimpleName());
    }
    
    /**
     * Pack a book with all files in a dedicated directory.
     * This means marshalling the metadata of the book, and retrieving the image files.
     * TODO: Get the e-book.
     * @param book The book to pack.
     * @throws JAXBException If the book cannot be marshalled into an XML file.
     * @throws IOException If an issue occurs when retrieving the directory, or creating or downloading the files.
     */
    public void packBook(Book book) throws JAXBException, IOException {
        File bookDir = getBookDir(book.getBookId());
        
        JAXBElement<Book> rootElement = null;
        Marshaller marshaller = getMarshallerForClass(book.getClass());
        File bookFile = new File(bookDir, book.getBookId() +  Constants.XML_SUFFIX);
        rootElement = new JAXBElement<Book>(new QName(namespace, Book.class.getSimpleName()), 
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
    
    /**
     * Packs a file for the book. This is expected to be the content file, either pdf or epub.
     * This makes a symbolic link to the file from the book-folder.
     * It is a prerequisite that the is file has the name of the ID.
     * @param bookFile The file for the book.
     */
    public void packFileForBook(File bookFile) throws IOException {
        String id = StringUtils.getPrefix(bookFile.getName());
        File bookDir = getBookDir(id);
        File symbolicBookFile = new File(bookDir, bookFile.getName());
        
        Files.createSymbolicLink(symbolicBookFile.toPath(), bookFile.toPath().toAbsolutePath());
    }
    
    /**
     * 
     * @param id
     * @return
     * @throws IOException
     */
    protected File getBookDir(String id) throws IOException {
        String path = baseDir.getAbsolutePath() + "/" + id + "/";
        return FileUtils.createDirectory(path);
    }
}
