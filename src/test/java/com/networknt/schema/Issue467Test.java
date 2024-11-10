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
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.walk.JsonSchemaWalkListener;
import com.networknt.schema.walk.WalkEvent;
import com.networknt.schema.walk.WalkFlow;

class Issue467Test {
    private static final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
    private static final String schemaPath = "/schema/issue467.json";

    protected ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldWalkKeywordWithValidation() throws URISyntaxException, IOException {
        InputStream schemaInputStream = Issue467Test.class.getResourceAsStream(schemaPath);
        final Set<JsonNodePath> properties = new LinkedHashSet<>();
        final SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                .keywordWalkListener(ValidatorTypeCode.PROPERTIES.getValue(), new JsonSchemaWalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        properties.add(walkEvent.getSchema().getEvaluationPath().append(walkEvent.getKeyword()));
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> set) {
                    }
                })
                .build();
        JsonSchema schema = factory.getSchema(schemaInputStream, config);
        JsonNode data = mapper.readTree(Issue467Test.class.getResource("/data/issue467.json"));
        ValidationResult result = schema.walk(data, true);
        assertEquals(new HashSet<>(Arrays.asList("/properties", "/properties/tags/items/0/properties")),
                properties.stream().map(Object::toString).collect(Collectors.toSet()));
        assertEquals(1, result.getValidationMessages().size());
    }

    @Test
    void shouldWalkPropertiesWithValidation() throws URISyntaxException, IOException {
        InputStream schemaInputStream = Issue467Test.class.getResourceAsStream(schemaPath);
        final Set<JsonNodePath> properties = new LinkedHashSet<>();
        final SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                .propertyWalkListener(new JsonSchemaWalkListener() {
                    @Override
                    public WalkFlow onWalkStart(WalkEvent walkEvent) {
                        properties.add(walkEvent.getSchema().getEvaluationPath());
                        return WalkFlow.CONTINUE;
                    }

                    @Override
                    public void onWalkEnd(WalkEvent walkEvent, Set<ValidationMessage> set) {
                    }
                })
                .build();
        JsonSchema schema = factory.getSchema(schemaInputStream, config);
        JsonNode data = mapper.readTree(Issue467Test.class.getResource("/data/issue467.json"));
        ValidationResult result = schema.walk(data, true);
        assertEquals(
                new HashSet<>(Arrays.asList("/properties/tags", "/properties/tags/items/0/properties/category", "/properties/tags/items/0/properties/value")),
                properties.stream().map(Object::toString).collect(Collectors.toSet()));
        assertEquals(1, result.getValidationMessages().size());
    }

}
