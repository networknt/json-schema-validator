/*
 * Copyright (c) 2023 the original author or authors.
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
 * AdditionalPropertiesValidatorTest.
 */
class AdditionalPropertiesValidatorTest {
    /**
     * Tests that the message contains the correct values when additional properties
     * schema is false.
     */
    @Test
    void messageFalse() {
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://www.example.org/schema\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"foo\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"additionalProperties\": false\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        String inputData = "{\r\n"
                + "  \"foo\":\"hello\",\r\n"
                + "  \"bar\":\"world\"\r\n"
                + "}";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertFalse(messages.isEmpty());
        ValidationMessage message = messages.iterator().next();
        assertEquals("/additionalProperties", message.getEvaluationPath().toString());
        assertEquals("https://www.example.org/schema#/additionalProperties", message.getSchemaLocation().toString());
        assertEquals("", message.getInstanceLocation().toString());
        assertEquals("false", message.getSchemaNode().toString());
        assertEquals("{\"foo\":\"hello\",\"bar\":\"world\"}", message.getInstanceNode().toString());
        assertEquals(": property 'bar' is not defined in the schema and the schema does not allow additional properties", message.getMessage());
        assertEquals("bar", message.getProperty());
    }
    
    /**
     * Tests that the message contains the correct values when additional properties
     * schema has a schema with type.
     */
    @Test
    void messageSchema() {
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://www.example.org/schema\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"foo\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"additionalProperties\": { \"type\": \"number\" }\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        String inputData = "{\r\n"
                + "  \"foo\":\"hello\",\r\n"
                + "  \"bar\":\"world\"\r\n"
                + "}";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertFalse(messages.isEmpty());
        ValidationMessage message = messages.iterator().next();
        assertEquals("/additionalProperties/type", message.getEvaluationPath().toString());
        assertEquals("https://www.example.org/schema#/additionalProperties/type", message.getSchemaLocation().toString());
        assertEquals("/bar", message.getInstanceLocation().toString());
        assertEquals("\"number\"", message.getSchemaNode().toString());
        assertEquals("\"world\"", message.getInstanceNode().toString());
        assertEquals("/bar: string found, number expected", message.getMessage());
        assertNull(message.getProperty());
    }

}
