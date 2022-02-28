package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Set;

public class Issue526Test {
    private static final JsonSchemaFactory FACTORY =
            JsonSchemaFactory
                    .builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909))
                    .build();

    @Test
    public void testBundledSchema()
            throws Exception {
        String schemaPath = "/schema/issue526.schema.json";
        InputStream schemaInputStream = getClass().getResourceAsStream(schemaPath);
        JsonSchema schema = FACTORY.getSchema(schemaInputStream);

        JsonNode node = getJsonNodeFromJsonData("/data/issue526.example.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertTrue(errors.isEmpty());
    }

    private JsonNode getJsonNodeFromJsonData(String jsonFilePath)
            throws Exception {
        InputStream content = getClass().getResourceAsStream(jsonFilePath);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }
}