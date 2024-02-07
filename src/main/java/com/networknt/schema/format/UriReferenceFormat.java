package com.networknt.schema.format;

import java.net.URI;

/**
 * Format for uri-reference.
 */
public class UriReferenceFormat extends AbstractRFC3986Format {
    @Override
    protected boolean validate(URI uri) {
        return true;
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
