package com.networknt.schema;

import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

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
public class PrefixItemsValidatorTest extends AbstractJsonSchemaTestSuite {

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
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
        JsonSchema schema = factory.getSchema(schemaData, config);
        String inputData = "[1, \"x\"]";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertFalse(messages.isEmpty());
        ValidationMessage message = messages.iterator().next();
        assertEquals("/prefixItems/type", message.getEvaluationPath().toString());
        assertEquals("https://www.example.org/schema#/prefixItems/type", message.getSchemaLocation().toString());
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
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
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
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
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
}
