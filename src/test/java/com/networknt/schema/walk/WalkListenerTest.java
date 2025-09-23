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
package com.networknt.schema.walk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.ApplyDefaultsStrategy;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRef;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.Specification.Version;
import com.networknt.schema.dialect.DialectId;
import com.networknt.schema.keyword.ItemsValidator;
import com.networknt.schema.keyword.ItemsValidator202012;
import com.networknt.schema.keyword.PropertiesValidator;
import com.networknt.schema.keyword.ValidatorTypeCode;
import com.networknt.schema.serialization.JsonMapperFactory;
import com.networknt.schema.utils.JsonNodes;
import com.networknt.schema.utils.SchemaRefs;
import com.networknt.schema.Error;
import com.networknt.schema.ValidationResult;

/**
 * JsonSchemaWalkListenerTest.
 */
class WalkListenerTest {

    @Test
    void keywordListener() {
        String schemaData = "{\r\n"
                + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"description\": \"Default Description\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"tags\": {\r\n"
                + "      \"type\": \"array\",\r\n"
                + "      \"items\": {\r\n"
                + "        \"$ref\": \"#/definitions/tag\"\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"definitions\": {\r\n"
                + "    \"tag\": {\r\n"
                + "      \"properties\": {\r\n"
                + "        \"name\": {\r\n"
                + "          \"type\": \"string\"\r\n"
                + "        },\r\n"
                + "        \"description\": {\r\n"
                + "          \"type\": \"string\"\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        KeywordWalkListenerRunner keywordWalkListenerRunner = KeywordWalkListenerRunner.builder()
                .keywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(), new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        @SuppressWarnings("unchecked")
                        List<WalkEvent> propertyKeywords = (List<WalkEvent>) walkEvent.getExecutionContext()
                                .getCollectorContext()
                                .getData()
                                .computeIfAbsent("propertyKeywords", key -> new ArrayList<>());
                        propertyKeywords.add(walkEvent);
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                    }
                })
                .build();
        Schema schema = SchemaRegistry.withDefaultDialect(Version.DRAFT_7).getSchema(schemaData);
        String inputData = "{\r\n"
                + "  \"tags\": [\r\n"
                + "    {\r\n"
                + "      \"name\": \"image\",\r\n"
                + "      \"description\": \"An image\"\r\n"
                + "    },\r\n"
                + "    {\r\n"
                + "      \"name\": \"link\",\r\n"
                + "      \"description\": \"A link\"\r\n"
                + "    }\r\n"
                + "  ]\r\n"
                + "}";
        WalkConfig walkConfig = WalkConfig.builder()
                .keywordWalkListenerRunner(keywordWalkListenerRunner)
                .build();
        ValidationResult result = schema.walk(inputData, InputFormat.JSON, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertTrue(result.getErrors().isEmpty());
        @SuppressWarnings("unchecked")
        List<WalkEvent> propertyKeywords = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("propertyKeywords"); 
        assertEquals(3, propertyKeywords.size());
        assertEquals("properties", propertyKeywords.get(0).getValidator().getKeyword());
        assertEquals("", propertyKeywords.get(0).getInstanceLocation().toString());
        assertEquals("/properties", propertyKeywords.get(0).getSchema().getEvaluationPath()
                .append(propertyKeywords.get(0).getKeyword()).toString());
        assertEquals("/tags/0", propertyKeywords.get(1).getInstanceLocation().toString());
        assertEquals("image", propertyKeywords.get(1).getInstanceNode().get("name").asText());
        assertEquals("/properties/tags/items/$ref/properties",
                propertyKeywords.get(1).getValidator().getEvaluationPath().toString());
        assertEquals("/properties/tags/items/$ref/properties", propertyKeywords.get(1).getSchema().getEvaluationPath()
                .append(propertyKeywords.get(1).getKeyword()).toString());
        assertEquals("/tags/1", propertyKeywords.get(2).getInstanceLocation().toString());
        assertEquals("/properties/tags/items/$ref/properties", propertyKeywords.get(2).getSchema().getEvaluationPath()
                .append(propertyKeywords.get(2).getKeyword()).toString());
        assertEquals("link", propertyKeywords.get(2).getInstanceNode().get("name").asText());
    }

    @Test
    void propertyListener() {
        String schemaData = "{\r\n"
                + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"description\": \"Default Description\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"tags\": {\r\n"
                + "      \"type\": \"array\",\r\n"
                + "      \"items\": {\r\n"
                + "        \"$ref\": \"#/definitions/tag\"\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"definitions\": {\r\n"
                + "    \"tag\": {\r\n"
                + "      \"properties\": {\r\n"
                + "        \"name\": {\r\n"
                + "          \"type\": \"string\"\r\n"
                + "        },\r\n"
                + "        \"description\": {\r\n"
                + "          \"type\": \"string\"\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        
        PropertyWalkListenerRunner propertyWalkListenerRunner = PropertyWalkListenerRunner.builder()
                .propertyWalkListener(new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        @SuppressWarnings("unchecked")
                        List<WalkEvent> properties = (List<WalkEvent>) walkEvent.getExecutionContext()
                                .getCollectorContext()
                                .getData()
                                .computeIfAbsent("properties", key -> new ArrayList<>());
                        properties.add(walkEvent);
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                    }
                })
                .build();
        Schema schema = SchemaRegistry.withDefaultDialect(Version.DRAFT_7).getSchema(schemaData);
        String inputData = "{\r\n"
                + "  \"tags\": [\r\n"
                + "    {\r\n"
                + "      \"name\": \"image\",\r\n"
                + "      \"description\": \"An image\"\r\n"
                + "    },\r\n"
                + "    {\r\n"
                + "      \"name\": \"link\",\r\n"
                + "      \"description\": \"A link\"\r\n"
                + "    }\r\n"
                + "  ]\r\n"
                + "}";
        WalkConfig walkConfig = WalkConfig.builder()
                .propertyWalkListenerRunner(propertyWalkListenerRunner)
                .build();
        ValidationResult result = schema.walk(inputData, InputFormat.JSON, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertTrue(result.getErrors().isEmpty());

        @SuppressWarnings("unchecked")
        List<WalkEvent> properties = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("properties");
        assertEquals(5, properties.size());
        assertEquals("properties", properties.get(0).getValidator().getKeyword());

        assertEquals("/tags", properties.get(0).getInstanceLocation().toString());
        assertEquals("/properties/tags", properties.get(0).getSchema().getEvaluationPath().toString());

        assertEquals("/tags/0/name", properties.get(1).getInstanceLocation().toString());
        assertEquals("image", properties.get(1).getInstanceNode().asText());
        assertEquals("/properties/tags/items/$ref/properties/name", properties.get(1).getSchema().getEvaluationPath().toString());

        assertEquals("/tags/0/description", properties.get(2).getInstanceLocation().toString());
        assertEquals("An image", properties.get(2).getInstanceNode().asText());
        assertEquals("/properties/tags/items/$ref/properties/description", properties.get(2).getSchema().getEvaluationPath().toString());

        assertEquals("/tags/1/name", properties.get(3).getInstanceLocation().toString());
        assertEquals("link", properties.get(3).getInstanceNode().asText());
        assertEquals("/properties/tags/items/$ref/properties/name", properties.get(3).getSchema().getEvaluationPath().toString());

        assertEquals("/tags/1/description", properties.get(4).getInstanceLocation().toString());
        assertEquals("A link", properties.get(4).getInstanceNode().asText());
        assertEquals("/properties/tags/items/$ref/properties/description", properties.get(4).getSchema().getEvaluationPath().toString());
    }

    @Test
    void itemsListener() {
        String schemaData = "{\r\n"
                + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"description\": \"Default Description\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"tags\": {\r\n"
                + "      \"type\": \"array\",\r\n"
                + "      \"items\": {\r\n"
                + "        \"$ref\": \"#/definitions/tag\"\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"definitions\": {\r\n"
                + "    \"tag\": {\r\n"
                + "      \"properties\": {\r\n"
                + "        \"name\": {\r\n"
                + "          \"type\": \"string\"\r\n"
                + "        },\r\n"
                + "        \"description\": {\r\n"
                + "          \"type\": \"string\"\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        ItemWalkListenerRunner itemWalkListenerRunner = ItemWalkListenerRunner.builder().itemWalkListener(new WalkListener() {
            @Override
            public WalkFlow onWalkStart(WalkEvent walkEvent) {
                @SuppressWarnings("unchecked")
                List<WalkEvent> items = (List<WalkEvent>) walkEvent.getExecutionContext()
                        .getCollectorContext()
                        .getData()
                        .computeIfAbsent("items", key -> new ArrayList<>());
                items.add(walkEvent);
                return WalkFlow.CONTINUE;
            }

            @Override
            public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
            }
        }).build();
        Schema schema = SchemaRegistry.withDefaultDialect(Version.DRAFT_7).getSchema(schemaData);
        String inputData = "{\r\n"
                + "  \"tags\": [\r\n"
                + "    {\r\n"
                + "      \"name\": \"image\",\r\n"
                + "      \"description\": \"An image\"\r\n"
                + "    },\r\n"
                + "    {\r\n"
                + "      \"name\": \"link\",\r\n"
                + "      \"description\": \"A link\"\r\n"
                + "    }\r\n"
                + "  ]\r\n"
                + "}";
        WalkConfig walkConfig = WalkConfig.builder()
                .itemWalkListenerRunner(itemWalkListenerRunner)
                .build();

        ValidationResult result = schema.walk(inputData, InputFormat.JSON, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertTrue(result.getErrors().isEmpty());

        @SuppressWarnings("unchecked")
        List<WalkEvent> items = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("items");
        assertEquals(2, items.size());
        assertEquals("items", items.get(0).getValidator().getKeyword());
	    assertInstanceOf(ItemsValidator.class, items.get(0).getValidator());

        assertEquals("/tags/0", items.get(0).getInstanceLocation().toString());
        assertEquals("/properties/tags/items", items.get(0).getSchema().getEvaluationPath().toString());

        assertEquals("/tags/1", items.get(1).getInstanceLocation().toString());
        assertEquals("/properties/tags/items", items.get(1).getSchema().getEvaluationPath().toString());
    }

    @Test
    void items202012Listener() {
        String schemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"description\": \"Default Description\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"tags\": {\r\n"
                + "      \"type\": \"array\",\r\n"
                + "      \"items\": {\r\n"
                + "        \"$ref\": \"#/definitions/tag\"\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"definitions\": {\r\n"
                + "    \"tag\": {\r\n"
                + "      \"properties\": {\r\n"
                + "        \"name\": {\r\n"
                + "          \"type\": \"string\"\r\n"
                + "        },\r\n"
                + "        \"description\": {\r\n"
                + "          \"type\": \"string\"\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        ItemWalkListenerRunner itemWalkListenerRunner = ItemWalkListenerRunner.builder().itemWalkListener(new WalkListener() {
            @Override
            public WalkFlow onWalkStart(WalkEvent walkEvent) {
                @SuppressWarnings("unchecked")
                List<WalkEvent> items = (List<WalkEvent>) walkEvent.getExecutionContext()
                        .getCollectorContext()
                        .getData()
                        .computeIfAbsent("items", key -> new ArrayList<>());
                items.add(walkEvent);
                return WalkFlow.CONTINUE;
            }

            @Override
            public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
            }
        }).build();
        Schema schema = SchemaRegistry.withDefaultDialect(Version.DRAFT_7).getSchema(schemaData);
        String inputData = "{\r\n"
                + "  \"tags\": [\r\n"
                + "    {\r\n"
                + "      \"name\": \"image\",\r\n"
                + "      \"description\": \"An image\"\r\n"
                + "    },\r\n"
                + "    {\r\n"
                + "      \"name\": \"link\",\r\n"
                + "      \"description\": \"A link\"\r\n"
                + "    }\r\n"
                + "  ]\r\n"
                + "}";
        WalkConfig walkConfig = WalkConfig.builder()
                .itemWalkListenerRunner(itemWalkListenerRunner)
                .build();
        ValidationResult result = schema.walk(inputData, InputFormat.JSON, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertTrue(result.getErrors().isEmpty());

        @SuppressWarnings("unchecked")
        List<WalkEvent> items = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("items");
        assertEquals(2, items.size());
        assertEquals("items", items.get(0).getValidator().getKeyword());
	    assertInstanceOf(ItemsValidator202012.class, items.get(0).getValidator());

        assertEquals("/tags/0", items.get(0).getInstanceLocation().toString());
        assertEquals("/properties/tags/items", items.get(0).getSchema().getEvaluationPath().toString());

        assertEquals("/tags/1", items.get(1).getInstanceLocation().toString());
        assertEquals("/properties/tags/items", items.get(1).getSchema().getEvaluationPath().toString());
    }

    @Test
    void draft201909() {
    	KeywordWalkListenerRunner keywordWalkListenerRunner = KeywordWalkListenerRunner.builder()
                .keywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(), new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        @SuppressWarnings("unchecked")
                        List<WalkEvent> propertyKeywords = (List<WalkEvent>) walkEvent.getExecutionContext()
                                .getCollectorContext()
                                .getData()
                                .computeIfAbsent("propertyKeywords", key -> new ArrayList<>());
                        propertyKeywords.add(walkEvent);
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                    }
                })
                .build();
        Schema schema = SchemaRegistry.withDefaultDialect(Version.DRAFT_2019_09)
                .getSchema(SchemaLocation.of(DialectId.DRAFT_2019_09));

        String inputData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2019-09/schema\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"kebab-case\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"snake_case\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    \"a\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        WalkConfig walkConfig = WalkConfig.builder()
                .keywordWalkListenerRunner(keywordWalkListenerRunner)
                .build();
        ValidationResult result = schema.walk(inputData, InputFormat.JSON, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertTrue(result.getErrors().isEmpty());

        List<WalkEvent> propertyKeywords = result.getExecutionContext().getCollectorContext().get("propertyKeywords");
        
        assertEquals(28, propertyKeywords.size());

        assertEquals("", propertyKeywords.get(0).getInstanceLocation().toString());
        assertEquals("/properties", propertyKeywords.get(0).getSchema().getEvaluationPath().append(propertyKeywords.get(0).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/schema#/properties", propertyKeywords.get(0).getSchema().getSchemaLocation().append(propertyKeywords.get(0).getKeyword()).toString());

        assertEquals("", propertyKeywords.get(1).getInstanceLocation().toString());
        assertEquals("/allOf/0/$ref/properties", propertyKeywords.get(1).getSchema().getEvaluationPath().append(propertyKeywords.get(1).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/core#/properties", propertyKeywords.get(1).getSchema().getSchemaLocation().append(propertyKeywords.get(1).getKeyword()).toString());

        assertEquals("", propertyKeywords.get(2).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties", propertyKeywords.get(2).getSchema().getEvaluationPath().append(propertyKeywords.get(2).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/applicator#/properties", propertyKeywords.get(2).getSchema().getSchemaLocation().append(propertyKeywords.get(2).getKeyword()).toString());

        assertEquals("/properties/kebab-case", propertyKeywords.get(3).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/properties", propertyKeywords.get(3).getSchema().getEvaluationPath().append(propertyKeywords.get(3).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/schema#/properties", propertyKeywords.get(3).getSchema().getSchemaLocation().append(propertyKeywords.get(3).getKeyword()).toString());

        assertEquals("/properties/kebab-case", propertyKeywords.get(4).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/0/$ref/properties", propertyKeywords.get(4).getSchema().getEvaluationPath().append(propertyKeywords.get(4).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/core#/properties", propertyKeywords.get(4).getSchema().getSchemaLocation().append(propertyKeywords.get(4).getKeyword()).toString());

        assertEquals("/properties/kebab-case", propertyKeywords.get(5).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/1/$ref/properties", propertyKeywords.get(5).getSchema().getEvaluationPath().append(propertyKeywords.get(5).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/applicator#/properties", propertyKeywords.get(5).getSchema().getSchemaLocation().append(propertyKeywords.get(5).getKeyword()).toString());

        assertEquals("/properties/kebab-case", propertyKeywords.get(6).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/2/$ref/properties", propertyKeywords.get(6).getSchema().getEvaluationPath().append(propertyKeywords.get(6).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/validation#/properties", propertyKeywords.get(6).getSchema().getSchemaLocation().append(propertyKeywords.get(6).getKeyword()).toString());

        assertEquals("/properties/kebab-case", propertyKeywords.get(7).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/3/$ref/properties", propertyKeywords.get(7).getSchema().getEvaluationPath().append(propertyKeywords.get(7).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/meta-data#/properties", propertyKeywords.get(7).getSchema().getSchemaLocation().append(propertyKeywords.get(7).getKeyword()).toString());

        assertEquals("/properties/kebab-case", propertyKeywords.get(8).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/4/$ref/properties", propertyKeywords.get(8).getSchema().getEvaluationPath().append(propertyKeywords.get(8).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/format#/properties", propertyKeywords.get(8).getSchema().getSchemaLocation().append(propertyKeywords.get(8).getKeyword()).toString());

        assertEquals("/properties/kebab-case", propertyKeywords.get(9).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/5/$ref/properties", propertyKeywords.get(9).getSchema().getEvaluationPath().append(propertyKeywords.get(9).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/content#/properties", propertyKeywords.get(9).getSchema().getSchemaLocation().append(propertyKeywords.get(9).getKeyword()).toString());

        assertEquals("/properties/snake_case", propertyKeywords.get(10).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/properties", propertyKeywords.get(10).getSchema().getEvaluationPath().append(propertyKeywords.get(10).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/schema#/properties", propertyKeywords.get(10).getSchema().getSchemaLocation().append(propertyKeywords.get(10).getKeyword()).toString());

        assertEquals("/properties/snake_case", propertyKeywords.get(11).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/0/$ref/properties", propertyKeywords.get(11).getSchema().getEvaluationPath().append(propertyKeywords.get(11).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/core#/properties", propertyKeywords.get(11).getSchema().getSchemaLocation().append(propertyKeywords.get(11).getKeyword()).toString());

        assertEquals("/properties/snake_case", propertyKeywords.get(12).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/1/$ref/properties", propertyKeywords.get(12).getSchema().getEvaluationPath().append(propertyKeywords.get(12).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/applicator#/properties", propertyKeywords.get(12).getSchema().getSchemaLocation().append(propertyKeywords.get(12).getKeyword()).toString());

        assertEquals("/properties/snake_case", propertyKeywords.get(13).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/2/$ref/properties", propertyKeywords.get(13).getSchema().getEvaluationPath().append(propertyKeywords.get(13).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/validation#/properties", propertyKeywords.get(13).getSchema().getSchemaLocation().append(propertyKeywords.get(13).getKeyword()).toString());

        assertEquals("/properties/snake_case", propertyKeywords.get(14).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/3/$ref/properties", propertyKeywords.get(14).getSchema().getEvaluationPath().append(propertyKeywords.get(14).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/meta-data#/properties", propertyKeywords.get(14).getSchema().getSchemaLocation().append(propertyKeywords.get(14).getKeyword()).toString());

        assertEquals("/properties/snake_case", propertyKeywords.get(15).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/4/$ref/properties", propertyKeywords.get(15).getSchema().getEvaluationPath().append(propertyKeywords.get(15).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/format#/properties", propertyKeywords.get(15).getSchema().getSchemaLocation().append(propertyKeywords.get(15).getKeyword()).toString());

        assertEquals("/properties/snake_case", propertyKeywords.get(16).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/5/$ref/properties", propertyKeywords.get(16).getSchema().getEvaluationPath().append(propertyKeywords.get(16).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/content#/properties", propertyKeywords.get(16).getSchema().getSchemaLocation().append(propertyKeywords.get(16).getKeyword()).toString());

        assertEquals("/properties/a", propertyKeywords.get(17).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/properties", propertyKeywords.get(17).getSchema().getEvaluationPath().append(propertyKeywords.get(17).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/schema#/properties", propertyKeywords.get(17).getSchema().getSchemaLocation().append(propertyKeywords.get(17).getKeyword()).toString());

        assertEquals("/properties/a", propertyKeywords.get(18).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/0/$ref/properties", propertyKeywords.get(18).getSchema().getEvaluationPath().append(propertyKeywords.get(18).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/core#/properties", propertyKeywords.get(18).getSchema().getSchemaLocation().append(propertyKeywords.get(18).getKeyword()).toString());

        assertEquals("/properties/a", propertyKeywords.get(19).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/1/$ref/properties", propertyKeywords.get(19).getSchema().getEvaluationPath().append(propertyKeywords.get(19).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/applicator#/properties", propertyKeywords.get(19).getSchema().getSchemaLocation().append(propertyKeywords.get(19).getKeyword()).toString());

        assertEquals("/properties/a", propertyKeywords.get(20).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/2/$ref/properties", propertyKeywords.get(20).getSchema().getEvaluationPath().append(propertyKeywords.get(20).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/validation#/properties", propertyKeywords.get(20).getSchema().getSchemaLocation().append(propertyKeywords.get(20).getKeyword()).toString());

        assertEquals("/properties/a", propertyKeywords.get(21).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/3/$ref/properties", propertyKeywords.get(21).getSchema().getEvaluationPath().append(propertyKeywords.get(21).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/meta-data#/properties", propertyKeywords.get(21).getSchema().getSchemaLocation().append(propertyKeywords.get(21).getKeyword()).toString());

        assertEquals("/properties/a", propertyKeywords.get(22).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/4/$ref/properties", propertyKeywords.get(22).getSchema().getEvaluationPath().append(propertyKeywords.get(22).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/format#/properties", propertyKeywords.get(22).getSchema().getSchemaLocation().append(propertyKeywords.get(22).getKeyword()).toString());

        assertEquals("/properties/a", propertyKeywords.get(23).getInstanceLocation().toString());
        assertEquals("/allOf/1/$ref/properties/properties/additionalProperties/$recursiveRef/allOf/5/$ref/properties", propertyKeywords.get(23).getSchema().getEvaluationPath().append(propertyKeywords.get(23).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/content#/properties", propertyKeywords.get(23).getSchema().getSchemaLocation().append(propertyKeywords.get(23).getKeyword()).toString());

        assertEquals("", propertyKeywords.get(24).getInstanceLocation().toString());
        assertEquals("/allOf/2/$ref/properties", propertyKeywords.get(24).getSchema().getEvaluationPath().append(propertyKeywords.get(24).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/validation#/properties", propertyKeywords.get(24).getSchema().getSchemaLocation().append(propertyKeywords.get(24).getKeyword()).toString());

        assertEquals("", propertyKeywords.get(25).getInstanceLocation().toString());
        assertEquals("/allOf/3/$ref/properties", propertyKeywords.get(25).getSchema().getEvaluationPath().append(propertyKeywords.get(25).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/meta-data#/properties", propertyKeywords.get(25).getSchema().getSchemaLocation().append(propertyKeywords.get(25).getKeyword()).toString());

        assertEquals("", propertyKeywords.get(26).getInstanceLocation().toString());
        assertEquals("/allOf/4/$ref/properties", propertyKeywords.get(26).getSchema().getEvaluationPath().append(propertyKeywords.get(26).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/format#/properties", propertyKeywords.get(26).getSchema().getSchemaLocation().append(propertyKeywords.get(26).getKeyword()).toString());

        assertEquals("", propertyKeywords.get(27).getInstanceLocation().toString());
        assertEquals("/allOf/5/$ref/properties", propertyKeywords.get(27).getSchema().getEvaluationPath().append(propertyKeywords.get(27).getKeyword()).toString());
        assertEquals("https://json-schema.org/draft/2019-09/meta/content#/properties", propertyKeywords.get(27).getSchema().getSchemaLocation().append(propertyKeywords.get(27).getKeyword()).toString());
    }

    @Test
    void applyDefaults() throws JsonProcessingException {
        String schemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"title\": \"\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"s\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"default\": \"S\"\r\n"
                + "    },\r\n"
                + "    \"ref\": { \"$ref\": \"#/$defs/r\" }\r\n"
                + "  },\r\n"
                + "  \"required\": [ \"s\", \"ref\" ],\r\n"
                + "\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"r\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"default\": \"REF\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

		WalkConfig walkConfig = WalkConfig.builder()
				.applyDefaultsStrategy(new ApplyDefaultsStrategy(true, true, true)).build();
        Schema schema = SchemaRegistry.withDefaultDialect(Version.DRAFT_2020_12).getSchema(schemaData);
        JsonNode inputNode = JsonMapperFactory.getInstance().readTree("{}");
        ValidationResult result = schema.walk(inputNode, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertEquals("{\"s\":\"S\",\"ref\":\"REF\"}", inputNode.toString());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void applyDefaultsWithWalker() throws JsonProcessingException {
        String schemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"title\": \"\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"s\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"default\": \"S\"\r\n"
                + "    },\r\n"
                + "    \"ref\": { \"$ref\": \"#/$defs/r\" }\r\n"
                + "  },\r\n"
                + "  \"required\": [ \"s\", \"ref\" ],\r\n"
                + "\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"r\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"default\": \"REF\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        PropertyWalkListenerRunner propertyWalkListenerRunner = PropertyWalkListenerRunner.builder()
                .propertyWalkListener(new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        if (walkEvent.getInstanceNode() == null || walkEvent.getInstanceNode().isMissingNode()
                                || walkEvent.getInstanceNode().isNull()) {
                            Schema schema = walkEvent.getSchema();
                            SchemaRef schemaRef = SchemaRefs.from(schema);
                            if (schemaRef != null) {
                                schema = schemaRef.getSchema();
                            }
                            JsonNode defaultNode = schema.getSchemaNode().get("default");
                            if (defaultNode != null) {
                                ObjectNode parentNode = (ObjectNode) JsonNodes.get(walkEvent.getRootNode(),
                                        walkEvent.getInstanceLocation().getParent());
                                parentNode.set(walkEvent.getInstanceLocation().getName(-1), defaultNode);
                            }
                        }
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                    }
                })
                .build();

        Schema schema = SchemaRegistry.withDefaultDialect(Version.DRAFT_2020_12).getSchema(schemaData);
        WalkConfig walkConfig = WalkConfig.builder()
                .propertyWalkListenerRunner(propertyWalkListenerRunner)
                .build();
        JsonNode inputNode = JsonMapperFactory.getInstance().readTree("{}");
        ValidationResult result = schema.walk(inputNode, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertEquals("{\"s\":\"S\",\"ref\":\"REF\"}", inputNode.toString());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void applyInvalidDefaultsWithWalker() throws JsonProcessingException {
        String schemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"title\": \"\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"s\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"default\": 1\r\n"
                + "    },\r\n"
                + "    \"ref\": { \"$ref\": \"#/$defs/r\" }\r\n"
                + "  },\r\n"
                + "  \"required\": [ \"s\", \"ref\" ],\r\n"
                + "\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"r\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"default\": \"REF\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        PropertyWalkListenerRunner propertyWalkListenerRunner = PropertyWalkListenerRunner.builder()
                .propertyWalkListener(new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        if (walkEvent.getInstanceNode() == null || walkEvent.getInstanceNode().isMissingNode()
                                || walkEvent.getInstanceNode().isNull()) {
                            Schema schema = walkEvent.getSchema();
                            SchemaRef schemaRef = SchemaRefs.from(schema);
                            if (schemaRef != null) {
                                schema = schemaRef.getSchema();
                            }
                            JsonNode defaultNode = schema.getSchemaNode().get("default");
                            if (defaultNode != null) {
                                ObjectNode parentNode = (ObjectNode) JsonNodes.get(walkEvent.getRootNode(),
                                        walkEvent.getInstanceLocation().getParent());
                                parentNode.set(walkEvent.getInstanceLocation().getName(-1), defaultNode);
                            }
                        }
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                    }
                })
                .build();
        
        Schema schema = SchemaRegistry.withDefaultDialect(Version.DRAFT_2020_12).getSchema(schemaData);
        JsonNode inputNode = JsonMapperFactory.getInstance().readTree("{}");
        WalkConfig walkConfig = WalkConfig.builder()
                .propertyWalkListenerRunner(propertyWalkListenerRunner)
                .build();
        ValidationResult result = schema.walk(inputNode, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertEquals("{\"s\":1,\"ref\":\"REF\"}", inputNode.toString());
        assertFalse(result.getErrors().isEmpty());
        
        inputNode = JsonMapperFactory.getInstance().readTree("{}");
        result = schema.walk(inputNode, false, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertEquals("{\"s\":1,\"ref\":\"REF\"}", inputNode.toString());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    void missingRequired() throws JsonProcessingException {
        String schemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"title\": \"\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"s\": {\r\n"
                + "      \"type\": \"integer\"\r\n"
                + "    },\r\n"
                + "    \"ref\": { \"$ref\": \"#/$defs/r\" }\r\n"
                + "  },\r\n"
                + "  \"required\": [ \"s\", \"ref\" ],\r\n"
                + "\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"r\": {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        Map<String, JsonNode> missingSchemaNode = new LinkedHashMap<>();
        KeywordWalkListenerRunner keywordWalkListenerRunner = KeywordWalkListenerRunner.builder()
                .keywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(), new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        JsonNode requiredNode = walkEvent.getSchema().getSchemaNode().get("required");
                        List<String> requiredProperties = new ArrayList<>();
                        if (requiredNode != null) {
                            if (requiredNode.isArray()) {
                                for (JsonNode fieldName : requiredNode) {
                                    requiredProperties.add(fieldName.asText());
                                }
                            }
                        }
                        for (String requiredProperty : requiredProperties) {
                            JsonNode propertyNode = walkEvent.getInstanceNode().get(requiredProperty);
                            if (propertyNode == null) {
                                // Get the schema
                                PropertiesValidator propertiesValidator = walkEvent.getValidator();
                                Schema propertySchema = propertiesValidator.getSchemas().get(requiredProperty);
                                SchemaRef schemaRef = SchemaRefs.from(propertySchema);
                                if (schemaRef != null) {
                                    propertySchema = schemaRef.getSchema();
                                }
                                missingSchemaNode.put(requiredProperty, propertySchema.getSchemaNode());
                            }
                        }
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                    }
                })
                .build();
        WalkConfig walkConfig = WalkConfig.builder()
                .keywordWalkListenerRunner(keywordWalkListenerRunner)
                .build();
        Schema schema = SchemaRegistry.withDefaultDialect(Version.DRAFT_2020_12).getSchema(schemaData);

        JsonNode inputNode = JsonMapperFactory.getInstance().readTree("{}");
        ValidationResult result = schema.walk(inputNode, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertFalse(result.getErrors().isEmpty());
        assertEquals("{\"type\":\"integer\"}", missingSchemaNode.get("s").toString());
        assertEquals("{\"type\":\"string\"}", missingSchemaNode.get("ref").toString());
    }

    @Test
    void generateDataWithWalker() throws JsonProcessingException {
        Map<String, Supplier<String>> generators = new HashMap<>();
        generators.put("name.findName", () -> "John Doe");
        generators.put("internet.email", () -> "john.doe@gmail.com");

        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"name\": {\r\n"
                + "      \"$ref\": \"#/$defs/Name\"\r\n"
                + "    },\r\n"
                + "    \"email\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"faker\": \"internet.email\"\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"required\": [\r\n"
                + "    \"name\",\r\n"
                + "    \"email\"\r\n"
                + "  ],\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"Name\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"faker\": \"name.findName\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        PropertyWalkListenerRunner propertyWalkListenerRunner = PropertyWalkListenerRunner.builder()
                .propertyWalkListener(new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        if (walkEvent.getInstanceNode() == null || walkEvent.getInstanceNode().isMissingNode()
                                || walkEvent.getInstanceNode().isNull()) {
                            Schema schema = walkEvent.getSchema();
                            SchemaRef schemaRef = null;
                            do {
                                schemaRef = SchemaRefs.from(schema);
                                if (schemaRef != null) {
                                    schema = schemaRef.getSchema();
                                }
                            } while (schemaRef != null);
                            JsonNode fakerNode = schema.getSchemaNode().get("faker");
                            if (fakerNode != null) {
                                String faker = fakerNode.asText();
                                String fakeData = generators.get(faker).get();
                                JsonNode fakeDataNode = JsonNodeFactory.instance.textNode(fakeData);
                                ObjectNode parentNode = (ObjectNode) JsonNodes.get(walkEvent.getRootNode(),
                                        walkEvent.getInstanceLocation().getParent());
                                parentNode.set(walkEvent.getInstanceLocation().getName(-1), fakeDataNode);
                            }
                        }
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                    }
                })
                .build();
        WalkConfig walkConfig = WalkConfig.builder()
                .propertyWalkListenerRunner(propertyWalkListenerRunner)
                .build();
        Schema schema = SchemaRegistry.withDefaultDialect(Version.DRAFT_2020_12).getSchema(schemaData);

        JsonNode inputNode = JsonMapperFactory.getInstance().readTree("{}");
        ValidationResult result = schema.walk(inputNode, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertEquals("{\"name\":\"John Doe\",\"email\":\"john.doe@gmail.com\"}", inputNode.toString());
        assertTrue(result.getErrors().isEmpty());
    }

    /**
     * Issue 989
     */
    @Test
    void itemListenerDraft201909() {
        String schemaData = "        {\r\n"
                + "          \"type\": \"object\",\r\n"
                + "          \"properties\": {\r\n"
                + "            \"name\": {\r\n"
                + "              \"type\": \"string\"\r\n"
                + "            },\r\n"
                + "            \"children\": {\r\n"
                + "              \"type\": \"array\",\r\n"
                + "              \"items\": {\r\n"
                + "                \"type\": \"object\",\r\n"
                + "                \"properties\": {\r\n"
                + "                  \"name\": {\r\n"
                + "                    \"type\": \"string\"\r\n"
                + "                  }\r\n"
                + "                }\r\n"
                + "              }\r\n"
                + "            }\r\n"
                + "          }\r\n"
                + "        }";
        WalkListener listener = new WalkListener() {
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
                        .computeIfAbsent("items", key -> new ArrayList<JsonNodePath>());
                items.add(walkEvent);
            }
        }; 
        ItemWalkListenerRunner itemWalkListenerRunner = ItemWalkListenerRunner.builder().itemWalkListener(listener).build();
        PropertyWalkListenerRunner propertyWalkListenerRunner = PropertyWalkListenerRunner.builder().propertyWalkListener(listener).build();
        WalkConfig walkConfig = WalkConfig.builder()
                .itemWalkListenerRunner(itemWalkListenerRunner)
                .propertyWalkListenerRunner(propertyWalkListenerRunner)
                .build();
        Schema schema = SchemaRegistry.withDefaultDialect(Version.DRAFT_2019_09).getSchema(schemaData);

        ValidationResult result = schema.walk(null, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        @SuppressWarnings("unchecked")
        List<WalkEvent> items = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("items");
        assertEquals(4, items.size());
        assertEquals("/name", items.get(0).getInstanceLocation().toString());
        assertEquals("properties", items.get(0).getKeyword());
        assertEquals("#/properties/name", items.get(0).getSchema().getSchemaLocation().toString());
        assertEquals("/children/0/name", items.get(1).getInstanceLocation().toString());
        assertEquals("properties", items.get(1).getKeyword());
        assertEquals("#/properties/children/items/properties/name", items.get(1).getSchema().getSchemaLocation().toString());
        assertEquals("/children/0", items.get(2).getInstanceLocation().toString());
        assertEquals("items", items.get(2).getKeyword());
        assertEquals("#/properties/children/items", items.get(2).getSchema().getSchemaLocation().toString());
        assertEquals("/children", items.get(3).getInstanceLocation().toString());
        assertEquals("properties", items.get(3).getKeyword());
        assertEquals("#/properties/children", items.get(3).getSchema().getSchemaLocation().toString());
    }

    /**
     * Issue 989
     */
    @Test
    void itemListenerDraft202012() {
        String schemaData = "        {\r\n"
                + "          \"type\": \"object\",\r\n"
                + "          \"properties\": {\r\n"
                + "            \"name\": {\r\n"
                + "              \"type\": \"string\"\r\n"
                + "            },\r\n"
                + "            \"children\": {\r\n"
                + "              \"type\": \"array\",\r\n"
                + "              \"items\": {\r\n"
                + "                \"type\": \"object\",\r\n"
                + "                \"properties\": {\r\n"
                + "                  \"name\": {\r\n"
                + "                    \"type\": \"string\"\r\n"
                + "                  }\r\n"
                + "                }\r\n"
                + "              }\r\n"
                + "            }\r\n"
                + "          }\r\n"
                + "        }";
        WalkListener listener = new WalkListener() {
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
                        .computeIfAbsent("items", key -> new ArrayList<JsonNodePath>());
                items.add(walkEvent);
            }
        };
        ItemWalkListenerRunner itemWalkListenerRunner = ItemWalkListenerRunner.builder().itemWalkListener(listener).build();
        PropertyWalkListenerRunner propertyWalkListenerRunner = PropertyWalkListenerRunner.builder().propertyWalkListener(listener).build();
        WalkConfig walkConfig = WalkConfig.builder()
                .itemWalkListenerRunner(itemWalkListenerRunner)
                .propertyWalkListenerRunner(propertyWalkListenerRunner)
                .build();
        Schema schema = SchemaRegistry.withDefaultDialect(Version.DRAFT_2020_12).getSchema(schemaData);

        ValidationResult result = schema.walk(null, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        List<WalkEvent> items = result.getExecutionContext().getCollectorContext().get("items");
        assertEquals(4, items.size());
        assertEquals("/name", items.get(0).getInstanceLocation().toString());
        assertEquals("properties", items.get(0).getKeyword());
        assertEquals("#/properties/name", items.get(0).getSchema().getSchemaLocation().toString());
        assertEquals("/children/0/name", items.get(1).getInstanceLocation().toString());
        assertEquals("properties", items.get(1).getKeyword());
        assertEquals("#/properties/children/items/properties/name", items.get(1).getSchema().getSchemaLocation().toString());
        assertEquals("/children/0", items.get(2).getInstanceLocation().toString());
        assertEquals("items", items.get(2).getKeyword());
        assertEquals("#/properties/children/items", items.get(2).getSchema().getSchemaLocation().toString());
        assertEquals("/children", items.get(3).getInstanceLocation().toString());
        assertEquals("properties", items.get(3).getKeyword());
        assertEquals("#/properties/children", items.get(3).getSchema().getSchemaLocation().toString());
    }

}
