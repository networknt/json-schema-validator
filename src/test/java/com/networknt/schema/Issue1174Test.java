package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.node.JsonNodeFactory;

class Issue1174Test {
    @Test
    void textNodeShouldNotBeAcceptedAsRootSchema() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);

        SchemaException exception = assertThrows(SchemaException.class,
                () -> registry.getSchema(JsonNodeFactory.instance.textNode("false")));

        assertTrue(exception.getMessage().contains("must be object or boolean"));
        assertTrue(exception.getMessage().contains("STRING"));
    }

    @Test
    void textNodeContainingJsonShouldNotBeAcceptedAsRootSchema() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);

        SchemaException exception = assertThrows(SchemaException.class,
                () -> registry.getSchema(JsonNodeFactory.instance.textNode("{\"type\":\"string\"}")));

        assertTrue(exception.getMessage().contains("must be object or boolean"));
        assertTrue(exception.getMessage().contains("STRING"));
    }

    @Test
    void booleanSchemaShouldBeAcceptedForDraft7() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
        Schema schema = registry.getSchema("false");

        List<Error> errors = schema.validate("42", InputFormat.JSON);

        assertEquals(1, errors.size());
    }

    @Test
    void booleanSchemaShouldNotBeAcceptedForDraft4() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4);

        SchemaException exception = assertThrows(SchemaException.class, () -> registry.getSchema("false"));

        assertTrue(exception.getMessage().contains("must be object"));
        assertTrue(exception.getMessage().contains("BOOLEAN"));
    }
}
