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

import org.junit.jupiter.api.Test;

import com.networknt.schema.keyword.KeywordType;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.walk.WalkListener;
import com.networknt.schema.walk.KeywordWalkListenerRunner;
import com.networknt.schema.walk.WalkConfig;
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
        KeywordWalkListenerRunner keywordWalkListenerRunner = KeywordWalkListenerRunner.builder()
                .keywordWalkListener(KeywordType.TYPE.getValue(), new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                        @SuppressWarnings("unchecked")
                        List<WalkEvent> types = (List<WalkEvent>) walkEvent.getExecutionContext()
                                .getCollectorContext()
                                .getData()
                                .computeIfAbsent("types", key -> new ArrayList<NodePath>());
                        types.add(walkEvent);
                    }
                })
                .build();
        WalkConfig walkConfig = WalkConfig.builder()
                .keywordWalkListenerRunner(keywordWalkListenerRunner)
                .build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        Result result = schema.walk("\"false\"", InputFormat.JSON, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertFalse(result.getErrors().isEmpty());

        List<WalkEvent> types = result.getExecutionContext().getCollectorContext().get("types");
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
        KeywordWalkListenerRunner keywordWalkListenerRunner = KeywordWalkListenerRunner.builder()
                .keywordWalkListener(KeywordType.TYPE.getValue(), new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                        @SuppressWarnings("unchecked")
                        List<WalkEvent> types = (List<WalkEvent>) walkEvent.getExecutionContext()
                                .getCollectorContext()
                                .getData()
                                .computeIfAbsent("types", key -> new ArrayList<NodePath>());
                        types.add(walkEvent);
                    }
                })
                .build();
        WalkConfig walkConfig = WalkConfig.builder()
                .keywordWalkListenerRunner(keywordWalkListenerRunner)
                .build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        Result result = schema.walk("\"hello\"", InputFormat.JSON, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertFalse(result.getErrors().isEmpty());

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
        KeywordWalkListenerRunner keywordWalkListenerRunner = KeywordWalkListenerRunner.builder()
                .keywordWalkListener(KeywordType.TYPE.getValue(), new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                        @SuppressWarnings("unchecked")
                        List<WalkEvent> types = (List<WalkEvent>) walkEvent.getExecutionContext()
                                .getCollectorContext()
                                .getData()
                                .computeIfAbsent("types", key -> new ArrayList<NodePath>());
                        types.add(walkEvent);
                    }
                })
                .build();
        WalkConfig walkConfig = WalkConfig.builder()
                .keywordWalkListenerRunner(keywordWalkListenerRunner)
                .build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        Result result = schema.walk(null, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertTrue(result.getErrors().isEmpty());

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
        KeywordWalkListenerRunner keywordWalkListenerRunner = KeywordWalkListenerRunner.builder()
                .keywordWalkListener(KeywordType.TYPE.getValue(), new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> errors) {
                        @SuppressWarnings("unchecked")
                        List<WalkEvent> types = (List<WalkEvent>) walkEvent.getExecutionContext()
                                .getCollectorContext()
                                .getData()
                                .computeIfAbsent("types", key -> new ArrayList<NodePath>());
                        types.add(walkEvent);
                    }
                })
                .build();
        WalkConfig walkConfig = WalkConfig.builder()
                .keywordWalkListenerRunner(keywordWalkListenerRunner)
                .build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        Result result = schema.walk("\"false\"", InputFormat.JSON, false, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertTrue(result.getErrors().isEmpty());

        @SuppressWarnings("unchecked")
        List<WalkEvent> types = (List<WalkEvent>) result.getExecutionContext().getCollectorContext().get("types");
        assertEquals(2, types.size());
    }
}
