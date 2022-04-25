package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by josejulio on 25/04/22.
 */
public class PropertiesValidatorTest extends BaseJsonSchemaValidatorTest {

    @Test
    public void testDoesNotThrowWhenApplyingDefaultPropertiesToNonObjects() throws Exception {
        Assertions.assertDoesNotThrow(() -> {
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);

            SchemaValidatorsConfig schemaValidatorsConfig = new SchemaValidatorsConfig();
            schemaValidatorsConfig.setApplyDefaultsStrategy(new ApplyDefaultsStrategy(
                    true,
                    true,
                    true
            ));

            JsonSchema schema = factory.getSchema("{\"type\":\"object\",\"properties\":{\"foo\":{\"type\":\"object\", \"properties\": {} },\"i-have-default\":{\"type\":\"string\",\"default\":\"foo\"}}}", schemaValidatorsConfig);
            JsonNode node = getJsonNodeFromStringContent("{\"foo\": \"bar\"}");
            ValidationResult result = schema.walk(node, true);
            Assertions.assertEquals(result.getValidationMessages().size(), 1);
        });
    }
}
