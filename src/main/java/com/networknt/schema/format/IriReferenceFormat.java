package com.networknt.schema.format;

import java.net.URI;

/**
 * Format for iri-reference.
 */
public class IriReferenceFormat extends AbstractRFC3986Format {
    @Override
    protected boolean validate(URI uri) {
        String authority = uri.getAuthority();
        if (authority != null) {
            if (IPv6Format.PATTERN.matcher(authority).matches() ) {
                return false;
            }
        }
        String query = uri.getRawQuery();
        if (query != null) {
            // [ and ] must be percent encoded
	        return query.indexOf('[') == -1 && query.indexOf(']') == -1;
        }
        return true;
    }

    @Override
    public String getName() {
        return "iri-reference";
    }
    
    @Override
    public String getMessageKey() {
        return "format.iri-reference";
    }

}
