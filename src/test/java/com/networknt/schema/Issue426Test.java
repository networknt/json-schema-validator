package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

/**
 * Validating custom message
 */
class Issue426Test {
    protected Schema getJsonSchemaFromStreamContentV7(InputStream schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(Specification.Version.DRAFT_7);
        return factory.getSchema(schemaContent);
    }

    protected JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    @Test
    void shouldWorkV7() throws Exception {
        String schemaPath = "/schema/issue426-v7.json";
        String dataPath = "/data/issue426.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        Schema schema = getJsonSchemaFromStreamContentV7(schemaInputStream);
        InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
        JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
        List<Error> errors = schema.validate(node);
        Assertions.assertEquals(2, errors.size());
        final JsonNode message = schema.schemaNode.get("message");
        for(Error error : errors) {
            //validating custom message
            Assertions.assertEquals(message.get(error.getKeyword()).asText(),  error.getMessage());
        }
    }
}

