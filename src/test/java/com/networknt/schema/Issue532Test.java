package com.networknt.schema;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Issue532Test {
    @Test
    void failure() {
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
        SchemaException ex = assertThrows(SchemaException.class, () -> {
            factory.getSchema("{ \"$schema\": true }");
        });
        assertEquals("Unknown dialect: true", ex.getMessage());
    }
}
