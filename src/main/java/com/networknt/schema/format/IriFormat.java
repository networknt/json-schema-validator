package com.networknt.schema.format;

import java.net.URI;

/**
 * IriFormat.
 */
public class IriFormat extends AbstractRFC3986Format {
    public IriFormat() {
        super("iri", "must be a valid RFC 3987 IRI");
    }

    @Override
    protected boolean validate(URI uri) {
        boolean result = uri.isAbsolute();
        if (result) {
            String authority = uri.getAuthority();
            if (authority != null) {
                if (IPv6Format.PATTERN.matcher(authority).matches() ) {
                    return false;
                }
            }
        }
        return result;
    }
}
