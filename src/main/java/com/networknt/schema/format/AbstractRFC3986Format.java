package com.networknt.schema.format;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractRFC3986Format extends AbstractFormat {

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

    protected abstract boolean validate(URI uri);

}
