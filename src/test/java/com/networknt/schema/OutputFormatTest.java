package com.networknt.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.utils.CachingSupplier;

class OutputFormatTest {

    private static final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    private static final String schemaPath1 = "/schema/output-format-schema.json";

    private JsonNode getJsonNodeFromJsonData(String jsonFilePath) throws Exception {
        InputStream content = getClass().getResourceAsStream(jsonFilePath);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(content);
    }

    @Test
    @DisplayName("Test Validation Messages")
    void testInvalidJson() throws Exception {
        InputStream schemaInputStream = OutputFormatTest.class.getResourceAsStream(schemaPath1);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(schemaInputStream, config);
        JsonNode node = getJsonNodeFromJsonData("/data/output-format-input.json");
        Set<ValidationMessage> errors = schema.validate(node);
        Assertions.assertEquals(3, errors.size());

        Set<String[]> messages = errors.stream().map(m -> new String[] { m.getEvaluationPath().toString(),
                m.getSchemaLocation().toString(), m.getInstanceLocation().toString(), m.getMessage() })
                .collect(Collectors.toSet());
        
        assertThat(messages,
                Matchers.containsInAnyOrder(
                        new String[] { "/minItems", "https://example.com/polygon#/minItems", "", ": must have at least 3 items but found 2" },
                        new String[] { "/items/$ref/additionalProperties", "https://example.com/polygon#/$defs/point/additionalProperties", "/1",
                                "/1: property 'z' is not defined in the schema and the schema does not allow additional properties" },
                        new String[] { "/items/$ref/required", "https://example.com/polygon#/$defs/point/required", "/1", "/1: required property 'y' not found"}));
    }

    public static class Detailed implements OutputFormat<Set<ValidationMessage>> {
        private ValidationMessage format(ValidationMessage message) {
            Supplier<String> messageSupplier = () -> {
                StringBuilder builder = new StringBuilder();
                builder.append("[");
                builder.append(message.getInstanceLocation().toString());
                builder.append("] ");
                JsonNode value = message.getInstanceNode();
                if (!value.isObject() && !value.isArray()) {
                    builder.append("with value ");
                    builder.append("'");
                    builder.append(value.asText());
                    builder.append("'");
                    builder.append(" ");
                }
                builder.append(message.getError());

                return builder.toString();
            };
            return ValidationMessage.builder()
                    .messageSupplier(new CachingSupplier<>(messageSupplier))
                    .evaluationPath(message.getEvaluationPath())
                    .instanceLocation(message.getInstanceLocation())
                    .instanceNode(message.getInstanceNode())
                    .schemaLocation(message.getSchemaLocation())
                    .schemaNode(message.getSchemaNode())
                    .arguments(message.getArguments())
                    .build();
        }

        @Override
        public Set<ValidationMessage> format(JsonSchema jsonSchema, Set<ValidationMessage> validationMessages,
                ExecutionContext executionContext, ValidationContext validationContext) {
            return validationMessages.stream().map(this::format).collect(Collectors.toCollection(LinkedHashSet::new));
        }
    }

    public static final OutputFormat<Set<ValidationMessage>> DETAILED = new Detailed();

    @Test
    void customFormat() {
        String schemaData = "{\n"
                + "  \"properties\": {\n"
                + "    \"type\": {\n"
                + "      \"enum\": [\n"
                + "        \"book\",\n"
                + "        \"author\"\n"
                + "      ]\n"
                + "    },\n"
                + "    \"id\": {\n"
                + "      \"type\": \"string\"\n"
                + "    }\n"
                + "  }\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012)
                .getSchema(schemaData, InputFormat.JSON, SchemaValidatorsConfig.builder().build());
        String inputData = "{\n"
                + "  \"type\": \"cat\",\n"
                + "  \"id\": 1\n"
                + "}";
        List<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON, DETAILED).stream().collect(Collectors.toList());
        assertEquals("[/type] with value 'cat' does not have a value in the enumeration [\"book\", \"author\"]", messages.get(0).getMessage());
        assertEquals("[/id] with value '1' integer found, string expected", messages.get(1).getMessage());
    }
}
