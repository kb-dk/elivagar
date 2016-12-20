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

import dk.kb.elivagar.Configuration;
import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.script.CharacterizationScriptWrapper;
import dk.kb.elivagar.utils.FileUtils;
import dk.kb.elivagar.utils.StringUtils;
import dk.pubhub.service.Book;
import dk.pubhub.service.BookTypeEnum;
import dk.pubhub.service.Image;

/**
 * Class for packing the data from PubHub.
 * 
 * Each individual book will be placed in its own directory, where the book metadata is placed in an XML file, 
 * along with all images (frontpage, thumbnail, etc.) for book, and a symbolic link to the content file for the book.
 */
public class PubhubPacker {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(PubhubPacker.class);

    /** The suffix of XML files.*/
    public static final String XML_SUFFIX = ".xml";
    /** The suffix for the fits characterization metadata output files.*/
    public static final String FITS_SUFFIX = ".fits";

    /** The Configuration with the base directories for the files to be packed.*/
    protected final Configuration conf;
    /** The namespace of marshalled xml.*/
    protected final String namespace;
    /** The HTTP client for retrieving the extra files for the book, e.g. the images.*/
    protected final HttpClient httpClient;
    /** Map between marshallers and their the classes they marshall.*/
    protected final Map<String, Marshaller> marshallers;
    /** The script for characterizing the book files. May be null, if no script exists.*/
    protected CharacterizationScriptWrapper characterizationScript;

    /**
     * Constructor.
     * @param conf The Configuration with the base directories for the files to be packed.
     * @param serviceNamespace The namespace for the service.
     * @param script The script for characterizing the book files. May be null, for no characterization.
     */
    public PubhubPacker(Configuration conf, String serviceNamespace, CharacterizationScriptWrapper script) {
        this.conf = conf;
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
            JAXBContext context = JAXBContext.newInstance(c);
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
        File bookDir = getBookDir(book.getBookId(), book.getBookType());

        JAXBElement<Book> rootElement = null;
        Marshaller marshaller = getMarshallerForClass(book.getClass());
        File bookFile = new File(bookDir, book.getBookId() +  XML_SUFFIX);
        rootElement = new JAXBElement<Book>(new QName(namespace, Book.class.getSimpleName()), 
                Book.class, book);
        marshaller.marshal(rootElement, bookFile);

        for(Image image : book.getImages().getImage()) {
            try {
                log.debug("Retrieving image file for '" + book.getBookId() + "', at " + image.getValue());
                String suffix = StringUtils.getSuffix(image.getValue());
                File imageFile = new File(bookDir, book.getBookId() + "_" + image.getType() + "." + suffix);

                try (OutputStream os = new FileOutputStream(imageFile)) {
                    httpClient.performDownload(os, new URL(image.getValue()));
                }
            } catch (Exception e) {
                log.warn("Failed to download the images '" + image.getValue() + "'. Continues without it.", e);
            }
        }
    }

    /**
     * Packs a file for the book. This is expected to be the content file, either pdf or epub.
     * This makes a symbolic link to the file from the book-folder.
     * It is a prerequisite that the file has the name of the ID.
     * Also, if the file is ignored, if it does not have the pdf or epub suffix.
     * @param bookFile The file for the book.
     * @throws IOException If the book directory cannot be instantiated, or if the symbolic link from the
     * original book file cannot be created.
     */
    public void packFileForAudioBook(File bookFile) throws IOException {
        if(!hasEbookFileSuffix(bookFile)) {
            log.trace("The file '" + bookFile.getAbsolutePath() + "' does not have a ebook suffix.");
            return;
        }
        String id = StringUtils.getPrefix(bookFile.getName());
        log.info("Packaging book file for book-id: " + id);
        File bookDir = getBookDir(id, BookTypeEnum.EBOG);
        File symbolicBookFile = new File(bookDir, bookFile.getName());
        File characterizationOutputFile = new File(bookDir, id + FITS_SUFFIX);
        if(symbolicBookFile.isFile()) {
            log.trace("The symbolic link for the book file for book-id '" + id + "' already exists.");
            runCharacterizationIfNeeded(bookFile, characterizationOutputFile);
        } else {
            Files.createSymbolicLink(symbolicBookFile.toPath(), bookFile.toPath().toAbsolutePath());
            runCharacterizationIfNeeded(bookFile, characterizationOutputFile);
        }
    }

