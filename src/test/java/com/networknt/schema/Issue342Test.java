package com.networknt.schema;

import java.io.InputStream;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class Issue342Test {
    protected JsonSchema getJsonSchemaFromStreamContentV7(InputStream schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        return factory.getSchema(schemaContent);
    }

    protected JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(content);
        return node;
    }

    @Test
    void propertyNameEnumShouldFailV7() throws Exception {
        String schemaPath = "/schema/issue342-v7.json";
        String dataPath = "/data/issue342.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        JsonSchema schema = getJsonSchemaFromStreamContentV7(schemaInputStream);
        InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
        JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(1, errors.size());
        final ValidationMessage error = errors.iterator().next();
        Assertions.assertEquals("$", error.getInstanceLocation().toString());
        Assertions.assertEquals("$: property 'z' name is not valid: does not have a value in the enumeration [\"a\", \"b\", \"c\"]", error.getMessage());
    }
}
