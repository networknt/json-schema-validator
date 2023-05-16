package com.networknt.schema.format;

import java.net.URI;

public class UriReferenceFormat extends AbstractRFC3339Format {

    public UriReferenceFormat() {
        super("uri-reference", "must be a valid RFC 3986 URI-reference");
    }

    @Override
    protected boolean validate(URI uri) {
        return true;
    }

}
