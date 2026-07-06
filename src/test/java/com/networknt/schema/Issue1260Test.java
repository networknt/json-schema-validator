package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class Issue1260Test extends BaseJsonSchemaValidatorTest {

    private static final String RESOURCE_PREFIX = "issues/1260/";
    private static Schema schema;

    @BeforeAll
    static void setup() {
        schema = getJsonSchemaFromClasspath(resource("schema.json"), SpecificationVersion.DRAFT_2020_12);
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid1.json", "valid2.json", "valid3.json"})
    void testNoErrorsForValidInput(String validJson) throws IOException {
        final var node = getJsonNodeFromClasspath(resource(validJson));
        final var errors = schema.validate(node);
        assertTrue(errors.isEmpty(), "No validation errors for valid input");
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid1.json", "invalid2.json", "invalid3.json"})
    void testErrorsForInvalidInput(String invalidJson) throws IOException {
        final var node = getJsonNodeFromClasspath(resource(invalidJson));
        final var errors = schema.validate(node);
        assertFalse(errors.isEmpty(), "Validation errors for invalid input");
    }

    private static String resource(String name) {
        return RESOURCE_PREFIX + name;
    }
}
