package com.networknt.schema.format;

import java.net.URI;

public class IriFormat extends AbstractRFC3339Format {

    public IriFormat() {
        super("iri", "must be a valid RFC 3986 IRI");
    }

    @Override
    protected boolean validate(URI uri) {
        return uri.isAbsolute();
    }

}
