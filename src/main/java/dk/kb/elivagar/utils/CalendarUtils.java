package dk.kb.elivagar.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for dealing with dates and calendars. Especially converting to and from the date XML date format.
 */
public class CalendarUtils {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(CalendarUtils.class);

    /** Our default date format.*/
    public static final String DEFAULT_DATE_FORMAT = "YYYYMMdd-hhmmss";
    
    /**
     * Turns a date into a XMLGregorianCalendar.
     * @param date The date. If the argument is null, then epoch is returned.
     * @return The XMLGregorianCalendar.
     */
    public static XMLGregorianCalendar getXmlGregorianCalendar(Date date) {
        if(date == null) {
            log.debug("Cannot convert a null date. Returning epoch instead.");
            date = new Date(0);
        }
        
        GregorianCalendar gc = new GregorianCalendar();
        try {
            gc.setTime(date);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        } catch (Exception e) {
            throw new IllegalStateException("Could not convert the date '" + date + "' into the xml format.", e);
        }
    }
    
    /**
     * Returns the date in text format (
     * @param date The date to convert into a string.
     * @return The text version of the date.
     */
    public static String getDateAsString(Date date) {
        DateFormat format = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        return format.format(date);
    }
}
