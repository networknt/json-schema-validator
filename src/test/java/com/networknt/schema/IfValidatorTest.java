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
 * Test for IfValidator.
 */
class IfValidatorTest {

    @Test
    void walkValidateThen() {
        String schemaData = "{\r\n"
                + "  \"if\": {\r\n"
                + "    \"const\": \"false\"\r\n"
                + "  },\r\n"
                + "  \"then\": {\r\n"
                + "    \"type\": \"object\"\r\n"
                + "  },\r\n"
                + "  \"else\": {\r\n"
                + "    \"type\": \"number\"\r\n"
                + "  }\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                .keywordWalkListener(ValidatorTypeCode.TYPE.getValue(), new JsonSchemaWalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages) {
                        @SuppressWarnings("unchecked")
                        List<WalkEvent> types = (List<WalkEvent>) walkEvent.getExecutionContext()
                                .getCollectorContext()
                                .getCollectorMap()
                                .computeIfAbsent("types", key -> new ArrayList<JsonNodePath>());
                        types.add(walkEvent);
                    }
                })
                .build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        ValidationResult result = schema.walk("\"false\"", InputFormat.JSON, true);
        assertFalse(result.getValidationMessages().isEmpty());

        @SuppressWarnings("unchecked")
        List<WalkEvent> types = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("types");
        assertEquals(1, types.size());
        assertEquals("", types.get(0).getInstanceLocation().toString());
        assertEquals("/then", types.get(0).getSchema().getEvaluationPath().toString());
    }

    @Test
    void walkValidateElse() {
        String schemaData = "{\r\n"
                + "  \"if\": {\r\n"
                + "    \"const\": \"false\"\r\n"
                + "  },\r\n"
                + "  \"then\": {\r\n"
                + "    \"type\": \"object\"\r\n"
                + "  },\r\n"
                + "  \"else\": {\r\n"
                + "    \"type\": \"number\"\r\n"
                + "  }\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                .keywordWalkListener(ValidatorTypeCode.TYPE.getValue(), new JsonSchemaWalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages) {
                        @SuppressWarnings("unchecked")
                        List<WalkEvent> types = (List<WalkEvent>) walkEvent.getExecutionContext()
                                .getCollectorContext()
                                .getCollectorMap()
                                .computeIfAbsent("types", key -> new ArrayList<JsonNodePath>());
                        types.add(walkEvent);
                    }
                })
                .build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        ValidationResult result = schema.walk("\"hello\"", InputFormat.JSON, true);
        assertFalse(result.getValidationMessages().isEmpty());

        @SuppressWarnings("unchecked")
        List<WalkEvent> types = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("types");
        assertEquals(1, types.size());
        assertEquals("", types.get(0).getInstanceLocation().toString());
        assertEquals("/else", types.get(0).getSchema().getEvaluationPath().toString());
    }

    @Test
    void walkValidateNull() {
        String schemaData = "{\r\n"
                + "  \"if\": {\r\n"
                + "    \"const\": \"false\"\r\n"
                + "  },\r\n"
                + "  \"then\": {\r\n"
                + "    \"type\": \"object\"\r\n"
                + "  },\r\n"
                + "  \"else\": {\r\n"
                + "    \"type\": \"number\"\r\n"
                + "  }\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                .keywordWalkListener(ValidatorTypeCode.TYPE.getValue(), new JsonSchemaWalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages) {
                        @SuppressWarnings("unchecked")
                        List<WalkEvent> types = (List<WalkEvent>) walkEvent.getExecutionContext()
                                .getCollectorContext()
                                .getCollectorMap()
                                .computeIfAbsent("types", key -> new ArrayList<JsonNodePath>());
                        types.add(walkEvent);
                    }
                })
                .build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        ValidationResult result = schema.walk(null, true);
        assertTrue(result.getValidationMessages().isEmpty());

        @SuppressWarnings("unchecked")
        List<WalkEvent> types = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("types");
        assertEquals(2, types.size());
    }

    @Test
    void walkNoValidate() {
        String schemaData = "{\r\n"
                + "  \"if\": {\r\n"
                + "    \"const\": \"false\"\r\n"
                + "  },\r\n"
                + "  \"then\": {\r\n"
                + "    \"type\": \"object\"\r\n"
                + "  },\r\n"
                + "  \"else\": {\r\n"
                + "    \"type\": \"number\"\r\n"
                + "  }\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                .keywordWalkListener(ValidatorTypeCode.TYPE.getValue(), new JsonSchemaWalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> validationMessages) {
                        @SuppressWarnings("unchecked")
                        List<WalkEvent> types = (List<WalkEvent>) walkEvent.getExecutionContext()
                                .getCollectorContext()
                                .getCollectorMap()
                                .computeIfAbsent("types", key -> new ArrayList<JsonNodePath>());
                        types.add(walkEvent);
                    }
                })
                .build();
        JsonSchema schema = factory.getSchema(schemaData, config);
        ValidationResult result = schema.walk("\"false\"", InputFormat.JSON, false);
        assertTrue(result.getValidationMessages().isEmpty());

        @SuppressWarnings("unchecked")
        List<WalkEvent> types = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("types");
        assertEquals(2, types.size());
    }
}
