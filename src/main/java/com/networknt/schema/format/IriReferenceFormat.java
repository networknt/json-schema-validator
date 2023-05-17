package com.networknt.schema.format;

import java.net.URI;

public class IriReferenceFormat extends AbstractRFC3339Format {

    public IriReferenceFormat() {
        super("iri-reference", "must be a valid RFC 3986 IRI-reference");
    }

    @Override
    protected boolean validate(URI uri) {
        return true;
    }

}
