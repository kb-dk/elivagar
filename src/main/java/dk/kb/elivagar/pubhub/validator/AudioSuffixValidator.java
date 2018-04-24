package dk.kb.elivagar.pubhub.validator;

import dk.kb.elivagar.config.Configuration;

/**
 * Validates the suffix for audio books.
 */
public class AudioSuffixValidator extends FileSuffixValidator {
    /**
     * Constructor.
     * @param conf The configuration with the valid suffixes for the audio books.
     */
    public AudioSuffixValidator(Configuration conf) {
        super(conf.getAudioFormats());
    }
}
