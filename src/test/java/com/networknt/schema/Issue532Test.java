package com.networknt.schema;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Issue532Test {
    @Test
    public void failure() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        JsonSchemaException ex = assertThrows(JsonSchemaException.class, () -> {
            factory.getSchema("{ \"$schema\": true }");
        });
        assertEquals("Unknown MetaSchema: true", ex.getMessage());
    }
}
