package com.networknt.schema;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(booleans = { true, false } )
    public void dataIsValid(boolean failFast) throws Exception {
        String schemaPath = "/schema/issue386-v7.json";
        String dataPath = "/data/issue386.json";
        JsonSchema schema = getJsonSchemaFromPathV7(schemaPath, failFast);
        JsonNode node = getJsonNodeFromPath(dataPath).get("valid");
        node.forEach(testNode -> {
            Set<ValidationMessage> errors = schema.validate(testNode.get("data"));
            Assertions.assertEquals(0, errors.size(), "Expected no errors for " + testNode.get("data"));
        });
    }

    @Test
    public void dataIsInvalidFailFast() throws Exception {
        String schemaPath = "/schema/issue386-v7.json";
        String dataPath = "/data/issue386.json";
        JsonSchema schema = getJsonSchemaFromPathV7(schemaPath, true);
        JsonNode node = getJsonNodeFromPath(dataPath).get("invalid");
        node.forEach(testNode -> {
            try {
                schema.validate(testNode.get("data"));
                Assertions.fail();
            } catch (JsonSchemaException e) {
                Assertions.assertEquals(testNode.get("expectedErrors").get(0).asText(), e.getMessage());
            }
        });
    }

    @Test
    public void dataIsInvalidFailSlow() throws Exception {
        String schemaPath = "/schema/issue386-v7.json";
        String dataPath = "/data/issue386.json";
        JsonSchema schema = getJsonSchemaFromPathV7(schemaPath, false);
        JsonNode node = getJsonNodeFromPath(dataPath).get("invalid");
        node.forEach(testNode -> {
            Set<ValidationMessage> errors = schema.validate(testNode.get("data"));
            List<String> errorMessages = errors.stream().map(x -> x.getMessage()).collect(Collectors.toList());
            testNode.get("expectedErrors").forEach(expectedError -> {
                Assertions.assertTrue(errorMessages.contains(expectedError.asText()));
            });
        });
    }
}
