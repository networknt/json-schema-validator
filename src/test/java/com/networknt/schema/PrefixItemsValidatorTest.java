package com.networknt.schema;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.serialization.JsonMapperFactory;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class handles exception case for {@link PrefixItemsValidator}
 */
class PrefixItemsValidatorTest extends AbstractJsonSchemaTestSuite {

    /**
     * this method create test cases from JSON and run those test cases with assertion
     */
    @Test
    void testEmptyPrefixItemsException() {
        Stream<DynamicNode> dynamicNodeStream = createTests(SpecVersion.VersionFlag.V7, "src/test/resources/prefixItemsException");
        dynamicNodeStream.forEach(
                dynamicNode -> {
                    assertThrows(JsonSchemaException.class, () -> {
                        ((DynamicContainer) dynamicNode).getChildren().forEach(dynamicNode1 -> {
                        });
                    });
                }
        );
    }

    /**
     * Tests that the message contains the correct values when there are invalid
     * items.
     */
    @Test
    void messageInvalid() {
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://www.example.org/schema\",\r\n"
                + "  \"prefixItems\": [{\"type\": \"string\"},{\"type\": \"integer\"}]"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        String inputData = "[1, \"x\"]";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertFalse(messages.isEmpty());
        ValidationMessage message = messages.iterator().next();
        assertEquals("/prefixItems/0/type", message.getEvaluationPath().toString());
        assertEquals("https://www.example.org/schema#/prefixItems/0/type", message.getSchemaLocation().toString());
        assertEquals("/0", message.getInstanceLocation().toString());
        assertEquals("\"string\"", message.getSchemaNode().toString());
        assertEquals("1", message.getInstanceNode().toString());
        assertEquals("/0: integer found, string expected", message.getMessage());
        assertNull(message.getProperty());
    }

    /**
     * Tests that the message contains the correct values when there are invalid
     * items.
     */
    @Test
    void messageValid() {
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://www.example.org/schema\",\r\n"
                + "  \"prefixItems\": [{\"type\": \"string\"},{\"type\": \"integer\"}]"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        String inputData = "[\"x\", 1, 1]";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertTrue(messages.isEmpty());
    }

    /**
     * Tests that the message contains the correct values when there are invalid
     * items.
     */
    @Test
    void messageInvalidAdditionalItems() {
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://www.example.org/schema\",\r\n"
                + "  \"prefixItems\": [{\"type\": \"string\"},{\"type\": \"integer\"}],\r\n"
                + "  \"items\": false"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        String inputData = "[\"x\", 1, 1, 2]";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertFalse(messages.isEmpty());
        ValidationMessage message = messages.iterator().next();
        assertEquals("/items", message.getEvaluationPath().toString());
        assertEquals("https://www.example.org/schema#/items", message.getSchemaLocation().toString());
        assertEquals("", message.getInstanceLocation().toString());
        assertEquals("false", message.getSchemaNode().toString());
        assertEquals("[\"x\",1,1,2]", message.getInstanceNode().toString());
        assertEquals(": index '2' is not defined in the schema and the schema does not allow additional items", message.getMessage());
        assertNull(message.getProperty());
    }

    @Test
    void walkNull() {
        String schemaData = "{\n"
                + "  \"prefixItems\": [\n"
                + "    {\n"
                + "      \"type\": \"integer\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"type\": \"string\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"type\": \"integer\"\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().itemWalkListener(new JsonSchemaWalkListener() {
            @Override
            public WalkFlow onWalkStart(WalkEvent walkEvent) {
                return WalkFlow.CONTINUE;
            }

            @Override
            public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages) {
                @SuppressWarnings("unchecked")
                List<WalkEvent> items = (List<WalkEvent>) walkEvent.getExecutionContext()
                        .getCollectorContext()
                        .getCollectorMap()
                        .computeIfAbsent("items", key -> new ArrayList<JsonNodePath>());
                items.add(walkEvent);
            }
        }).build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        ValidationResult result = schema.walk(null, true);
        assertTrue(result.getValidationMessages().isEmpty());
        
        @SuppressWarnings("unchecked")
        List<WalkEvent> items = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("items");
        assertEquals(3, items.size());
        assertEquals("/0", items.get(0).getInstanceLocation().toString());
        assertEquals("prefixItems", items.get(0).getKeyword());
        assertEquals("#/prefixItems/0", items.get(0).getSchema().getSchemaLocation().toString());
        assertEquals("/1", items.get(1).getInstanceLocation().toString());
        assertEquals("prefixItems", items.get(1).getKeyword());
        assertEquals("#/prefixItems/1", items.get(1).getSchema().getSchemaLocation().toString());
        assertEquals("/2", items.get(2).getInstanceLocation().toString());
        assertEquals("prefixItems", items.get(2).getKeyword());
        assertEquals("#/prefixItems/2", items.get(2).getSchema().getSchemaLocation().toString());
    }

    @Test
    void walkDefaults() throws JsonProcessingException {
        String schemaData = "{\n"
                + "  \"prefixItems\": [\n"
                + "    {\n"
                + "      \"type\": \"integer\",\n"
                + "      \"default\": 1\n"
                + "    },\n"
                + "    {\n"
                + "      \"type\": \"string\",\n"
                + "      \"default\": \"2\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"type\": \"integer\",\n"
                + "      \"default\": 3\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                .applyDefaultsStrategy(new ApplyDefaultsStrategy(true, true, true))
                .itemWalkListener(new JsonSchemaWalkListener() {

                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages) {
                        @SuppressWarnings("unchecked")
                        List<WalkEvent> items = (List<WalkEvent>) walkEvent.getExecutionContext()
                                .getCollectorContext()
                                .getCollectorMap()
                                .computeIfAbsent("items", key -> new ArrayList<JsonNodePath>());
                        items.add(walkEvent);
                    }
                })
                .build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        JsonNode input = JsonMapperFactory.getInstance().readTree("[null, null]");
        ValidationResult result = schema.walk(input, true);
        assertTrue(result.getValidationMessages().isEmpty());
        
        @SuppressWarnings("unchecked")
        List<WalkEvent> items = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("items");
        assertEquals(3, items.size());
        assertEquals("/0", items.get(0).getInstanceLocation().toString());
        assertEquals("prefixItems", items.get(0).getKeyword());
        assertEquals("#/prefixItems/0", items.get(0).getSchema().getSchemaLocation().toString());
        assertEquals("/1", items.get(1).getInstanceLocation().toString());
        assertEquals("prefixItems", items.get(1).getKeyword());
        assertEquals("#/prefixItems/1", items.get(1).getSchema().getSchemaLocation().toString());
        assertEquals("/2", items.get(2).getInstanceLocation().toString());
        assertEquals("prefixItems", items.get(2).getKeyword());
        assertEquals("#/prefixItems/2", items.get(2).getSchema().getSchemaLocation().toString());
    }
}
