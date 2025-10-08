package com.networknt.schema.resource;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import com.networknt.schema.AbsoluteIri;

/**
 * Map implementation of {@link SchemaIdResolver}.
 */
public class MapSchemaIdResolver implements SchemaIdResolver {
    private final Function<String, String> mappings;
    
    public MapSchemaIdResolver(Map<String, String> mappings) {
        this(mappings::get);
    }

    public MapSchemaIdResolver(Function<String, String> mappings) {
        this.mappings = mappings;
    }

    /**
     * Apply the mapping function if the predicate is true.
     * 
     * @param test     the predicate
     * @param mappings the mapping
     */
    public MapSchemaIdResolver(Predicate<String> test, Function<String, String> mappings) {
        this.mappings = iri -> {
            if (test.test(iri)) {
                return mappings.apply(iri);
            }
            return null;
        };
    }

    @Override
    public AbsoluteIri resolve(AbsoluteIri absoluteIRI) {
        String mapped = this.mappings.apply(absoluteIRI.toString());
        if (mapped != null) {
            return AbsoluteIri.of(mapped);
        }
        return null;
    }

}
