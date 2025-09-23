package com.networknt.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.walk.WalkConfig;

import java.io.IOException;
import java.util.List;
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
        Schema jsonSchema = createSchema();
		WalkConfig walkConfig = WalkConfig.builder()
				.applyDefaultsStrategy(new ApplyDefaultsStrategy(true, true, true)).build();
        Result result = jsonSchema.walk(inputNode, shouldValidateSchema, executionContext -> executionContext.setWalkConfig(walkConfig));
        if (shouldValidateSchema) {
            assertThat(result.getErrors().stream().map(Error::toString).collect(Collectors.toList()),
                       Matchers.containsInAnyOrder("/outer/mixedObject/intValue_missingButError: string found, integer expected",
                                                   "/outer/badArray/1: integer found, string expected",
                               "/outer/reference/stringValue_missing_with_default_null: null found, string expected"));
        } else {
            assertThat(result.getErrors(), Matchers.empty());
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
        Schema jsonSchema = createSchema();
		WalkConfig walkConfig = WalkConfig.builder()
				.applyDefaultsStrategy(new ApplyDefaultsStrategy(true, true, false)).build();
        Result result = jsonSchema.walk(inputNode, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertThat(result.getErrors().stream().map(Error::toString).collect(Collectors.toList()),
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
        Schema jsonSchema = createSchema();
		WalkConfig walkConfig = WalkConfig.builder()
				.applyDefaultsStrategy(new ApplyDefaultsStrategy(true, false, false)).build();
        Result result = jsonSchema.walk(inputNode, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertThat(result.getErrors().stream().map(Error::toString).collect(Collectors.toList()),
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
        List<Error> errors;
        switch (method) {
            case "walkWithEmptyStrategy": {
        		WalkConfig walkConfig = WalkConfig.builder()
        				.applyDefaultsStrategy(new ApplyDefaultsStrategy(false, false, false)).build();
                Schema jsonSchema = createSchema();
                errors = jsonSchema.walk(inputNode, true, executionContext -> executionContext.setWalkConfig(walkConfig)).getErrors();
                break;
            }
            case "walkWithNoDefaults": {
                // same empty strategy, but tests for NullPointerException
        		WalkConfig walkConfig = WalkConfig.builder()
        				.applyDefaultsStrategy(null).build();
            	Schema jsonSchema = createSchema();
                errors = jsonSchema.walk(inputNode, true, executionContext -> executionContext.setWalkConfig(walkConfig)).getErrors();
                break;
            }
            case "validateWithApplyAllDefaults": {
        		WalkConfig walkConfig = WalkConfig.builder()
        				.applyDefaultsStrategy(new ApplyDefaultsStrategy(true, true, true)).build();
                Schema jsonSchema = createSchema();
                errors = jsonSchema.validate(inputNode, executionContext -> executionContext.setWalkConfig(walkConfig));
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }
        assertThat(errors.stream().map(Error::toString).collect(Collectors.toList()),
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

    private Schema createSchema() {
        SchemaRegistry schemaFactory = SchemaRegistry.withDefaultDialect(Specification.Version.DRAFT_4);
        return schemaFactory
                .getSchema(getClass().getClassLoader().getResourceAsStream("schema/walk-schema-default.json"));
    }
}
