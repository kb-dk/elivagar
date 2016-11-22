package dk.kb.elivagar.pubhub;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import dk.kb.elivagar.utils.CalendarUtils;
import dk.pubhub.service.ArrayOfBook;
import dk.pubhub.service.ArrayOfBookId;
import dk.pubhub.service.MediaServiceAsmx;
import dk.pubhub.service.MediaServiceAsmxSoap;
import dk.pubhub.service.ModifiedBookIdList;
import dk.pubhub.service.ModifiedBookList;

/**
 * Class for retrieving the data from Pubhub.
 */
public class PubhubRetriever {

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
    public PubhubRetriever(String licenseKey) {
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
     * Retrieves all the BookIDs from PubHub.
     * @return The array of all the book ids.
     */
    public ArrayOfBookId retrieveAllBookIDs() {
        System.out.println("Retrieving all BookIDs");
        return mediaService.listAllBookIds(licenseKey);
    }
    
    /**
     * Retrieves the book ids for the books which has been changed after the given date. 
     * @param earliestDate The date limit for the modified books.
     * @return The list of modified book ids.
     */
    public ModifiedBookIdList retrieveBookIDsAfterModifyDate(Date earliestDate) {
        System.out.println("Retrieving IDs for modified books after date '" + earliestDate + "'.");
        XMLGregorianCalendar xmlDate = CalendarUtils.getXmlGregorianCalendar(earliestDate);
        return mediaService.listModifiedBookIds(licenseKey, xmlDate);
    }

    /**
     * Retrieves all the books.
     * @return Array of all the books.
     */
    public ArrayOfBook downloadAllBooks() {        
        System.out.println("Downloading all books.");
        return mediaService.listAllBooks(licenseKey);
    }
    
    /**
     * Retrieves all the books which have been modified after a given date.
     * @param earliestDate The date limit for the modified books.
     * @return The list of modified books.
     */
    public ModifiedBookList downloadBooksAfterModifyDate(Date earliestDate) {
        System.out.println("Downloading books modified after date '" + earliestDate + "'.");
        XMLGregorianCalendar xmlDate = CalendarUtils.getXmlGregorianCalendar(earliestDate);
        return mediaService.listModifiedBooks(licenseKey, xmlDate);
    }
}
