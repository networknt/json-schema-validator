package com.networknt.schema.format;

import java.net.URI;

/**
 * Format for uri-reference.
 */
public class UriReferenceFormat extends AbstractRFC3986Format {
    @Override
    protected boolean validate(URI uri) {
        // Java URI accepts non ASCII characters and this is not a valid in RFC3986
        boolean result = uri.toString().codePoints().allMatch(ch -> ch < 0x7F);
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
    public String getName() {
        return "uri-reference";
    }

    @Override
    public String getMessageKey() {
        return "format.uri-reference";
    }
}
