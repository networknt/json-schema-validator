package com.networknt.schema.format;

import java.net.URI;
import java.net.URISyntaxException;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Format;

/**
 * {@link AbstractFormat} for RFC 3986.
 */
public abstract class AbstractRFC3986Format implements Format {
    @Override
    public final boolean matches(ExecutionContext executionContext, String value) {
        try {
            URI uri = new URI(value);
            return validate(uri);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Determines if the uri matches the format.
     * 
     * @param uri the uri to match
     * @return true if matches
     */
    protected abstract boolean validate(URI uri);

}
