package dk.kb.elivagar.statistics;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.kb.elivagar.Constants;
import dk.kb.elivagar.config.Configuration;
import dk.kb.elivagar.exception.ArgumentCheck;

/**
 * Class for calculating the statistics for the books retrieved from pubhub. 
 */
public class ElivagarStatistics {
    /** The logger.*/
    private static final Logger log = LoggerFactory.getLogger(ElivagarStatistics.class);

    /** The number of directories traversed. */
    protected long totalCount;
    /** The number of new directories traversed.*/
    protected long newDirCount;
    /** The number of non-standard named items found.*/
    protected long numberOfOtherCount;
    /** The mapping between file suffixes and the number of files with given suffix.*/
    protected SuffixMap numberOfFiles;
    /** The mapping between file suffixes and the number of new files with the given suffix.*/
    protected SuffixMap numberOfNewFiles;

    /** The configuration.*/
    protected final Configuration conf;

    /**
     * Constructor.
     * @param conf The configuration.
     */
    public ElivagarStatistics(Configuration conf) {
        this.conf = conf;
        totalCount = 0l;
        newDirCount = 0l;
        numberOfOtherCount = 0l;
        numberOfFiles = new SuffixMap();
        numberOfNewFiles = new SuffixMap();
    }

    /**
     * Traverses the given base directory, containing the books directories (either audio books or ebooks).
     * @param baseDir The base directory.
     * @param date The date in millis from epoch, where everything with a newer date is considered 'new'.
     */
    public void traverseBaseDir(File baseDir, long date) {
        ArgumentCheck.checkNotNull(baseDir, "File baseDir");
        File[] directories = baseDir.listFiles();
        if(directories == null) {
            throw new IllegalStateException("No directories at '" + baseDir.getAbsolutePath() 
            + "' to make statistics on.");
        } else {
            log.info("Calculating the statistics on the books in directory '" + baseDir.getAbsolutePath()
            + "'. Expecting '" + directories.length + "' books.");
            for(File dir : directories) {
                calculateStatisticsOnBookDir(dir, date);
            }
        }
    }

    /**
     * Calculates the statistics on a specific book directory.
     * For each file in the directory, it increments the number of times the suffix of the given file is encountered.
     * It also counts the number of new files and directories, and also the number of files, which does
     * not follow the naming scheme ('id'/'id'.suffix).
     * @param dir The directory to calculate the statistics upon.
     * @param date The date in millis since epoch.
     */
    protected void calculateStatisticsOnBookDir(File dir, long date) {
        File[] files = dir.listFiles();
        if(files == null) {
            log.warn("Expected the directory '" + dir.getAbsolutePath() + "' to be a directory for a book. "
                    + "Continue to next.");
        } else {
            totalCount++;
            checkNewDirectory(dir, date);
            String dirName = dir.getName();

            for(File f : files) {
                String filename = f.getName();
                if(filename.startsWith(dirName)) {
                    String suffix = filename.replace(dirName, "");
                    numberOfFiles.addSuffix(suffix);
                    if(f.lastModified() > date) {
                        numberOfNewFiles.addSuffix(suffix);
                    }
                } else {
                    numberOfOtherCount++;
                }
            }
        }
    }

    /**
     * Checks whether a given directory was modified at a newer date than the given date.
     * If so, then it is added to the list of new directories.
     * @param dir The directory for check.
     * @param date The date to check against.
     */
    protected void checkNewDirectory(File dir, long date) {
        if(dir.lastModified() > date) {
            newDirCount++;
        }
    }

    /** @return The number of directories traversed. */
    public long getTotalCount() {
        return totalCount;
    }

    /** @return The number of new directories traversed.*/
    public long getNewDirCount() {
        return newDirCount;
    }

    /** @return The number of non-standard named files encountered.*/
    public long getNonStandardNamedCount() {
        return numberOfOtherCount;
    }

    /** @return The suffixes map for all files.*/
    public SuffixMap getMapOfFileSuffixes() {
        return numberOfFiles;
    }

    /** @return The suffixes map for the new files.*/
    public SuffixMap getMapOfNewFileSuffixes() {
        return numberOfNewFiles;
    }

