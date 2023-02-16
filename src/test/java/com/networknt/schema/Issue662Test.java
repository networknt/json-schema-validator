package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Issue662Test extends BaseJsonSchemaValidatorTest {

    private static final String RESOURCE_PREFIX = "issues/662/";
    private static JsonSchema schema;

    @BeforeAll
    static void setup() {
        schema = getJsonSchemaFromClasspath(resource("schema.json"), SpecVersion.VersionFlag.V7);
    }

    @Test
    void testNoErrorsForEmptyObject() throws IOException {
        JsonNode node = getJsonNodeFromClasspath(resource("emptyObject.json"));
        Set<ValidationMessage> errors = schema.validate(node);
        assertTrue(errors.isEmpty(), "No validation errors for empty optional object");
    }

    @Test
    void testNoErrorsForValidObject() throws IOException {
        JsonNode node = getJsonNodeFromClasspath(resource("validObject.json"));
        Set<ValidationMessage> errors = schema.validate(node);
        assertTrue(errors.isEmpty(), "No validation errors for a valid optional object");
    }

    @Test
    void testCorrectErrorForInvalidValue() throws IOException {
        JsonNode node = getJsonNodeFromClasspath(resource("objectInvalidValue.json"));
        Set<ValidationMessage> errors = schema.validate(node);
        List<String> errorMessages = errors.stream()
            .map(ValidationMessage::getMessage)
            .collect(toList());

        assertTrue(
            errorMessages.contains("$.optionalObject.value: does not have a value in the enumeration [one, two]"),
            "Validation error for invalid object property is captured"
        );
        assertFalse(
            errorMessages.contains("$.optionalObject: object found, null expected"),
            "No validation error that the object is not expected"
        );
    }

    private static String resource(String name) {
        return RESOURCE_PREFIX + name;
    }
}
