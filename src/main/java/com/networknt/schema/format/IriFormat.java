package com.networknt.schema.format;

import java.net.URI;

/**
 * Format for iri.
 */
public class IriFormat extends AbstractRFC3986Format {
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

            String query = uri.getRawQuery();
            if (query != null) {
                // [ and ] must be percent encoded
                if (query.indexOf('[') != -1 || query.indexOf(']') != -1) {
                    return false;
                }
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "iri";
    }
    
    @Override
    public String getMessageKey() {
        return "format.iri";
    }
}
