package com.networknt.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;


class JsonWalkApplyDefaultsTest {

    @Test
    void testApplyDefaults3() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode inputNode = objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data-default.json"));
        JsonSchema jsonSchema = createSchema(new ApplyDefaultsStrategy(true, true, true));
        ValidationResult result = jsonSchema.walk(inputNode, true);
        assertThat(result.getValidationMessages().stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                   Matchers.containsInAnyOrder("$.outer.mixedObject.intValue_missingButError: string found, integer expected",
                                               "$.outer.badArray[1]: integer found, string expected"));
        assertEquals("{\"outer\":{\"mixedObject\":{\"intValue_present\":11,\"intValue_null\":25,\"intValue_missing\":15,\"intValue_missingButError\":\"thirty-five\"},\"goodArray\":[\"hello\",\"five\"],\"badArray\":[\"hello\",5]}}",
                     inputNode.toString());
    }

    @Test
    void testApplyDefaults2() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode inputNode = objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data-default.json"));
        JsonSchema jsonSchema = createSchema(new ApplyDefaultsStrategy(true, true, false));
        ValidationResult result = jsonSchema.walk(inputNode, true);
        assertThat(result.getValidationMessages().stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                   Matchers.containsInAnyOrder("$.outer.mixedObject.intValue_missingButError: string found, integer expected",
                                               "$.outer.goodArray[1]: null found, string expected",
                                               "$.outer.badArray[1]: null found, string expected"));
        assertEquals("{\"outer\":{\"mixedObject\":{\"intValue_present\":11,\"intValue_null\":25,\"intValue_missing\":15,\"intValue_missingButError\":\"thirty-five\"},\"goodArray\":[\"hello\",null],\"badArray\":[\"hello\",null]}}",
                     inputNode.toString());
    }

    @Test
    void testApplyDefaults1() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode inputNode = objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data-default.json"));
        JsonSchema jsonSchema = createSchema(new ApplyDefaultsStrategy(true, false, false));
        ValidationResult result = jsonSchema.walk(inputNode, true);
        assertThat(result.getValidationMessages().stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                   Matchers.containsInAnyOrder("$.outer.mixedObject.intValue_null: null found, integer expected",
                                               "$.outer.mixedObject.intValue_missingButError: string found, integer expected",
                                               "$.outer.goodArray[1]: null found, string expected",
                                               "$.outer.badArray[1]: null found, string expected"));
        assertEquals("{\"outer\":{\"mixedObject\":{\"intValue_present\":11,\"intValue_null\":null,\"intValue_missing\":15,\"intValue_missingButError\":\"thirty-five\"},\"goodArray\":[\"hello\",null],\"badArray\":[\"hello\",null]}}",
                     inputNode.toString());
    }

    @Test
    void testApplyDefaults0() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode inputNode = objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data-default.json"));
        JsonNode inputNodeOriginal = objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data-default.json"));
        JsonSchema jsonSchema = createSchema(new ApplyDefaultsStrategy(false, false, false));
        ValidationResult result = jsonSchema.walk(inputNode, true);
        assertThat(result.getValidationMessages().stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                   Matchers.containsInAnyOrder("$.outer.mixedObject.intValue_missing: is missing but it is required",
                                               "$.outer.mixedObject.intValue_null: null found, integer expected",
                                               "$.outer.mixedObject.intValue_missingButError: is missing but it is required",
                                               "$.outer.goodArray[1]: null found, string expected",
                                               "$.outer.badArray[1]: null found, string expected"));
        assertEquals(inputNodeOriginal,  inputNode);
    }

    @Test
    void testIllegalArgumentException() {
        try {
            new ApplyDefaultsStrategy(false, true, false);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException ignored) {
        }
    }

    private JsonSchema createSchema(ApplyDefaultsStrategy applyDefaultsStrategy) {
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
        SchemaValidatorsConfig schemaValidatorsConfig = new SchemaValidatorsConfig();
        schemaValidatorsConfig.setApplyDefaultsStrategy(applyDefaultsStrategy);
        return schemaFactory.getSchema(getClass().getClassLoader().getResourceAsStream("schema/walk-schema-default.json"), schemaValidatorsConfig);
    }
}
