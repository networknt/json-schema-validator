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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;

/**
 * ItemsValidatorTest.
 */
class ItemsValidator202012Test {
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
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
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

    @Test
    void walkNull() {
        String schemaData = "{\r\n"
                + "  \"items\": {\r\n"
                + "    \"type\": \"string\"\r\n"
                + "  }\r\n"
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
        assertEquals(1, items.size());
        assertEquals("/0", items.get(0).getInstanceLocation().toString());
    }

    @Test
    void walkNullPrefixItems() {
        String schemaData = "{\r\n"
                + "  \"prefixItems\": [\r\n"
                + "    {\r\n"
                + "      \"type\": \"integer\"\r\n"
                + "    }\r\n"
                + "  ],\r\n"
                + "  \"items\": {\r\n"
                + "    \"type\": \"string\"\r\n"
                + "  }\r\n"
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
        assertEquals(2, items.size());
        assertEquals("/0", items.get(0).getInstanceLocation().toString());
        assertEquals("prefixItems", items.get(0).getKeyword());
        assertEquals("/1", items.get(1).getInstanceLocation().toString());
        assertEquals("items", items.get(1).getKeyword());
    }
}
