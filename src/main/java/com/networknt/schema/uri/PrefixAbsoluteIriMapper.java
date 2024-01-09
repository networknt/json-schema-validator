package com.networknt.schema.uri;

import com.networknt.schema.AbsoluteIri;

/**
 * Prefix implementation of {@link AbsoluteIriMapper}.
 */
public class PrefixAbsoluteIriMapper implements AbsoluteIriMapper {
    private final String source;
    private final String replacement;

    public PrefixAbsoluteIriMapper(String source, String replacement) {
        this.source = source;
        this.replacement = replacement;
    }

    @Override
    public AbsoluteIri map(AbsoluteIri absoluteIRI) {
        String absoluteIRIString = absoluteIRI != null ? absoluteIRI.toString() : null;
        if (absoluteIRIString != null && absoluteIRIString.startsWith(source)) {
            return AbsoluteIri.of(replacement + absoluteIRIString.substring(source.length()));
        }
        return null;
    }
}
