package williamnogueira.dev.shortener.infra.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.validator.routines.UrlValidator;
import williamnogueira.dev.shortener.infra.exception.InvalidUrlException;

@UtilityClass
public class UrlSanitizer {

    private static final String[] schemes = {"http","https"};
    private static final UrlValidator validator = new UrlValidator(schemes, UrlValidator.ALLOW_2_SLASHES);

    public static String sanitizeUrl(String url) {
        String sanitized = url.trim();

        if (!sanitized.matches("^(?i)http(s)?://.*")) {
            sanitized = "https://" + sanitized;
        }

        if (!validator.isValid(sanitized)) {
            throw new InvalidUrlException("Invalid URL format: " + url);
        }

        if (sanitized.contains("://www.") && sanitized.split("\\.").length < 3) {
            throw new InvalidUrlException("Incomplete URL. Did you mean " + sanitized + ".com?");
        }

        return sanitized;
    }
}