    /**
     * Prints the statistics to the print-stream.
     * @param printer The printstream where the statistics will be printed.
     */
    public void printStatistics(PrintStream printer) {
        ArgumentCheck.checkNotNull(printer, "PrintStream printer");
        ArgumentCheck.checkNotNull(conf, "Configuration conf");

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("elivagar");
            doc.appendChild(rootElement);

            addXmlElementsForDirsTraversed(rootElement, doc);
            addXmlElementsForFormats(rootElement, doc);
            addXmlElementsForMetadataFormats(rootElement, doc);
            addXmlElementsForOtherSuffices(rootElement, doc);

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(printer);

            transformer.transform(source, result);
        } catch (Exception e) {
            throw new IllegalStateException("Could not create the statistics", e);
        }
    }
    
    /**
     * Adds the statistics for the amount of directories traversed.
     * @param root The root element.
     * @param doc The document.
     */
    protected void addXmlElementsForDirsTraversed(Element root, Document doc) {
        Element dirElement = doc.createElement("directories_traversed");
        root.appendChild(dirElement);
        
        Element totalField = doc.createElement("total");
        dirElement.appendChild(totalField);
        totalField.appendChild(doc.createTextNode("" + getTotalCount()));
        Element newField = doc.createElement("new");
        dirElement.appendChild(newField);
        newField.appendChild(doc.createTextNode("" + getNewDirCount()));
        Element nonStandardField = doc.createElement("nonStandard");
        dirElement.appendChild(nonStandardField);
        nonStandardField.appendChild(doc.createTextNode("" + getNonStandardNamedCount()));
    }
    
    /**
     * Adds the statistics for the different formats for the e-books and audio books.
     * @param root The root element.
     * @param doc The document.
     */
    protected void addXmlElementsForFormats(Element root, Document doc) {
        Element formatsElement = doc.createElement("formats");
        root.appendChild(formatsElement);

        Element ebookField = doc.createElement("ebooks");
        formatsElement.appendChild(ebookField);
        Element totalEbookField = doc.createElement("total");
        ebookField.appendChild(totalEbookField);
        totalEbookField.appendChild(doc.createTextNode("" + getMapOfFileSuffixes().getMultiKeyCount(
                conf.getEbookFormats())));
        Element newEbookField = doc.createElement("new");
        ebookField.appendChild(newEbookField);
        newEbookField.appendChild(doc.createTextNode("" + getMapOfNewFileSuffixes().getMultiKeyCount(
                conf.getEbookFormats())));

        for(String ebookFormat : conf.getEbookFormats()) {
            String suffix = "." + ebookFormat;

            Element suffixField = doc.createElement(ebookFormat);
            ebookField.appendChild(suffixField);

            Element totalSuffixField = doc.createElement("total");
            suffixField.appendChild(totalSuffixField);
            totalSuffixField.appendChild(doc.createTextNode("" + getMapOfFileSuffixes().getValue(suffix)));

            Element newSuffixField = doc.createElement("new");
            suffixField.appendChild(newSuffixField);
            newSuffixField.appendChild(doc.createTextNode("" + getMapOfNewFileSuffixes().getValue(suffix)));
        }

        Element audioField = doc.createElement("audio");
        formatsElement.appendChild(audioField);
        Element totalAudioField = doc.createElement("total");
        audioField.appendChild(totalAudioField);
        totalAudioField.appendChild(doc.createTextNode("" + getMapOfFileSuffixes().getMultiKeyCount(
                conf.getAudioFormats())));
        Element newAudioField = doc.createElement("new");
        audioField.appendChild(newAudioField);
        newAudioField.appendChild(doc.createTextNode("" + getMapOfNewFileSuffixes().getMultiKeyCount(
                conf.getAudioFormats())));

        for(String audioFormat : conf.getAudioFormats()) {
            String suffix = "." + audioFormat;

            Element suffixField = doc.createElement(audioFormat);
            audioField.appendChild(suffixField);

            Element totalSuffixField = doc.createElement("total");
            suffixField.appendChild(totalSuffixField);
            totalSuffixField.appendChild(doc.createTextNode("" + getMapOfFileSuffixes().getValue(suffix)));

            Element newSuffixField = doc.createElement("new");
            suffixField.appendChild(newSuffixField);
            newSuffixField.appendChild(doc.createTextNode("" + getMapOfNewFileSuffixes().getValue(suffix)));
        }
    }
    
    /**
     * Adds the counts for the metadata files.
     * @param root The root element.
     * @param doc The document.
     */
    protected void addXmlElementsForMetadataFormats(Element root, Document doc) {
        Element metadataElement = doc.createElement("metadata");
        root.appendChild(metadataElement);
        
        Element pubhubElement = doc.createElement("pubhub");
        metadataElement.appendChild(pubhubElement);
        Element totalPubhubField = doc.createElement("total");
        pubhubElement.appendChild(totalPubhubField);
        totalPubhubField.appendChild(doc.createTextNode("" + getMapOfFileSuffixes().getValuesEndingWithKey(
                Constants.PUBHUB_METADATA_SUFFIX)));
        Element newPubhubField = doc.createElement("new");
        pubhubElement.appendChild(newPubhubField);
        newPubhubField.appendChild(doc.createTextNode("" + getMapOfNewFileSuffixes().getValuesEndingWithKey(
                Constants.PUBHUB_METADATA_SUFFIX)));
        
        Element modsElement = doc.createElement("mods");
        metadataElement.appendChild(modsElement);
        Element totalModsField = doc.createElement("total");
        modsElement.appendChild(totalModsField);
        totalModsField.appendChild(doc.createTextNode("" + getMapOfFileSuffixes().getValuesEndingWithKey(
                Constants.MODS_METADATA_SUFFIX)));
        Element newModsField = doc.createElement("new");
        modsElement.appendChild(newModsField);
        newModsField.appendChild(doc.createTextNode("" + getMapOfNewFileSuffixes().getValuesEndingWithKey(
                Constants.MODS_METADATA_SUFFIX)));

        Element fitsElement = doc.createElement("fits");
        metadataElement.appendChild(fitsElement);
        Element totalFitsField = doc.createElement("total");
        fitsElement.appendChild(totalFitsField);
        totalFitsField.appendChild(doc.createTextNode("" + getMapOfFileSuffixes().getValuesEndingWithKey(
                Constants.FITS_METADATA_SUFFIX)));
        Element newFitsField = doc.createElement("new");
        fitsElement.appendChild(newFitsField);
        newFitsField.appendChild(doc.createTextNode("" + getMapOfNewFileSuffixes().getValuesEndingWithKey(
                Constants.FITS_METADATA_SUFFIX)));
        
        Element epubcheckElement = doc.createElement("epubcheck");
        metadataElement.appendChild(epubcheckElement);
        Element totalEpubcheckField = doc.createElement("total");
        epubcheckElement.appendChild(totalEpubcheckField);
        totalEpubcheckField.appendChild(doc.createTextNode("" + getMapOfFileSuffixes().getValuesEndingWithKey(
                Constants.EPUBCHECK_METADATA_SUFFIX)));
        Element newEpubcheckField = doc.createElement("new");
        epubcheckElement.appendChild(newEpubcheckField);
        newEpubcheckField.appendChild(doc.createTextNode("" + getMapOfNewFileSuffixes().getValuesEndingWithKey(
                Constants.EPUBCHECK_METADATA_SUFFIX)));
    }
    
    /**
     * Adds the statistics for the amount of directories traversed.
     * @param root The root element.
     * @param doc The document.
     */
    protected void addXmlElementsForOtherSuffices(Element root, Document doc) {
        List<String> suffixes = new ArrayList<String>();
        suffixes.addAll(conf.getAudioFormats());
        for(String suffix : conf.getAudioFormats()) {
            suffixes.add("." + suffix);
            suffixes.add("." + suffix + Constants.FITS_METADATA_SUFFIX);
        }
        suffixes.addAll(conf.getEbookFormats());
        for(String suffix : conf.getEbookFormats()) {
            suffixes.add("." + suffix);
            suffixes.add("." + suffix + Constants.FITS_METADATA_SUFFIX);
            suffixes.add("." + suffix + Constants.EPUBCHECK_METADATA_SUFFIX);
        }
        suffixes.add(Constants.PUBHUB_METADATA_SUFFIX);
        suffixes.add(Constants.MODS_METADATA_SUFFIX);
        suffixes.add(Constants.FITS_METADATA_SUFFIX);
        suffixes.add(Constants.EPUBCHECK_METADATA_SUFFIX);

        Element otherElement = doc.createElement("other");
        root.appendChild(otherElement);
        Element totalOtherField = doc.createElement("total");
        otherElement.appendChild(totalOtherField);
        totalOtherField.appendChild(doc.createTextNode("" + getMapOfFileSuffixes().getCountExcludingKeys(suffixes)));
        Element newOtherField = doc.createElement("new");
        otherElement.appendChild(newOtherField);
        newOtherField.appendChild(doc.createTextNode("" + getMapOfNewFileSuffixes().getCountExcludingKeys(suffixes)));

        for(String otherSuffix : getMapOfFileSuffixes().getMissingKeys(suffixes)) {

            Element suffixField = doc.createElement(otherSuffix);
            otherElement.appendChild(suffixField);

            Element totalSuffixField = doc.createElement("total");
            suffixField.appendChild(totalSuffixField);
            totalSuffixField.appendChild(doc.createTextNode("" + getMapOfFileSuffixes().getValue(otherSuffix)));

            Element newSuffixField = doc.createElement("new");
            suffixField.appendChild(newSuffixField);
            newSuffixField.appendChild(doc.createTextNode("" + getMapOfNewFileSuffixes().getValue(otherSuffix)));
        }
    }
}
