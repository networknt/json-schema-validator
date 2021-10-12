package com.networknt.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;


public class JsonWalkApplyDefaultsTest {

    @Test
    public void testApplyDefaults() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode inputNode = objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data-default.json"));
        JsonSchema jsonSchema = createSchema();
        ValidationResult result = jsonSchema.walk(inputNode, true);
        assertThat(result.getValidationMessages().stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                   Matchers.contains("$.outer.mixedObject.intValue_missingButError: string found, integer expected",
                                     "$.outer.badArray[1]: integer found, string expected"));
        assertEquals(4, inputNode.get("outer").get("mixedObject").size());
        assertEquals(11, inputNode.get("outer").get("mixedObject").get("intValue_present").intValue());
        assertEquals(15, inputNode.get("outer").get("mixedObject").get("intValue_missing").intValue());
        assertEquals(25, inputNode.get("outer").get("mixedObject").get("intValue_null").intValue());
        assertEquals("thirty-five", inputNode.get("outer").get("mixedObject").get("intValue_missingButError").textValue());
        assertEquals(2, inputNode.get("outer").get("goodArray").size());
        assertEquals("hello", inputNode.get("outer").get("goodArray").get(0).textValue());
        assertEquals("five", inputNode.get("outer").get("goodArray").get(1).textValue());
        assertEquals(2, inputNode.get("outer").get("badArray").size());
        assertEquals("hello", inputNode.get("outer").get("badArray").get(0).textValue());
        assertEquals(5, inputNode.get("outer").get("badArray").get(1).intValue());
    }

    private JsonSchema createSchema() {
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        SchemaValidatorsConfig schemaValidatorsConfig = new SchemaValidatorsConfig();
        schemaValidatorsConfig.setShouldApplyDefaults(true);
        return schemaFactory.getSchema(getClass().getClassLoader().getResourceAsStream("schema/walk-schema-default.json"), schemaValidatorsConfig);
    }
}
