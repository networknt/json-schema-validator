package com.networknt.schema.format;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Format;

/**
 * {@link AbstractFormat} for RFC 3986.
 */
public abstract class AbstractRFC3986Format implements Format {
    private static final Pattern VALID = Pattern.compile("([A-Za-z0-9+-\\.]*:)?//|[A-Za-z0-9+-\\.]+:");

    @Override
    public final boolean matches(ExecutionContext executionContext, String value) {
        try {
            URI uri = new URI(value);
            return validate(uri);
        } catch (URISyntaxException e) {
            return handleException(e);
        }
    }

    /**
     * Determines if the uri matches the format.
     * 
     * @param uri the uri to match
     * @return true if matches
     */
    protected abstract boolean validate(URI uri);

    /**
     * Determines if the uri matches the format.
     *
     * @param e the URISyntaxException
     * @return false if it does not match
     */
    protected boolean handleException(URISyntaxException e) {
	    return VALID.matcher(e.getInput()).matches();
    }
}
