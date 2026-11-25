package com.networknt.schema.format;

import java.net.URI;

/**
 * Format for uri-reference.
 */
public class UriReferenceFormat extends AbstractRFC3986Format {
    @Override
    protected boolean validate(URI uri) {
        // Java URI accepts non ASCII characters and this is not a valid in RFC3986
        return uri.toString().codePoints().allMatch(ch -> ch < 0x7F);
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
