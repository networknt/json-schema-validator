package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by josejulio on 25/04/22.
 */
class PropertiesValidatorTest extends BaseJsonSchemaValidatorTest {

    @Test
    void testDoesNotThrowWhenApplyingDefaultPropertiesToNonObjects() throws Exception {
        Assertions.assertDoesNotThrow(() -> {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);

            SchemaValidatorsConfig schemaValidatorsConfig = SchemaValidatorsConfig.builder()
                    .applyDefaultsStrategy(new ApplyDefaultsStrategy(true, true, true))
                    .build();

            JsonSchema schema = factory.getSchema("{\"type\":\"object\",\"properties\":{\"foo\":{\"type\":\"object\", \"properties\": {} },\"i-have-default\":{\"type\":\"string\",\"default\":\"foo\"}}}", schemaValidatorsConfig);
            JsonNode node = getJsonNodeFromStringContent("{\"foo\": \"bar\"}");
            ValidationResult result = schema.walk(node, true);
            Assertions.assertEquals(result.getValidationMessages().size(), 1);
        });
    }
}
