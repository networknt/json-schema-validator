package com.networknt.schema.uri;

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
    
    public MapSchemaLoader(Map<String, String> mappings) {
        this(mappings::get);
    }
    
    public MapSchemaLoader(Function<String, String> mappings) {
        this.mappings = mappings;
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
