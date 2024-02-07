package com.networknt.schema.format;

import java.net.URI;

/**
 * Format for uri. 
 */
public class UriFormat extends AbstractRFC3986Format {
    @Override
    protected boolean validate(URI uri) {
        return uri.isAbsolute();
    }

    @Override
    public String getName() {
        return "uri";
    }

    @Override
    public String getMessageKey() {
        return "format.uri";
    }
}
