/*
 * Copyright (c) 2026 the original author or authors.
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

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.networknt.schema.dialect.Dialects;

class Issue1378Test {
    @ParameterizedTest
    @ValueSource(strings = { "oneOf", "anyOf" })
    void nestedDiscriminatorsStayWithinAlternativeBranches(String keyword) throws Exception {
        String doc = new String(Files.readAllBytes(Paths.get(
                getClass().getResource("/schema/issue1378.json").toURI())), StandardCharsets.UTF_8);
        // Change only the outer alternative; keep both nested oneOf schemas intact.
        doc = doc.replaceFirst("oneOf", keyword);
        String document = doc;
        SchemaRegistry registry = SchemaRegistry.withDialect(Dialects.getOpenApi30(),
                builder -> builder.schemas(Collections.singletonMap("https://example.com/openapi.json", document)));
        Schema schema = registry.getSchema(
                SchemaLocation.of("https://example.com/openapi.json#/components/schemas/Container"));
        assertEquals(0, schema.validate("{\"kind\":\"B\",\"items\":[{\"type\":\"x\",\"value\":\"hello\"}]}", InputFormat.JSON).size());
        assertFalse(schema.validate("{\"kind\":\"B\",\"items\":[{\"type\":\"x\",\"value\":42}]}", InputFormat.JSON).isEmpty());
        assertEquals(0, schema.validate("{\"kind\":\"B\",\"items\":[{\"type\":\"y\",\"count\":1}]}", InputFormat.JSON).size());
        assertEquals(0, schema.validate("{\"kind\":\"A\",\"items\":[{\"type\":\"x\",\"values\":[\"a\"]}]}", InputFormat.JSON).size());
    }
}
