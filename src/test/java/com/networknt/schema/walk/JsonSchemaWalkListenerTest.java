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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ApplyDefaultsStrategy;
import com.networknt.schema.InputFormat;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.JsonSchemaRef;
import com.networknt.schema.PathType;
import com.networknt.schema.PropertiesValidator;
import com.networknt.schema.SchemaId;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.serialization.JsonMapperFactory;
import com.networknt.schema.utils.JsonSchemaRefs;
import com.networknt.schema.ValidationMessage;
import com.networknt.schema.ValidationResult;
import com.networknt.schema.ValidatorTypeCode;

/**
 * JsonSchemaWalkListenerTest.
 */
class JsonSchemaWalkListenerTest {

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
        
        List<WalkEvent> propertyKeywords = new ArrayList<>();
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
        config.addKeywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(), new JsonSchemaWalkListener() {
            
            @Override
            public WalkFlow onWalkStart(WalkEvent walkEvent) {
                propertyKeywords.add(walkEvent);
                return WalkFlow.CONTINUE;
            }
            
            @Override
            public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages) {
            }
        });
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V7).getSchema(schemaData, config);
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
        ValidationResult result = schema.walk(inputData, InputFormat.JSON, true);
        assertTrue(result.getValidationMessages().isEmpty());
        assertEquals(3, propertyKeywords.size());
        assertEquals("", propertyKeywords.get(0).getInstanceLocation().toString());
        assertEquals("/properties", propertyKeywords.get(0).getSchema().getEvaluationPath()
                .append(propertyKeywords.get(0).getKeyword()).toString());
        assertEquals("/tags/0", propertyKeywords.get(1).getInstanceLocation().toString());
        assertEquals("image", propertyKeywords.get(1).getInstanceNode().get("name").asText());
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
        
        List<WalkEvent> properties = new ArrayList<>();
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
        config.addPropertyWalkListener(new JsonSchemaWalkListener() {
            
            @Override
            public WalkFlow onWalkStart(WalkEvent walkEvent) {
                properties.add(walkEvent);
                return WalkFlow.CONTINUE;
            }

            @Override
            public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages) {
            }
        });
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V7).getSchema(schemaData, config);
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
        ValidationResult result = schema.walk(inputData, InputFormat.JSON, true);
        assertTrue(result.getValidationMessages().isEmpty());
        assertEquals(5, properties.size());
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
    void draft201909() {
        List<WalkEvent> propertyKeywords = new ArrayList<>();
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setPathType(PathType.JSON_POINTER);
        config.addKeywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(), new JsonSchemaWalkListener() {
            
            @Override
            public WalkFlow onWalkStart(WalkEvent walkEvent) {
                propertyKeywords.add(walkEvent);
                return WalkFlow.CONTINUE;
            }
            
            @Override
            public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages) {
            }
        });
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V201909)
                .getSchema(SchemaLocation.of(SchemaId.V201909), config);
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
        ValidationResult result = schema.walk(inputData, InputFormat.JSON, true);
        assertTrue(result.getValidationMessages().isEmpty());
        
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
    void applyDefaults() throws JsonMappingException, JsonProcessingException {
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
        
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setApplyDefaultsStrategy(new ApplyDefaultsStrategy(true, true, true));
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData, config);
        JsonNode inputNode = JsonMapperFactory.getInstance().readTree("{}");
        ValidationResult result = schema.walk(inputNode, true);
        assertEquals("{\"s\":\"S\",\"ref\":\"REF\"}", inputNode.toString());
        assertTrue(result.getValidationMessages().isEmpty());
    }

    @Test
    void missingRequired() throws JsonMappingException, JsonProcessingException {
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
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.addKeywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(), new JsonSchemaWalkListener() {
            
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
                        PropertiesValidator propertiesValidator = (PropertiesValidator) walkEvent.getSchema().getValidators().stream()
                                .filter(v -> walkEvent.getKeyword().equals(v.getKeyword())).findFirst().get();
                        JsonSchema propertySchema = propertiesValidator.getSchemas().get(requiredProperty);
                        JsonSchemaRef schemaRef = JsonSchemaRefs.from(propertySchema);
                        if (schemaRef != null) {
                            propertySchema = schemaRef.getSchema();
                        }
                        missingSchemaNode.put(requiredProperty, propertySchema.getSchemaNode());
                    }
                }
                return WalkFlow.CONTINUE;
            }
            
            @Override
            public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages) {
            }
        });

        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData, config);
        JsonNode inputNode = JsonMapperFactory.getInstance().readTree("{}");
        ValidationResult result = schema.walk(inputNode, true);
        assertFalse(result.getValidationMessages().isEmpty());
        assertEquals("{\"type\":\"integer\"}", missingSchemaNode.get("s").toString());
        assertEquals("{\"type\":\"string\"}", missingSchemaNode.get("ref").toString());
    }
}
