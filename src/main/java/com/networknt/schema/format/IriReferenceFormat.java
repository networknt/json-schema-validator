package com.networknt.schema.format;

import java.net.URI;

public class IriReferenceFormat extends AbstractRFC3986Format {

    public IriReferenceFormat() {
        super("iri-reference", "must be a valid RFC 3987 IRI-reference");
    }

    @Override
    protected boolean validate(URI uri) {
        return true;
    }

}
