/*
 * Copyright (c) 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.networknt.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * ItemsValidatorTest.
 */
public class ItemsValidatorTest {
    /**
     * Tests that the message contains the correct values when there are invalid
     * items.
     */
    @Test
    void messageInvalid() {
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://www.example.org/schema\",\r\n"
                + "  \"items\": {\"type\": \"integer\"}"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V201909);
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
        JsonSchema schema = factory.getSchema(schemaData, config);
        String inputData = "[1, \"x\"]";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertFalse(messages.isEmpty());
        ValidationMessage message = messages.iterator().next();
        assertEquals("/items/type", message.getEvaluationPath().toString());
        assertEquals("https://www.example.org/schema#/items/type", message.getSchemaLocation().toString());
        assertEquals("/1", message.getInstanceLocation().toString());
        assertEquals("\"integer\"", message.getSchemaNode().toString());
        assertEquals("\"x\"", message.getInstanceNode().toString());
        assertEquals("/1: string found, integer expected", message.getMessage());
        assertNull(message.getProperty());
    }
    
    /**
     * Tests that the message contains the correct values when there are invalid
     * items.
     */
    @Test
    void messageAdditionalItemsInvalid() {
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://www.example.org/schema\",\r\n"
                + "  \"items\": [{}],"
                + "  \"additionalItems\": {\"type\": \"integer\"}"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V201909);
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
        JsonSchema schema = factory.getSchema(schemaData, config);
        String inputData = "[ null, 2, 3, \"foo\" ]";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertFalse(messages.isEmpty());
        ValidationMessage message = messages.iterator().next();
        assertEquals("/additionalItems/type", message.getEvaluationPath().toString());
        assertEquals("https://www.example.org/schema#/additionalItems/type", message.getSchemaLocation().toString());
        assertEquals("/3", message.getInstanceLocation().toString());
        assertEquals("\"integer\"", message.getSchemaNode().toString());
        assertEquals("\"foo\"", message.getInstanceNode().toString());
        assertEquals("/3: string found, integer expected", message.getMessage());
        assertNull(message.getProperty());
    }
    
    /**
     * Tests that the message contains the correct values when there are invalid
     * items.
     */
    @Test
    void messageAdditionalItemsFalseInvalid() {
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://www.example.org/schema\",\r\n"
                + "  \"items\": [{}],"
                + "  \"additionalItems\": false"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V201909);
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
        JsonSchema schema = factory.getSchema(schemaData, config);
        String inputData = "[ null, 2, 3, \"foo\" ]";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertFalse(messages.isEmpty());
        ValidationMessage message = messages.iterator().next();
        assertEquals("/additionalItems", message.getEvaluationPath().toString());
        assertEquals("https://www.example.org/schema#/additionalItems", message.getSchemaLocation().toString());
        assertEquals("", message.getInstanceLocation().toString());
        assertEquals("false", message.getSchemaNode().toString());
        assertEquals("[null,2,3,\"foo\"]", message.getInstanceNode().toString());
        assertEquals(": index '1' is not defined in the schema and the schema does not allow additional items", message.getMessage());
        assertNull(message.getProperty());
    }
}