    /**
     * Packs a file for the ebook. This is expected to be the content file in an ebook format 
     * - according to the configured formats (e.g. pdf or epub).
     * 
     * This makes a symbolic link to the file from the ebook folder to the original file.
     * It is a prerequisite that the file has the name of the ID.
     * Also, if the file is ignored, if it does not have an ebook suffix.
     * @param bookFile The file for the ebook.
     * @throws IOException If the book directory cannot be instantiated, or if the symbolic link from the
     * original ebook file cannot be created.
     */
    public void packFileForEbook(File bookFile) throws IOException {
        if(!hasEbookFileSuffix(bookFile)) {
            log.trace("The file '" + bookFile.getAbsolutePath() + "' does not have a ebook suffix.");
            return;
        }
        String id = StringUtils.getPrefix(bookFile.getName());
        log.info("Packaging book file for book-id: " + id);
        File bookDir = getBookDir(id, BookTypeEnum.EBOG);
        File symbolicBookFile = new File(bookDir, bookFile.getName());
        File characterizationOutputFile = new File(bookDir, bookFile.getName() + FITS_SUFFIX);
        if(symbolicBookFile.isFile()) {
            log.trace("The symbolic link for the book file for book-id '" + id + "' already exists.");
            runCharacterizationIfNeeded(bookFile, characterizationOutputFile);
        } else {
            Files.createSymbolicLink(symbolicBookFile.toPath(), bookFile.toPath().toAbsolutePath());
            runCharacterizationIfNeeded(bookFile, characterizationOutputFile);
        }
    }

    /**
     * Packs a file for the audio book. 
     * This is expected to be the content file in an audio format - according to the configured formats (e.g. mp3).
     * 
     * This makes a symbolic link to the file from the audio book folder to the original file.
     * It is a prerequisite that the file has the name of the ID.
     * Also, if the file is ignored, if it does not have an audio book suffix.
     * @param bookFile The file for the audio book.
     * @throws IOException If the book directory cannot be instantiated, or if the symbolic link from the
     * original audio file cannot be created.
     */
    public void packFileForAudio(File bookFile) throws IOException {
        if(!hasAudioFileSuffix(bookFile)) {
            log.trace("The file '" + bookFile.getAbsolutePath() + "' does not have a audio suffix.");
            return;
        }
        String id = StringUtils.getPrefix(bookFile.getName());
        log.info("Packaging book file for book-id: " + id);
        File bookDir = getBookDir(id, BookTypeEnum.LYDBOG);
        File symbolicBookFile = new File(bookDir, bookFile.getName());
        File characterizationOutputFile = new File(bookDir, bookFile.getName() + FITS_SUFFIX);
        if(symbolicBookFile.isFile()) {
            log.trace("The symbolic link for the book file for book-id '" + id + "' already exists.");
            runCharacterizationIfNeeded(bookFile, characterizationOutputFile);
        } else {
            Files.createSymbolicLink(symbolicBookFile.toPath(), bookFile.toPath().toAbsolutePath());
            runCharacterizationIfNeeded(bookFile, characterizationOutputFile);
        }
    }
    
    /**
     * Checks if a file has an ebook suffix as defined in the configuration (e.g. pdf or epub).
     * @param bookFile The file.
     * @return Whether the file has an ebook suffix.
     */
    protected boolean hasEbookFileSuffix(File bookFile) {
        for(String suffix : conf.getEbookFormats()) {
            if(bookFile.getName().endsWith("." + suffix)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if a file has an audio book suffix as defined in the configuration (e.g. mp3).
     * @param bookFile The file.
     * @return Whether the file has an audio book suffix.
     */
    protected boolean hasAudioFileSuffix(File bookFile) {
        for(String suffix : conf.getAudioFormats()) {
            if(bookFile.getName().endsWith("." + suffix)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Runs the characterization if the prerequisites for characterization are met.
     * The prerequisites are, that a characerization scripts was defined in the configuration, 
     * that either the file has not yet been characterized, 
     * or that the file is newer that the output characterization file. 
     * @param inputFile The file to have characterized.
     * @param outputFile The file where the output of the characterization should be placed.
     */
    protected void runCharacterizationIfNeeded(File inputFile, File outputFile) {
        if(characterizationScript != null) {
            if(!outputFile.exists() || 
                    (outputFile.lastModified() < inputFile.lastModified())) {
                // 2 args; 1 for input file path and 1 for output file path.
                characterizationScript.execute(inputFile, outputFile);                    
            } else {
                log.trace("Characterization output file is newer that the file to characterize.");
            }
        } else {
            log.trace("Characterization is turned off.");
        }
    }

    /**
     * Retrieves the directory for the book with the given ID.
     * @param id The ID for the book, whose directory should be retrieved.
     * @param type The type of book (ebook or audio book).
     * @return The directory for the given book id.
     * @throws IOException If the directory cannot be instantiated.
     */
    protected File getBookDir(String id, BookTypeEnum type) throws IOException {
        String path;
        if(type == BookTypeEnum.EBOG) {
            path = conf.getEbookOutputDir().getAbsolutePath() + "/" + id + "/";
        } else if(type == BookTypeEnum.LYDBOG) {
            path = conf.getAudioOutputDir().getAbsolutePath() + "/" + id + "/";
        } else {
            throw new IllegalStateException("Cannot handle unknown BookTypeEnum '" + type + "'.");
        }
        return FileUtils.createDirectory(path);
    }
}
