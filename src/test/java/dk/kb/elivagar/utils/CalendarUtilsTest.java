package dk.kb.elivagar.utils;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CalendarUtilsTest extends ExtendedTestCase {

    XMLGregorianCalendar xmlEpoc = CalendarUtils.getXmlGregorianCalendar(new Date(0));
    
    @Test
    public void testXmlDateForEpoc() throws Exception {
        XMLGregorianCalendar date = CalendarUtils.getXmlGregorianCalendar(new Date(0));
        Assert.assertEquals(date, xmlEpoc);
    }
    
    @Test
    public void testXmlDateForNull() throws Exception {
        XMLGregorianCalendar date = CalendarUtils.getXmlGregorianCalendar(null);
        Assert.assertEquals(date, xmlEpoc);
    }
    
    @Test
    public void testDateConvertedFromXmlEpoc() throws Exception {
        Date date = CalendarUtils.getDateFromXmlGregorianCalendar(xmlEpoc);
        Assert.assertEquals(date.getTime(), 0);
    }
    
    @Test
    public void testConstructor() {
        new CalendarUtils();
    }
}
