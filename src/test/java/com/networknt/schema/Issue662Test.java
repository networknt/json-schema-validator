package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Issue662Test extends BaseJsonSchemaValidatorTest {

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
            .map(v -> v.getEvaluationPath() + " = " + v.getMessage())
            .collect(toList());

        // As this is from an anyOf evaluation both error messages should be present as they didn't match any
        // The evaluation cannot be expected to know the semantic meaning that this is an optional object
        // The evaluation path can be used to provide clarity on the reason
        // Omitting the 'object found, null expected' message also provides the misleading impression that the
        // object is required when leaving it empty is a possible option
        assertTrue(errorMessages
                .contains("$.properties.optionalObject.anyOf[0].type = $.optionalObject: object found, null expected"));
        assertTrue(errorMessages.contains(
                "$.properties.optionalObject.anyOf[1].properties.value.enum = $.optionalObject.value: does not have a value in the enumeration [\"one\", \"two\"]"));
    }

    private static String resource(String name) {
        return RESOURCE_PREFIX + name;
    }
}
