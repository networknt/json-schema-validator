package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Set;

public class Issue255Test {
    protected JsonSchema getJsonSchemaFromStreamContent(InputStream schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        return factory.getSchema(schemaContent);
    }

    protected JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(content);
        return node;
    }

    @Test
    public void shouldFailWhenRequiredPropertiesDoNotExistInReferencedSubSchema() throws Exception {
        String schemaPath = "/draft2019-09/issue255.json";
        String dataPath = "/data/issue255.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        JsonSchema schema = getJsonSchemaFromStreamContent(schemaInputStream);
        InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
        JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
        Set<ValidationMessage> errors = schema.validate(node);
        Assert.assertEquals(2, errors.size());
    }
}
