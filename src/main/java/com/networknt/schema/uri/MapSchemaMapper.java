package com.networknt.schema.uri;

import java.util.Map;
import java.util.function.Function;

import com.networknt.schema.AbsoluteIri;

/**
 * Map implementation of {@link SchemaMapper}.
 */
public class MapSchemaMapper implements SchemaMapper {
    private final Function<String, String> mappings;
    
    public MapSchemaMapper(Map<String, String> mappings) {
        this(mappings::get);
    }
    
    public MapSchemaMapper(Function<String, String> mappings) {
        this.mappings = mappings;
    }
 
    @Override
    public AbsoluteIri map(AbsoluteIri absoluteIRI) {
        String mapped = this.mappings.apply(absoluteIRI.toString());
        if (mapped != null) {
            return AbsoluteIri.of(mapped);
        }
        return null;
    }

}
