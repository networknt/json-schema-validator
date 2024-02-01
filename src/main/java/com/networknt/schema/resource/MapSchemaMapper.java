package com.networknt.schema.resource;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

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

    /**
     * Apply the mapping function if the predicate is true.
     * 
     * @param test     the predicate
     * @param mappings the mapping
     */
    public MapSchemaMapper(Predicate<String> test, Function<String, String> mappings) {
        this.mappings = iri -> {
            if (test.test(iri)) {
                return mappings.apply(iri);
            }
            return null;
        };
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
