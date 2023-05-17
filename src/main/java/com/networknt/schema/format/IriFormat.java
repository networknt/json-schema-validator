package com.networknt.schema.format;

import java.net.URI;

public class IriFormat extends AbstractRFC3986Format {

    public IriFormat() {
        super("iri", "must be a valid RFC 3987 IRI");
    }

    @Override
    protected boolean validate(URI uri) {
        return uri.isAbsolute();
    }

}
