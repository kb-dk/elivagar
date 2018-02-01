package dk.kb.elivagar.pubhub;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.exception.ArgumentCheck;
import dk.kb.elivagar.utils.CalendarUtils;
import dk.pubhub.service.ArrayOfBook;
import dk.pubhub.service.MediaServiceAsmx;
import dk.pubhub.service.MediaServiceAsmxSoap;
import dk.pubhub.service.ModifiedBookList;

/**
 * Class for retrieving the data from Pubhub.
 */
public class PubhubMetadataRetriever {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(PubhubMetadataRetriever.class);

    /** The license key for pubhub.*/
    protected final String licenseKey;
    
    /** The media service for using the SOAP API of pubhub.*/
    protected final MediaServiceAsmxSoap mediaService;
    /** The namespace for the service.*/
    protected final String serviceNS;
    
    /**
     * Constructor.
     * @param licenseKey The license key for pubhub.
     */
    public PubhubMetadataRetriever(String licenseKey) {
        ArgumentCheck.checkNotNullOrEmpty(licenseKey, "String licenseKey");
        this.licenseKey = licenseKey;
        
        MediaServiceAsmx mediaServiceAsmx = new MediaServiceAsmx();
        QName serviceName = mediaServiceAsmx.getServiceName();
        serviceNS = serviceName.getNamespaceURI();
        mediaService = mediaServiceAsmx.getMediaServiceAsmxSoap();
    }
    
    /**
     * @return The namespace of the service.
     */
    public String getServiceNamespace() {
        return serviceNS;
    }

    /**
     * Retrieves the metadata for all the books.
     * @return Array of all the book metadata.
     */
    public ArrayOfBook downloadAllBookMetadata() {        
        log.info("Downloading the metadata for all the books.");
        return mediaService.listAllBooks(licenseKey);
    }
    
    /**
     * Retrieves all the metadata for the books which have been modified after a given date.
     * @param earliestDate The date limit for the modified books. If null, then all books newer than epoch is returned.
     * @return The list of modified book metadata.
     */
    public ModifiedBookList downloadBookMetadataAfterModifyDate(Date earliestDate) {
        log.info("Downloading the metadata for the books modified after date '" + earliestDate + "'.");
        XMLGregorianCalendar xmlDate = CalendarUtils.getXmlGregorianCalendar(earliestDate);
        return mediaService.listModifiedBooks(licenseKey, xmlDate);
    }
}
