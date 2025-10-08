package com.networknt.schema.resource;

import com.networknt.schema.AbsoluteIri;

/**
 * Prefix implementation of {@link SchemaIdResolver}.
 */
public class PrefixSchemaIdResolver implements SchemaIdResolver {
    private final String source;
    private final String replacement;

    public PrefixSchemaIdResolver(String source, String replacement) {
        this.source = source;
        this.replacement = replacement;
    }

    @Override
    public AbsoluteIri resolve(AbsoluteIri absoluteIRI) {
        String absoluteIRIString = absoluteIRI != null ? absoluteIRI.toString() : null;
        if (absoluteIRIString != null && absoluteIRIString.startsWith(source)) {
            return AbsoluteIri.of(replacement + absoluteIRIString.substring(source.length()));
        }
        return null;
    }
}
