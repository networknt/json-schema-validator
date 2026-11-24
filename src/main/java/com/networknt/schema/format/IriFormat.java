package com.networknt.schema.format;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Format for iri.
 */
public class IriFormat extends AbstractRFC3986Format {
    @Override
    protected boolean validate(URI uri) {
        if (containsBidiFormatting(uri.toString())) {
            return false;
        }
        boolean result = uri.isAbsolute();
        if (result) {
            String query = uri.getRawQuery();
            if (query != null) {
                // [ and ] must be percent encoded
                if (query.indexOf('[') != -1 || query.indexOf(']') != -1) {
                    return false;
                }
            }
        }
        return result;
    }

    @Override
    protected boolean isInternationalized() {
        return true;
    }

    @Override
    protected boolean handleException(URISyntaxException e) {
        // RFC 3986 section 4.3: an absolute URI starts with a scheme, so "//" is only a valid reference
        return super.handleException(e) && !e.getInput().startsWith("//");
    }

    @Override
    public String getName() {
        return "iri";
    }
    
    @Override
    public String getMessageKey() {
        return "format.iri";
    }
}
