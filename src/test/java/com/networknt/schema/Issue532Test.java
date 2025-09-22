package com.networknt.schema;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Issue532Test {
    @Test
    void failure() {
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(Specification.Version.DRAFT_7);
        JsonSchemaException ex = assertThrows(JsonSchemaException.class, () -> {
            factory.getSchema("{ \"$schema\": true }");
        });
        assertEquals("Unknown MetaSchema: true", ex.getMessage());
    }
}
