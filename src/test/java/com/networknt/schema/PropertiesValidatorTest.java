package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.walk.WalkConfig;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by josejulio on 25/04/22.
 */
class PropertiesValidatorTest extends BaseJsonSchemaValidatorTest {

    @Test
    void testDoesNotThrowWhenApplyingDefaultPropertiesToNonObjects() throws Exception {
        Assertions.assertDoesNotThrow(() -> {
            WalkConfig walkConfig = WalkConfig.builder().applyDefaultsStrategy(new ApplyDefaultsStrategy(true, true, true)).build();
            SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4);
            Schema schema = factory.getSchema("{\"type\":\"object\",\"properties\":{\"foo\":{\"type\":\"object\", \"properties\": {} },\"i-have-default\":{\"type\":\"string\",\"default\":\"foo\"}}}");
            JsonNode node = getJsonNodeFromStringContent("{\"foo\": \"bar\"}");
            Result result = schema.walk(node, true, executionContext -> executionContext.setWalkConfig(walkConfig));
            Assertions.assertEquals(result.getErrors().size(), 1);
        });
    }
}
