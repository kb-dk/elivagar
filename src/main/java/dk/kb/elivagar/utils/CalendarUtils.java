package dk.kb.elivagar.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.kb.elivagar.exception.ArgumentCheck;

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
     * Transforms a Date object into text.
     * @param date The date to convert into a string.
     * @return The text version of the date.
     */
    public static String getDateAsString(Date date) {
        ArgumentCheck.checkNotNull(date, "Date date");
        
        DateFormat format = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        return format.format(date);
    }
    
    /**
     * Transforms a text date into a date object.
     * @param date The date in text.
     * @param format The format for the date in the text.
     * @return The date.
     * @throws ParseException If it cannot transform the date.
     */
    public static Date getDateFromString(String date, String format) throws ParseException {
        ArgumentCheck.checkNotNull(date, "String date");
        ArgumentCheck.checkNotNull(format, "String format");
        
        DateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        return dateFormat.parse(date);
    }
}
