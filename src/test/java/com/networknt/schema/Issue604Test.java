package com.networknt.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class Issue604Test {
    @Test
    void failure() {
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                .applyDefaultsStrategy(new ApplyDefaultsStrategy(true, false, false))
                .build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        JsonSchema schema = factory.getSchema("{ \"type\": \"object\", \"properties\": { \"foo\": { \"type\": \"object\", \"properties\": { \"bar\": { \"type\": \"boolean\", \"default\": false } } } } }", config);
        ObjectMapper objectMapper = new ObjectMapper();
        assertDoesNotThrow(() -> {
            schema.walk(objectMapper.readTree("{}"), false);
        });
    }

}
