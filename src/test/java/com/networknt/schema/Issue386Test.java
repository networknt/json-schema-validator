package com.networknt.schema;

import java.io.InputStream;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Issue386Test {
    protected JsonSchema getJsonSchemaFromPathV7(String schemaPath, boolean failFast) {
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
    	JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    	SchemaValidatorsConfig config = new SchemaValidatorsConfig();
    	config.setFailFast(failFast);
        return factory.getSchema(schemaInputStream, config);
    }

    protected JsonNode getJsonNodeFromPath(String dataPath) throws Exception {
    	InputStream dataInputStream = getClass().getResourceAsStream(dataPath);
    	ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(dataInputStream);
        return node;
    }

    @Test
    public void dataIsValidFailAll() throws Exception {
        String schemaPath = "/schema/issue386-v7.json";
        String dataPath = "/data/issue386.json";
        JsonSchema schema = getJsonSchemaFromPathV7(schemaPath, false);
        JsonNode node = getJsonNodeFromPath(dataPath);
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(0, errors.size());
    }

    @Test
    public void dataIsValidFailFast() throws Exception {
        String schemaPath = "/schema/issue386-v7.json";
        String dataPath = "/data/issue386.json";
        JsonSchema schema = getJsonSchemaFromPathV7(schemaPath, true);
        JsonNode node = getJsonNodeFromPath(dataPath);
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(0, errors.size());
    }
}
