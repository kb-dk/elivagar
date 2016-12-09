package dk.kb.elivagar.pubhub;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.ScriptWrapper;
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
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(PubhubPacker.class);

    /** The base directory where the data should be placed.*/
    protected final File baseDir;
    /** The namespace of marshalled xml.*/
    protected final String namespace;
    /** The HTTP client for retrieving the extra files for the book, e.g. the images.*/
    protected final HttpClient httpClient;
    /** Map between marshallers and their the classes they marshall.*/
    protected final Map<String, Marshaller> marshallers;
    /** The script for characterizing the book files. May be null, if no script exists.*/
    protected PubhubCharacterizationScriptWrapper characterizationScript;
    
    /**
     * Constructor.
     * @param baseDir The base directory where the books are being packages.
     * @param serviceNamespace The namespace for the service.
     * @param script The script for characterizing the book files. May be null, for no characterization.
     */
    public PubhubPacker(File baseDir, String serviceNamespace, PubhubCharacterizationScriptWrapper script) {
        this.baseDir = baseDir;
        this.namespace = serviceNamespace;
        this.marshallers = new HashMap<String, Marshaller>();
        this.httpClient = new HttpClient();
        this.characterizationScript = script;
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
            log.debug("Instantiating marshaller for class '" + c.getName() + "'.");
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
        log.info("Packaging book '" + book.getBookId() + "'.");
        File bookDir = getBookDir(book.getBookId());
        
        JAXBElement<Book> rootElement = null;
        Marshaller marshaller = getMarshallerForClass(book.getClass());
        File bookFile = new File(bookDir, book.getBookId() +  Constants.XML_SUFFIX);
        rootElement = new JAXBElement<Book>(new QName(namespace, Book.class.getSimpleName()), 
                Book.class, book);
        marshaller.marshal(rootElement, bookFile);

        for(Image image : book.getImages().getImage()) {
            log.debug("Retrieving image file for '" + book.getBookId() + "', at " + image.getValue());
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
     * @throws IOException If the book directory cannot be instantiated, or if the symbolic link from the
     * original book file cannot be created.
     */
    public void packFileForBook(File bookFile) throws IOException {
        String id = StringUtils.getPrefix(bookFile.getName());
        log.info("Packaging book file for book-id: " + id);
        File bookDir = getBookDir(id);
        File symbolicBookFile = new File(bookDir, bookFile.getName());
        if(symbolicBookFile.isFile()) {
            log.trace("The symbolic link for the book file for book-id '" + id + "' already exists.");
        } else {
            Files.createSymbolicLink(symbolicBookFile.toPath(), bookFile.toPath().toAbsolutePath());
            if(characterizationScript != null) {
                // 2 args; 1 for input file path and 1 for output file path.
                File characterizationOutputFile = new File(bookDir, id + ".fits");
                characterizationScript.execute(bookFile, characterizationOutputFile);
            }
        }
    }
    
    /**
     * Retrieves the directory for the book with the given ID.
     * @param id The ID for the book, whose directory should be retrieved.
     * @return The directory for the given book id.
     * @throws IOException If the directory cannot be instantiated.
     */
    protected File getBookDir(String id) throws IOException {
        String path = baseDir.getAbsolutePath() + "/" + id + "/";
        return FileUtils.createDirectory(path);
    }
}
