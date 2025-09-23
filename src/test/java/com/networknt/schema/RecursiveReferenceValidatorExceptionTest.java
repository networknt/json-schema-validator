package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.keyword.RecursiveRefValidator;

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
        SchemaRegistry jsonSchemaFactory = SchemaRegistry.withDefaultDialect(Specification.Version.DRAFT_2020_12);
        Schema jsonSchema = jsonSchemaFactory.getSchema(invalidSchemaJson);
        JsonNode schemaNode = jsonSchema.getSchemaNode();
        SchemaContext schemaContext = new SchemaContext(jsonSchema.getSchemaContext().getDialect(),
                jsonSchemaFactory);

        // Act and Assert
        assertThrows(JsonSchemaException.class, () -> {
            new RecursiveRefValidator(SchemaLocation.of(""), new JsonNodePath(PathType.JSON_POINTER), schemaNode, null, schemaContext);
        });
    }

}
