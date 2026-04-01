package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.networknt.schema.dialect.Dialects;

/**
 * Test support for non-finite values (Infinity, -Infinity, and NaN) in numeric validations.
 */
public class NonFiniteTest {
    private final static SchemaRegistry REGISTRY = SchemaRegistry.withDialect(Dialects.getDraft202012());
    private final static String YAML_STRING =
    """
    ---
    - 10.0
    - 50.0
    - .inf
    - -.inf
    - .nan
    """;

    @Test
    public void nonFiniteConst() {
        final String schemaString =
        """
        $schema: https://json-schema.org/draft/2020-12/schema
        type: array
        items:
            type: number
            not:
                anyOf:
                    - { const:  .inf }
                    - { const: -.inf }
                    - { const:  .nan }
        """;
        Schema      schema = REGISTRY.getSchema(schemaString, InputFormat.YAML);
        List<Error> errors = schema.validate(YAML_STRING, InputFormat.YAML);
        assertEquals(3, errors.size());
    }

    @Test
    public void nonFiniteEnum() {
        final String schemaString =
        """
        $schema: https://json-schema.org/draft/2020-12/schema
        type: array
        items:
            type: number
            not:
                enum:
                    -  .inf
                    - -.inf
                    -  .nan
        """;
        Schema      schema = REGISTRY.getSchema(schemaString, InputFormat.YAML);
        List<Error> errors = schema.validate(YAML_STRING, InputFormat.YAML);
        assertEquals(3, errors.size());
    }

    @Test
    public void nonFiniteDataMinMax() {
        final String schemaString =
        """
        $schema: https://json-schema.org/draft/2020-12/schema
        type: array
        items:
            type: number
            minimum: 0
            maximum: 100
        """;
        Schema      schema = REGISTRY.getSchema(schemaString, InputFormat.YAML);
        List<Error> errors = schema.validate(YAML_STRING, InputFormat.YAML);
        assertEquals(4, errors.size());
    }

    @Test
    public void nonFiniteDataExclusiveMinMax() {
        final String schemaString =
        """
        $schema: https://json-schema.org/draft/2020-12/schema
        type: array
        items:
            type: number
            exclusiveMinimum: 0
            exclusiveMaximum: 100
        """;
        Schema      schema = REGISTRY.getSchema(schemaString, InputFormat.YAML);
        List<Error> errors = schema.validate(YAML_STRING, InputFormat.YAML);
        assertEquals(4, errors.size());
    }

    @Test
    public void nonFiniteDataMultipleOf() {
        final String schemaString =
        """
        $schema: https://json-schema.org/draft/2020-12/schema
        type: array
        items:
            type: number
            multipleOf: 10.0
        """;
        Schema      schema = REGISTRY.getSchema(schemaString, InputFormat.YAML);
        List<Error> errors = schema.validate(YAML_STRING, InputFormat.YAML);
        assertEquals(3, errors.size());
    }
}
