package dk.kb.elivagar.pubhub.validator;

import dk.kb.elivagar.config.Configuration;

/**
 * Validates the suffix for ebooks.
 */
public class EbookSuffixValidator extends FileSuffixValidator {
    /**
     * Constructor.
     * @param conf The configuration with the valid suffixes for the ebooks.
     */
    public EbookSuffixValidator(Configuration conf) {
        super(conf.getEbookFormats());
    }

}
