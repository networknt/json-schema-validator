package com.networknt.schema.uri;

import java.util.Map;

import com.networknt.schema.AbsoluteIri;

/**
 * Map implementation of {@link SchemaMapper}.
 */
public class MapAbsoluteIriMapper implements SchemaMapper {
    private final Map<String, String> mappings;
    
    public MapAbsoluteIriMapper(Map<String, String> mappings) {
        this.mappings = mappings;
    }

    @Override
    public AbsoluteIri map(AbsoluteIri absoluteIRI) {
        String mapped = this.mappings.get(absoluteIRI.toString());
        if (mapped != null) {
            return AbsoluteIri.of(mapped);
        }
        return null;
    }

}
