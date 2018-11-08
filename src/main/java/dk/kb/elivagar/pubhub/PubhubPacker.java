package dk.kb.elivagar.pubhub;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.HttpClient;
import dk.kb.elivagar.characterization.CharacterizationHandler;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.exception.ArgumentCheck;
import dk.kb.elivagar.pubhub.validator.AudioSuffixValidator;
import dk.kb.elivagar.pubhub.validator.EbookSuffixValidator;
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

    /** The Configuration with the base directories for the files to be packed.*/
    protected final Configuration conf;
    /** The namespace of marshalled xml.*/
    protected final String namespace;
    /** The HTTP client for retrieving the extra files for the book, e.g. the images.*/
    protected final HttpClient httpClient;
    /** Map between marshallers and their the classes they marshall.*/
    protected final Map<String, Marshaller> marshallers;
    /** The characterization handler. */
    protected final CharacterizationHandler characterizationHandler;

    /** The suffix validator for audio files.*/
    protected final AudioSuffixValidator audioSuffixValidator;
    /** The suffix validator for ebook files..*/
    protected final EbookSuffixValidator ebookSuffixValidator;
    
    /**
     * Constructor.
     * @param conf The Configuration with the base directories for the files to be packed.
     * @param serviceNamespace The namespace for the service.
     * @param characterizer The characterizer for characterizing the files.
     * @param httpClient The http client.
     */
    public PubhubPacker(Configuration conf, String serviceNamespace, CharacterizationHandler characterizer, 
            HttpClient httpClient) {
        ArgumentCheck.checkNotNull(conf, "Configuration conf");
        ArgumentCheck.checkNotNull(characterizer, "Characterizer characterizer");
        ArgumentCheck.checkNotNullOrEmpty(serviceNamespace, "String serviceNamespace");
        ArgumentCheck.checkNotNull(httpClient, "HttpClient httpClient");
        this.conf = conf;
        this.namespace = serviceNamespace;
        this.marshallers = new HashMap<String, Marshaller>();
        this.httpClient = httpClient;
        this.characterizationHandler = characterizer;
        this.audioSuffixValidator = new AudioSuffixValidator(conf);
        this.ebookSuffixValidator = new EbookSuffixValidator(conf);
    }

    /**
     * Retrieves the marshaller for the given class.
     * This is made to reuse marshallers for each class.
     * @param c The class to marshal.
     * @return The marshaller for the class.
     * @throws JAXBException If a marshaller for the class cannot be created.
     */
    @SuppressWarnings("rawtypes")
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
     * @param book The book to pack.
     * @throws JAXBException If the book cannot be marshalled into an XML file.
     * @throws IOException If an issue occurs when retrieving the directory, or creating or downloading the files.
     */
    public void packBook(Book book) throws JAXBException, IOException {
        ArgumentCheck.checkNotNull(book, "Book book");
        log.info("Packaging book '" + book.getBookId() + "'.");
        File bookDir = getBookDir(book.getBookId(), book.getBookType());

        JAXBElement<Book> rootElement = null;
        Marshaller marshaller = getMarshallerForClass(book.getClass());
        rootElement = new JAXBElement<Book>(new QName(namespace, Book.class.getSimpleName()), 
                Book.class, book);
        
        File bookFile = new File(bookDir, book.getBookId() +  Constants.PUBHUB_METADATA_SUFFIX);
        if(bookFile.exists()) {
            File tempBookFile = new File(bookDir, UUID.randomUUID().toString());
            marshaller.marshal(rootElement, tempBookFile);
            if(FileUtils.areFilesIdentical(bookFile, tempBookFile)) {
                FileUtils.deleteFile(tempBookFile);
                log.debug("Do not pack book '" + book.getBookId() + "'. Identical to latest retrieved version.");
                return;
            } else {
                FileUtils.moveFile(tempBookFile, bookFile);
                log.debug("Overriding metadata for book '" + book.getBookId() + "'.");
            }
        } else {
            marshaller.marshal(rootElement, bookFile);
        }

        for(Image image : book.getImages().getImage()) {
            try {
                log.debug("Retrieving image file for '" + book.getBookId() + "', at " + image.getValue());
                String suffix = StringUtils.getSuffix(image.getValue());
                File imageFile = new File(bookDir, book.getBookId() + "_" + image.getType() + "." + suffix);
                // TODO: if the file already exists, then check last modify through http HEAD

                try (OutputStream os = new FileOutputStream(imageFile)) {
                    httpClient.retrieveUrlContent(image.getValue(), os);
                }
            } catch (Exception e) {
                log.warn("Failed to download the images '" + image.getValue() + "'. Continues without it.", e);
            }
        }
    }

    /**
     * Packs a file for the ebook. This is expected to be the content file in an ebook format 
     * - according to the configured formats (e.g. pdf or epub).
     * 
     * This makes a hard link to the file from the ebook folder to the original file.
     * It is a prerequisite that the file has the name of the ID.
     * Also, if the file is ignored, if it does not have an ebook suffix.
     * @param bookFile The file for the ebook.
     * @throws IOException If the book directory cannot be instantiated, or if the hard link from the
     * original ebook file cannot be created.
     */
    public void packFileForEbook(File bookFile) throws IOException {
        ArgumentCheck.checkNotNull(bookFile, "File bookFile");
        if(!ebookSuffixValidator.hasValidSuffix(bookFile)) {
            log.trace("The file '" + bookFile.getAbsolutePath() + "' does not have a ebook suffix.");
            return;
        }
        String id = StringUtils.getPrefix(bookFile.getName());
        log.info("Packaging book file for book-id: " + id);
        File bookDir = getBookDir(id, BookTypeEnum.EBOG);
        File bookLinkFile = new File(bookDir, bookFile.getName());
        if(bookLinkFile.isFile()) {
            log.trace("The hard link for the book file for book-id '" + id + "' already exists.");
        } else {
            Files.createLink(bookLinkFile.toPath(), bookFile.toPath().toAbsolutePath());
        }
        characterizationHandler.characterize(bookFile, bookDir);
    }

    /**
     * Packs a file for the audio book. 
     * This is expected to be the content file in an audio format - according to the configured formats (e.g. mp3).
     * 
     * This makes a hard link to the file from the audio book folder to the original file.
     * It is a prerequisite that the file has the name of the ID.
     * The file name might be in upper-case, but the ID should be in lower-case, therefore
     * it is lowercased for the directory, the hard link and the characterization file.
     * Also, if the file is ignored, if it does not have an audio book suffix.
     * 
     * This will also perform the characterization, if needed.
     * 
     * @param bookFile The file for the audio book.
     * @throws IOException If the book directory cannot be instantiated, or if the hard link from the
     * original audio file cannot be created.
     */
    public void packFileForAudio(File bookFile) throws IOException {
        ArgumentCheck.checkNotNull(bookFile, "File bookFile");
        if(!audioSuffixValidator.hasValidSuffix(bookFile)) {
            log.trace("The file '" + bookFile.getAbsolutePath() + "' does not have a audio suffix.");
            return;
        }
        String id = StringUtils.getPrefix(bookFile.getName()).toLowerCase(); 
        log.info("Packaging book file for book-id: " + id);
        File bookDir = getBookDir(id, BookTypeEnum.LYDBOG);
        File bookLinkFile = new File(bookDir, bookFile.getName().toLowerCase());
        if(bookLinkFile.isFile()) {
            log.trace("The hard link for the book file for book-id '" + id + "' already exists.");
        } else {
            Files.createLink(bookLinkFile.toPath(), bookFile.toPath().toAbsolutePath());
        }
        characterizationHandler.characterize(bookFile, bookDir);
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
