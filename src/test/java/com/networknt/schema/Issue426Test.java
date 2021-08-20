package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Set;

/**
 * Validating custom message
 */
public class Issue426Test {
    protected JsonSchema getJsonSchemaFromStreamContentV7(InputStream schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        return factory.getSchema(schemaContent);
    }

    protected JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    @Test
    public void shouldWorkV7() throws Exception {
        String schemaPath = "/schema/issue426-v7.json";
        String dataPath = "/data/issue426.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        JsonSchema schema = getJsonSchemaFromStreamContentV7(schemaInputStream);
        InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
        JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
        Set<ValidationMessage> errors = schema.validate(node);
        Assert.assertEquals(2, errors.size());
        final JsonNode message = schema.schemaNode.get("message");
        for(ValidationMessage error : errors) {
            //validating custom message
            Assert.assertEquals(message.get(error.getType()).asText(),  error.getMessage());
        }
    }
}

