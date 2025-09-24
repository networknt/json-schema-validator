package com.networknt.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.walk.ApplyDefaultsStrategy;
import com.networknt.schema.walk.WalkConfig;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class Issue604Test {
    @Test
    void failure() {
		WalkConfig walkConfig = WalkConfig.builder()
				.applyDefaultsStrategy(new ApplyDefaultsStrategy(true, false, false)).build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
        Schema schema = factory.getSchema("{ \"type\": \"object\", \"properties\": { \"foo\": { \"type\": \"object\", \"properties\": { \"bar\": { \"type\": \"boolean\", \"default\": false } } } } }");
        ObjectMapper objectMapper = new ObjectMapper();
        assertDoesNotThrow(() -> {
            schema.walk(objectMapper.readTree("{}"), false, executionContext -> executionContext.setWalkConfig(walkConfig));
        });
    }

}
