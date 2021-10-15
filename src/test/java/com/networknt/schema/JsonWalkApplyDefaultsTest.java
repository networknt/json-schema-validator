package com.networknt.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.stream.Collectors;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


class JsonWalkApplyDefaultsTest {

    @ParameterizedTest
    @ValueSource(booleans = { true, false})
    void testApplyDefaults3(boolean shouldValidateSchema) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode inputNode = objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data-default.json"));
        JsonSchema jsonSchema = createSchema(new ApplyDefaultsStrategy(true, true, true));
        ValidationResult result = jsonSchema.walk(inputNode, shouldValidateSchema);
        if (shouldValidateSchema) {
            assertThat(result.getValidationMessages().stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                       Matchers.containsInAnyOrder("$.outer.mixedObject.intValue_missingButError: string found, integer expected",
                                                   "$.outer.badArray[1]: integer found, string expected"));
        } else {
            assertThat(result.getValidationMessages(), Matchers.empty());
        }
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

    @ParameterizedTest
    @ValueSource(strings = { "walkWithNoDefaults", "validateWithApplyAllDefaults"} )
    void testApplyDefaults0(String method) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode inputNode = objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data-default.json"));
        JsonNode inputNodeOriginal = objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data-default.json"));
        Set<ValidationMessage> validationMessages;
        switch (method) {
            case "walkWithNoDefaults": {
                JsonSchema jsonSchema = createSchema(new ApplyDefaultsStrategy(false, false, false));
                validationMessages = jsonSchema.walk(inputNode, true).getValidationMessages();
                break;
            }
            case "validateWithApplyAllDefaults": {
                JsonSchema jsonSchema = createSchema(new ApplyDefaultsStrategy(true, true, true));
                validationMessages = jsonSchema.validate(inputNode);
                break;
            }
            default: throw new UnsupportedOperationException();
        }
        assertThat(validationMessages.stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                   Matchers.containsInAnyOrder("$.outer.mixedObject.intValue_missing: is missing but it is required",
                                               "$.outer.mixedObject.intValue_null: null found, integer expected",
                                               "$.outer.mixedObject.intValue_missingButError: is missing but it is required",
                                               "$.outer.goodArray[1]: null found, string expected",
                                               "$.outer.badArray[1]: null found, string expected"));
        assertEquals(inputNodeOriginal,  inputNode);
        CollectorContext.getInstance().reset(); // necessary because we are calling both jsonSchema.walk and jsonSchema.validate
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
