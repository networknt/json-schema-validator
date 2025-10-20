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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.serialization.JsonMapperFactory;
import com.networknt.schema.walk.ApplyDefaultsStrategy;
import com.networknt.schema.walk.ItemWalkHandler;
import com.networknt.schema.walk.WalkListener;
import com.networknt.schema.walk.WalkConfig;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;

/**
 * ItemsLegacyValidatorTest.
 */
class ItemsLegacyValidatorTest {
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
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2019_09);
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
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2019_09);
        Schema schema = factory.getSchema(schemaData);
        String inputData = "[ null, 2, 3, \"foo\" ]";
        List<Error> messages = schema.validate(inputData, InputFormat.JSON);
        assertFalse(messages.isEmpty());
        Error message = messages.iterator().next();
        assertEquals("/additionalItems/type", message.getEvaluationPath().toString());
        assertEquals("https://www.example.org/schema#/additionalItems/type", message.getSchemaLocation().toString());
        assertEquals("/3", message.getInstanceLocation().toString());
        assertEquals("\"integer\"", message.getSchemaNode().toString());
        assertEquals("\"foo\"", message.getInstanceNode().toString());
        assertEquals("/3: string found, integer expected", message.toString());
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
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2019_09);
        Schema schema = factory.getSchema(schemaData);
        String inputData = "[ null, 2, 3, \"foo\" ]";
        List<Error> messages = schema.validate(inputData, InputFormat.JSON);
        assertFalse(messages.isEmpty());
        Error message = messages.iterator().next();
        assertEquals("/additionalItems", message.getEvaluationPath().toString());
        assertEquals("https://www.example.org/schema#/additionalItems", message.getSchemaLocation().toString());
        assertEquals("", message.getInstanceLocation().toString());
        assertEquals("false", message.getSchemaNode().toString());
        assertEquals("[null,2,3,\"foo\"]", message.getInstanceNode().toString());
        assertEquals(": index '1' is not defined in the schema and the schema does not allow additional items", message.toString());
        assertNull(message.getProperty());
    }

    @Test
    void walk() {
        String schemaData = "{\r\n"
                + "  \"items\": {\r\n"
                + "    \"type\": \"string\"\r\n"
                + "  }\r\n"
                + "}";
        ItemWalkHandler itemWalkHandler = ItemWalkHandler.builder()
				.itemWalkListener(new WalkListener() {
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
        WalkConfig walkConfig = WalkConfig.builder().itemWalkHandler(itemWalkHandler).build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2019_09);
        Schema schema = factory.getSchema(schemaData);
        Result result = schema.walk("[\"the\",\"quick\",\"brown\"]", InputFormat.JSON, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertTrue(result.getErrors().isEmpty());
        
        @SuppressWarnings("unchecked")
        List<WalkEvent> items = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("items");
        assertEquals(3, items.size());
        assertEquals("/0", items.get(0).getInstanceLocation().toString());
        assertEquals("/1", items.get(1).getInstanceLocation().toString());
        assertEquals("/2", items.get(2).getInstanceLocation().toString());
    }

    @Test
    void walkNull() {
        String schemaData = "{\r\n"
                + "  \"items\": {\r\n"
                + "    \"type\": \"string\"\r\n"
                + "  }\r\n"
                + "}";
        ItemWalkHandler itemWalkHandler = ItemWalkHandler.builder()
				.itemWalkListener(new WalkListener() {
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
        WalkConfig walkConfig = WalkConfig.builder().itemWalkHandler(itemWalkHandler).build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2019_09);
        Schema schema = factory.getSchema(schemaData);
        Result result = schema.walk(null, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertTrue(result.getErrors().isEmpty());
        
        @SuppressWarnings("unchecked")
        List<WalkEvent> items = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("items");
        assertEquals(1, items.size());
        assertEquals("/0", items.get(0).getInstanceLocation().toString());
    }

    @Test
    void walkNullTupleItemsAdditional() {
        String schemaData = "{\r\n"
                + "  \"items\": [\r\n"
                + "    {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    }\r\n,"
                + "    {\r\n"
                + "      \"type\": \"integer\"\r\n"
                + "    }\r\n"
                + "  ],\r\n"
                + "  \"additionalItems\": {\r\n"
                + "    \"type\": \"string\"\r\n"
                + "  }\r\n"
                + "}";
        ItemWalkHandler itemWalkHandler = ItemWalkHandler.builder()
				.itemWalkListener(new WalkListener() {
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
        WalkConfig walkConfig = WalkConfig.builder().itemWalkHandler(itemWalkHandler).build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2019_09);
        Schema schema = factory.getSchema(schemaData);
        Result result = schema.walk(null, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertTrue(result.getErrors().isEmpty());

        @SuppressWarnings("unchecked")
        List<WalkEvent> items = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("items");
        assertEquals(3, items.size());
        assertEquals("/0", items.get(0).getInstanceLocation().toString());
        assertEquals("items", items.get(0).getKeyword());
        assertNull(items.get(0).getInstanceNode());
        assertEquals("/1", items.get(1).getInstanceLocation().toString());
        assertEquals("items", items.get(1).getKeyword());
        assertNull(items.get(1).getInstanceNode());
        assertEquals("/2", items.get(2).getInstanceLocation().toString());
        assertEquals("additionalItems", items.get(2).getKeyword());
        assertNull(items.get(2).getInstanceNode());
    }

    @Test
    void walkTupleItemsAdditional() throws JsonProcessingException {
        String schemaData = "{\r\n"
                + "  \"items\": [\r\n"
                + "    {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    }\r\n,"
                + "    {\r\n"
                + "      \"type\": \"integer\"\r\n"
                + "    }\r\n"
                + "  ],\r\n"
                + "  \"additionalItems\": {\r\n"
                + "    \"type\": \"string\"\r\n"
                + "  }\r\n"
                + "}";
        ItemWalkHandler itemWalkHandler = ItemWalkHandler.builder()
				.itemWalkListener(new WalkListener() {
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
		WalkConfig walkConfig = WalkConfig.builder().itemWalkHandler(itemWalkHandler)
				.build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2019_09);
        Schema schema = factory.getSchema(schemaData);
        JsonNode input = JsonMapperFactory.getInstance().readTree("[\"hello\"]");
        Result result = schema.walk(input, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertTrue(result.getErrors().isEmpty());

        @SuppressWarnings("unchecked")
        List<WalkEvent> items = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("items");
        assertEquals(3, items.size());
        assertEquals("/0", items.get(0).getInstanceLocation().toString());
        assertEquals("items", items.get(0).getKeyword());
        assertEquals("hello", items.get(0).getInstanceNode().textValue());
        assertEquals("/1", items.get(1).getInstanceLocation().toString());
        assertEquals("items", items.get(1).getKeyword());
        assertNull(items.get(1).getInstanceNode());
        assertEquals("/2", items.get(2).getInstanceLocation().toString());
        assertEquals("additionalItems", items.get(2).getKeyword());
        assertNull(items.get(2).getInstanceNode());
    }

    @Test
    void walkTupleItemsAdditionalDefaults() throws JsonProcessingException {
        String schemaData = "{\r\n"
                + "  \"items\": [\r\n"
                + "    {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"default\": \"1\"\r\n"
                + "    },\r\n"
                + "    {\r\n"
                + "      \"type\": \"integer\",\r\n"
                + "      \"default\": 2\r\n"
                + "    }\r\n"
                + "  ],\r\n"
                + "  \"additionalItems\": {\r\n"
                + "    \"type\": \"string\",\r\n"
                + "    \"default\": \"additional\"\r\n"
                + "  }\r\n"
                + "}";
        
		ItemWalkHandler itemWalkHandler = ItemWalkHandler.builder()
				.itemWalkListener(new WalkListener() {
					@Override
					public WalkFlow onWalkStart(WalkEvent walkEvent) {
						return WalkFlow.CONTINUE;
					}

					@Override
					public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
						@SuppressWarnings("unchecked")
						List<WalkEvent> items = (List<WalkEvent>) walkEvent.getExecutionContext().getCollectorContext()
								.getData().computeIfAbsent("items", key -> new ArrayList<NodePath>());
						items.add(walkEvent);
					}
				}).build();
		WalkConfig walkConfig = WalkConfig.builder().itemWalkHandler(itemWalkHandler)
				.applyDefaultsStrategy(new ApplyDefaultsStrategy(true, true, true)).build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2019_09);
        Schema schema = factory.getSchema(schemaData);
        JsonNode input = JsonMapperFactory.getInstance().readTree("[null, null, null, null]");
        Result result = schema.walk(input, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertTrue(result.getErrors().isEmpty());

        @SuppressWarnings("unchecked")
        List<WalkEvent> items = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("items");
        assertEquals(4, items.size());
        assertEquals("/0", items.get(0).getInstanceLocation().toString());
        assertEquals("items", items.get(0).getKeyword());
        assertEquals("1", items.get(0).getInstanceNode().textValue());
        assertEquals("/1", items.get(1).getInstanceLocation().toString());
        assertEquals("items", items.get(1).getKeyword());
        assertEquals(2, items.get(1).getInstanceNode().intValue());
        assertEquals("/2", items.get(2).getInstanceLocation().toString());
        assertEquals("additionalItems", items.get(2).getKeyword());
        assertEquals("additional", items.get(2).getInstanceNode().asText());
        assertEquals("/3", items.get(3).getInstanceLocation().toString());
        assertEquals("additionalItems", items.get(3).getKeyword());
        assertEquals("additional", items.get(3).getInstanceNode().asText());
    }
}
