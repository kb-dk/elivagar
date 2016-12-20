package dk.kb.elivagar.pubhub.validator;

import dk.kb.elivagar.Configuration;

/**
 * Validates the suffix for ebooks.
 */
public class AudioSuffixValidator extends FileSuffixValidator {
    /**
     * Constructor.
     * @param conf The configuration with the valid suffices for the ebooks.
     */
    public AudioSuffixValidator(Configuration conf) {
        super(conf.getAudioFormats());
    }
}
