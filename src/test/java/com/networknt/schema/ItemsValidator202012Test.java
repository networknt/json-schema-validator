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

import org.junit.jupiter.api.Test;

import com.networknt.schema.walk.ItemWalkListenerRunner;
import com.networknt.schema.walk.WalkListener;
import com.networknt.schema.walk.WalkConfig;
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
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        String inputData = "[1, \"x\"]";
        List<Error> messages = schema.validate(inputData, InputFormat.JSON);
        assertFalse(messages.isEmpty());
        Error message = messages.iterator().next();
        assertEquals("/items/type", message.getEvaluationPath().toString());
        assertEquals("https://www.example.org/schema#/items/type", message.getSchemaLocation().toString());
        assertEquals("/1", message.getInstanceLocation().toString());
        assertEquals("\"integer\"", message.getSchemaNode().toString());
        assertEquals("\"x\"", message.getInstanceNode().toString());
        assertEquals("/1: string found, integer expected", message.toString());
        assertNull(message.getProperty());
    }

    @Test
    void walkNull() {
        String schemaData = "{\r\n"
                + "  \"items\": {\r\n"
                + "    \"type\": \"string\"\r\n"
                + "  }\r\n"
                + "}";
        ItemWalkListenerRunner itemWalkListenerRunner = ItemWalkListenerRunner.builder().itemWalkListener(new WalkListener() {
            @Override
            public WalkFlow onWalkStart(WalkEvent walkEvent) {
                return WalkFlow.CONTINUE;
            }

            @Override
            public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                @SuppressWarnings("unchecked")
                List<WalkEvent> items = (List<WalkEvent>) walkEvent.getExecutionContext()
                        .getCollectorContext()
                        .getData()
                        .computeIfAbsent("items", key -> new ArrayList<NodePath>());
                items.add(walkEvent);
            }
        }).build();
        WalkConfig walkConfig = WalkConfig.builder()
                .itemWalkListenerRunner(itemWalkListenerRunner)
                .build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        Result result = schema.walk(null, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertTrue(result.getErrors().isEmpty());
        
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
        ItemWalkListenerRunner itemWalkListenerRunner = ItemWalkListenerRunner.builder().itemWalkListener(new WalkListener() {
            @Override
            public WalkFlow onWalkStart(WalkEvent walkEvent) {
                return WalkFlow.CONTINUE;
            }

            @Override
            public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                @SuppressWarnings("unchecked")
                List<WalkEvent> items = (List<WalkEvent>) walkEvent.getExecutionContext()
                        .getCollectorContext()
                        .getData()
                        .computeIfAbsent("items", key -> new ArrayList<NodePath>());
                items.add(walkEvent);
            }
        }).build();
        WalkConfig walkConfig = WalkConfig.builder()
                .itemWalkListenerRunner(itemWalkListenerRunner)
                .build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        Result result = schema.walk(null, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertTrue(result.getErrors().isEmpty());
        
        @SuppressWarnings("unchecked")
        List<WalkEvent> items = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("items");
        assertEquals(2, items.size());
        assertEquals("/0", items.get(0).getInstanceLocation().toString());
        assertEquals("prefixItems", items.get(0).getKeyword());
        assertEquals("/1", items.get(1).getInstanceLocation().toString());
        assertEquals("items", items.get(1).getKeyword());
    }
}
