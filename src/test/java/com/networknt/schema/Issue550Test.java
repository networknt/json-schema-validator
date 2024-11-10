package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Set;


class Issue550Test {
    protected JsonSchema getJsonSchemaFromStreamContentV7(String schemaPath) {
        InputStream schemaContent = getClass().getResourceAsStream(schemaPath);
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        return factory.getSchema(schemaContent);
    }

    protected JsonNode getJsonNodeFromStreamContent(String dataPath) throws Exception {
        InputStream content = getClass().getResourceAsStream(dataPath);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(content);
        return node;
    }

    @Test
    void testValidationMessageDoContainSchemaPath() throws Exception {
        String schemaPath = "/schema/issue500_1-v7.json";
        String dataPath = "/data/issue500_1.json";
        JsonSchema schema = getJsonSchemaFromStreamContentV7(schemaPath);
        JsonNode node = getJsonNodeFromStreamContent(dataPath);

        Set<ValidationMessage> errors = schema.validate(node);
        ValidationMessage validationMessage = errors.stream().findFirst().get();

        Assertions.assertEquals("https://example.com/person.schema.json#/properties/age/minimum", validationMessage.getSchemaLocation().toString());
        Assertions.assertEquals(1, errors.size());
    }

    @Test
    void testValidationMessageDoContainSchemaPathForOneOf() throws Exception {
        String schemaPath = "/schema/issue500_2-v7.json";
        String dataPath = "/data/issue500_2.json";
        JsonSchema schema = getJsonSchemaFromStreamContentV7(schemaPath);
        JsonNode node = getJsonNodeFromStreamContent(dataPath);

        Set<ValidationMessage> errors = schema.validate(node);
        ValidationMessage validationMessage = errors.stream().findFirst().get();

        // Instead of capturing all subSchema within oneOf, a pointer to oneOf should be provided.
        Assertions.assertEquals("https://example.com/person.schema.json#/oneOf", validationMessage.getSchemaLocation().toString());
        Assertions.assertEquals(1, errors.size());
    }

}
