package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * This class handles exception case for {@link RecursiveRefValidator},
 * as recursive ref is detected through #, providing any other keyword throws exception
 */
class RecursiveReferenceValidatorExceptionTest extends AbstractJsonSchemaTestSuite {


    /**
     * this method create test case for handling invalid recursive reference error
     */
    @Test
    void testInvalidRecursiveReference() {
        // Arrange
        String invalidSchemaJson = "{ \"$recursiveRef\": \"invalid\" }";
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema jsonSchema = jsonSchemaFactory.getSchema(invalidSchemaJson);
        JsonNode schemaNode = jsonSchema.getSchemaNode();
        ValidationContext validationContext = new ValidationContext(jsonSchemaFactory.getUriFactory(), null, jsonSchema.getValidationContext().getMetaSchema(), jsonSchemaFactory, null);

        // Act and Assert
        assertThrows(JsonSchemaException.class, () -> {
            new RecursiveRefValidator(SchemaLocation.of(""), new JsonNodePath(PathType.JSON_POINTER), schemaNode, null, validationContext);
        });
    }

}
