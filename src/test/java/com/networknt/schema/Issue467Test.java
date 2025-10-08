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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.keyword.KeywordType;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.walk.WalkListener;
import com.networknt.schema.walk.KeywordWalkListenerRunner;
import com.networknt.schema.walk.PropertyWalkListenerRunner;
import com.networknt.schema.walk.WalkConfig;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;

class Issue467Test {
    private static final String schemaPath = "/schema/issue467.json";

    protected ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldWalkKeywordWithValidation() throws URISyntaxException, IOException {
        InputStream schemaInputStream = Issue467Test.class.getResourceAsStream(schemaPath);
        final Set<NodePath> properties = new LinkedHashSet<>();
        KeywordWalkListenerRunner keywordWalkListenerRunner = KeywordWalkListenerRunner.builder()
                .keywordWalkListener(KeywordType.PROPERTIES.getValue(), new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        properties.add(walkEvent.getSchema().getEvaluationPath().append(walkEvent.getKeyword()));
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> set) {
                    }
                })
                .build();
        WalkConfig walkConfig = WalkConfig.builder()
                .keywordWalkListenerRunner(keywordWalkListenerRunner)
                .build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
        Schema schema = factory.getSchema(schemaInputStream);
        JsonNode data = mapper.readTree(Issue467Test.class.getResource("/data/issue467.json"));
        Result result = schema.walk(data, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertEquals(new HashSet<>(Arrays.asList("/properties", "/properties/tags/items/0/properties")),
                properties.stream().map(Object::toString).collect(Collectors.toSet()));
        assertEquals(1, result.getErrors().size());
    }

    @Test
    void shouldWalkPropertiesWithValidation() throws URISyntaxException, IOException {
        InputStream schemaInputStream = Issue467Test.class.getResourceAsStream(schemaPath);
        final Set<NodePath> properties = new LinkedHashSet<>();
        PropertyWalkListenerRunner propertyWalkListenerRunner = PropertyWalkListenerRunner.builder()
                .propertyWalkListener(new WalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        properties.add(walkEvent.getSchema().getEvaluationPath());
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, List<Error> set) {
                    }
                })
                .build();
        WalkConfig walkConfig = WalkConfig.builder()
                .propertyWalkListenerRunner(propertyWalkListenerRunner)
                .build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
        Schema schema = factory.getSchema(schemaInputStream);
        JsonNode data = mapper.readTree(Issue467Test.class.getResource("/data/issue467.json"));
        Result result = schema.walk(data, true, executionContext -> executionContext.setWalkConfig(walkConfig));
        assertEquals(
                new HashSet<>(Arrays.asList("/properties/tags", "/properties/tags/items/0/properties/category", "/properties/tags/items/0/properties/value")),
                properties.stream().map(Object::toString).collect(Collectors.toSet()));
        assertEquals(1, result.getErrors().size());
    }

}
