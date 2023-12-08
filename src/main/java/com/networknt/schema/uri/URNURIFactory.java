package com.networknt.schema.uri;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * A URIFactory that handles "urn" scheme of {@link URI}s.
 */
public final class URNURIFactory implements URIFactory {

    public static final String SCHEME = "urn";

    @Override
    public URI create(final String uri) {
        try {
            return URI.create(uri);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unable to create URI.", e);
        }
    }

    @Override
    public URI create(final URI baseURI, final String segment) {
        String urnPart = baseURI.getRawSchemeSpecificPart();
        int pos = urnPart.indexOf(':');
        String namespace = pos < 0 ? urnPart : urnPart.substring(0, pos);
        return URI.create(SCHEME + ":" + namespace + ":" + segment);
    }
}
