package com.networknt.schema.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStream;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
public class DefaultsTest {
    
    @Test
    void testGetDefaultNodeNotNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree("{\"default\": \"defaultValue\"}");
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V7);
        JsonSchema schema = factory.getSchema(node);
        JsonNode result = Defaults.getDefaultNode(schema);
        assertNotNull(result, "Default node should not be null");
    }

    @Test
    void testGetDefaultNodeWhenDefaultNotFound() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Create a JsonNode without a "default" field
        JsonNode node = mapper.readTree("{\"notDefault\": \"value\"}");
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V7);
        JsonSchema schema = factory.getSchema(node);
        JsonNode result = Defaults.getDefaultNode(schema);
        // Assert that the result is null, as there's no "default" node in the schema
        assertNull(result, "Default node should be null when 'default' node is not found in the schema");
    }

    @Test
    void testGetDefaultNodeWhenDefaultInRef() throws Exception {
        InputStream mainSchemaInputStream = DefaultsTest.class.getResourceAsStream("/mainSchema.json");
        InputStream referredSchemaInputStream = DefaultsTest.class.getResourceAsStream("/referredSchema.json");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode mainSchemaNode = mapper.readTree(mainSchemaInputStream);
        JsonNode referredSchemaNode = mapper.readTree(referredSchemaInputStream);
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V7);
        JsonSchema mainSchema = factory.getSchema(referredSchemaNode); // Use referred schema here
        JsonNode result = Defaults.getDefaultNode(mainSchema);
        assertNotNull(result, "Default node should not be null when 'default' node is in the referred schema");
        assertEquals("defaultValue", result.asText(), "Default node should have the default value in the referred schema");
    }   

}