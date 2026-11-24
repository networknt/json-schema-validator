package com.networknt.schema.format;

import java.net.URI;

/**
 * Format for iri.
 */
public class IriFormat extends AbstractRFC3986Format {
    @Override
    protected boolean validate(URI uri) {
        return !containsBidiFormatting(uri.toString()) && uri.isAbsolute();
    }

    @Override
    protected boolean isInternationalized() {
        return true;
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
