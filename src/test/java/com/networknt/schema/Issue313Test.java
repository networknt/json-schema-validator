package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.util.Set;

public class Issue313Test {
    protected JsonSchema getJsonSchemaFromStreamContentV7(InputStream schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        return factory.getSchema(schemaContent);
    }

    protected JsonSchema getJsonSchemaFromStreamContentV201909(InputStream schemaContent) {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909);
        return factory.getSchema(schemaContent);
    }

    protected JsonNode getJsonNodeFromStreamContent(InputStream content) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(content);
        return node;
    }

    @Test
    @Ignore
    public void shouldFailV201909() throws Exception {
        String schemaPath = "/schema/issue313-2019-09.json";
        String dataPath = "/data/issue313.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        JsonSchema schema = getJsonSchemaFromStreamContentV201909(schemaInputStream);
        InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
        JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
        Set<ValidationMessage> errors = schema.validate(node);
        Assert.assertEquals(2, errors.size());
    }

    @Test
    public void shouldFailV7() throws Exception {
        String schemaPath = "/schema/issue313-v7.json";
        String dataPath = "/data/issue313.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        JsonSchema schema = getJsonSchemaFromStreamContentV7(schemaInputStream);
        InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
        JsonNode node = getJsonNodeFromStreamContent(dataInputStream);
        Set<ValidationMessage> errors = schema.validate(node);
        Assert.assertEquals(2, errors.size());
    }

}
