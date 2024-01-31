package com.networknt.schema.resource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Function;

import com.networknt.schema.AbsoluteIri;

/**
 * Map implementation of {@link SchemaLoader}.
 */
public class MapSchemaLoader implements SchemaLoader {
    private final Function<String, String> mappings;

    /**
     * Sets the schema data by absolute IRI.
     * 
     * @param mappings the mappings
     */
    public MapSchemaLoader(Map<String, String> mappings) {
        this(mappings::get);
    }

    /**
     * Sets the schema data by absolute IRI function.
     * 
     * @param mappings the mappings
     */
    public MapSchemaLoader(Function<String, String> mappings) {
        this.mappings = mappings;
    }

    /**
     * Sets the schema data by using two mapping functions.
     * <p>
     * Firstly to map the IRI to an object. If the object is null no mapping is
     * performed.
     * <p>
     * Next to map the object to the schema data.
     * 
     * @param <T>             the type of the object
     * @param mapIriToObject  the mapping of IRI to object
     * @param mapObjectToData the mappingof object to schema data
     */
    public <T> MapSchemaLoader(Function<String, T> mapIriToObject, Function<T, String> mapObjectToData) {
        this.mappings = iri -> {
            T result = mapIriToObject.apply(iri);
            if (result != null) {
                return mapObjectToData.apply(result);
            }
            return null;
        };
    }

    @Override
    public InputStreamSource getSchema(AbsoluteIri absoluteIri) {
        try {
            String result = mappings.apply(absoluteIri.toString());
            if (result != null) {
                return () -> new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            // Do nothing
        }
        return null;
    }
}
