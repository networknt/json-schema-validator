package com.networknt.schema.format;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * {@link AbstractFormat} for RFC 3986.
 */
public abstract class AbstractRFC3986Format extends AbstractFormat {

    /**
     * Constructor.
     * 
     * @param name                    the format name
     * @param errorMessageDescription the error message description
     */
    public AbstractRFC3986Format(String name, String errorMessageDescription) {
        super(name, errorMessageDescription);
    }

    @Override
    public final boolean matches(String value) {
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
