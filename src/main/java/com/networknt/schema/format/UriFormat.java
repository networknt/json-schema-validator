package com.networknt.schema.format;

import java.net.URI;

public class UriFormat extends AbstractRFC3986Format {

    public UriFormat() {
        super("uri", "must be a valid RFC 3986 URI");
    }

    @Override
    protected boolean validate(URI uri) {
        return uri.isAbsolute();
    }

}
