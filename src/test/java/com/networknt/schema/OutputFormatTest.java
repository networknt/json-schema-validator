package com.networknt.schema;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class OutputFormatTest {

    private static JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    private static String schemaPath1 = "/schema/output-format-schema.json";

    private JsonNode getJsonNodeFromJsonData(String jsonFilePath) throws Exception {
        InputStream content = getClass().getResourceAsStream(jsonFilePath);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    @Test
    @DisplayName("Test Validation Messages")
    void testInvalidJson() throws Exception {
        InputStream schemaInputStream = OutputFormatTest.class.getResourceAsStream(schemaPath1);
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
        JsonSchema schema = factory.getSchema(schemaInputStream, config);
        JsonNode node = getJsonNodeFromJsonData("/data/output-format-input.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(3, errors.size());

        Set<String[]> messages = errors.stream().map(m -> new String[] { m.getEvaluationPath().toString(),
                m.getSchemaLocation().toString(), m.getInstanceLocation().toString(), m.getMessage() })
                .collect(Collectors.toSet());
        
        assertThat(messages,
                Matchers.containsInAnyOrder(
                        new String[] { "/minItems", "https://example.com/polygon#/minItems", "", ": expected at least 3 items but found 2" },
                        new String[] { "/items/$ref/additionalProperties", "https://example.com/polygon#/$defs/point/additionalProperties", "/1/z",
                                "/1/z: is not defined in the schema and the schema does not allow additional properties" },
                        new String[] { "/items/$ref/required", "https://example.com/polygon#/$defs/point/required", "/1", "/1: required property 'y' not found"}));
    }
}
