package com.networknt.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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
                       Matchers.containsInAnyOrder("/outer/mixedObject/intValue_missingButError: string found, integer expected",
                                                   "/outer/badArray/1: integer found, string expected",
                               "/outer/reference/stringValue_missing_with_default_null: null found, string expected"));
        } else {
            assertThat(result.getValidationMessages(), Matchers.empty());
        }
        // TODO: In Java 14 use text blocks
        assertEquals(
                objectMapper.readTree(
                        "{\"outer\":{\"mixedObject\":{\"intValue_present\":8,\"intValue_null\":35,\"intValue_missingButError\":\"forty-five\",\"intValue_missing\":15,\"intValue_missing_notRequired\":25},\"goodArray\":[\"hello\",\"five\"],\"badArray\":[\"hello\",5],\"reference\":{\"stringValue_missing_with_default_null\":null,\"stringValue_missing\":\"hello\"}}}"),
                inputNode);
    }

    @Test
    void testApplyDefaults2() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode inputNode = objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data-default.json"));
        JsonSchema jsonSchema = createSchema(new ApplyDefaultsStrategy(true, true, false));
        ValidationResult result = jsonSchema.walk(inputNode, true);
        assertThat(result.getValidationMessages().stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                   Matchers.containsInAnyOrder("/outer/mixedObject/intValue_missingButError: string found, integer expected",
                                               "/outer/goodArray/1: null found, string expected",
                                               "/outer/badArray/1: null found, string expected",
                           "/outer/reference/stringValue_missing_with_default_null: null found, string expected"));
        assertEquals(
                objectMapper.readTree(
                        "{\"outer\":{\"mixedObject\":{\"intValue_present\":8,\"intValue_null\":35,\"intValue_missingButError\":\"forty-five\",\"intValue_missing\":15,\"intValue_missing_notRequired\":25},\"goodArray\":[\"hello\",null],\"badArray\":[\"hello\",null],\"reference\":{\"stringValue_missing_with_default_null\":null,\"stringValue_missing\":\"hello\"}}}"),
                inputNode);
    }

    @Test
    void testApplyDefaults1() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode inputNode = objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data-default.json"));
        JsonSchema jsonSchema = createSchema(new ApplyDefaultsStrategy(true, false, false));
        ValidationResult result = jsonSchema.walk(inputNode, true);
        assertThat(result.getValidationMessages().stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                   Matchers.containsInAnyOrder("/outer/mixedObject/intValue_null: null found, integer expected",
                                               "/outer/mixedObject/intValue_missingButError: string found, integer expected",
                                               "/outer/goodArray/1: null found, string expected",
                                               "/outer/badArray/1: null found, string expected",
                           "/outer/reference/stringValue_missing_with_default_null: null found, string expected"));
        assertEquals(
                objectMapper.readTree(
                        "{\"outer\":{\"mixedObject\":{\"intValue_present\":8,\"intValue_null\":null,\"intValue_missingButError\":\"forty-five\",\"intValue_missing\":15,\"intValue_missing_notRequired\":25},\"goodArray\":[\"hello\",null],\"badArray\":[\"hello\",null],\"reference\":{\"stringValue_missing_with_default_null\":null,\"stringValue_missing\":\"hello\"}}}"),
                inputNode);
    }

    @ParameterizedTest
    @ValueSource(strings = { "walkWithEmptyStrategy", "walkWithNoDefaults", "validateWithApplyAllDefaults"} )
    void testApplyDefaults0(String method) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode inputNode = objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data-default.json"));
        JsonNode inputNodeOriginal = objectMapper.readTree(getClass().getClassLoader().getResourceAsStream("data/walk-data-default.json"));
        Set<ValidationMessage> validationMessages;
        switch (method) {
            case "walkWithEmptyStrategy": {
                JsonSchema jsonSchema = createSchema(new ApplyDefaultsStrategy(false, false, false));
                validationMessages = jsonSchema.walk(inputNode, true).getValidationMessages();
                break;
            }
            case "walkWithNoDefaults": {
                // same empty strategy, but tests for NullPointerException
                JsonSchema jsonSchema = createSchema(null);
                validationMessages = jsonSchema.walk(inputNode, true).getValidationMessages();
                break;
            }
            case "validateWithApplyAllDefaults": {
                JsonSchema jsonSchema = createSchema(new ApplyDefaultsStrategy(true, true, true));
                validationMessages = jsonSchema.validate(inputNode);
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }
        assertThat(validationMessages.stream().map(ValidationMessage::getMessage).collect(Collectors.toList()),
                   Matchers.containsInAnyOrder("/outer/mixedObject: required property 'intValue_missing' not found",
                                               "/outer/mixedObject: required property 'intValue_missingButError' not found",
                                               "/outer/mixedObject/intValue_null: null found, integer expected",
                                               "/outer/goodArray/1: null found, string expected",
                                               "/outer/badArray/1: null found, string expected",
                                               "/outer/reference: required property 'stringValue_missing' not found"));
        assertEquals(inputNodeOriginal, inputNode);
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
        SchemaValidatorsConfig schemaValidatorsConfig = SchemaValidatorsConfig.builder().applyDefaultsStrategy(applyDefaultsStrategy).build();
        return schemaFactory.getSchema(getClass().getClassLoader().getResourceAsStream("schema/walk-schema-default.json"), schemaValidatorsConfig);
    }
}
