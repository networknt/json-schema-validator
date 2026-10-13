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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.AnnotationKeyword;

class Issue1205Test {
    private static final List<String> DOCUMENT_FIELDS = Arrays.asList(
            "openapi", "info", "security", "servers", "paths", "components");

    private static Dialect recording(Dialect dialect, List<String> keywords) {
        return Dialect.builder(dialect).unknownKeywordFactory((keyword, context) -> {
            keywords.add(keyword);
            return new AnnotationKeyword(keyword);
        }).build();
    }

    private static SchemaRegistry openApi(String version, boolean preload, List<String> keywords) {
        Dialect dialect = "3.0".equals(version) ? Dialects.getOpenApi30() : Dialects.getOpenApi31();
        return SchemaRegistry.withDialect(recording(dialect, keywords), builder -> builder
                .schemaRegistryConfig(SchemaRegistryConfig.builder().preloadSchema(preload).build()));
    }

    private static String document(String version) {
        return "classpath:schema/issue1205/openapi-" + version + ".yaml";
    }

    @ParameterizedTest
    @CsvSource({ "3.0,true", "3.0,false", "3.1,true", "3.1,false" })
    void fragmentOnlyReportsUnknownKeywordsInSchemas(String version, boolean preload) {
        List<String> keywords = new ArrayList<>();
        SchemaRegistry registry = openApi(version, preload, keywords);
        Schema schema = registry.getSchema(SchemaLocation.of(document(version) + "#/components/schemas/Item"));
        assertTrue(schema.validate("{\"Foo\":\"123\",\"Bar\":\"DIY\",\"Baz\":7}", InputFormat.JSON).isEmpty());
        assertKeyword(schema, "{\"Foo\":\"abc\",\"Bar\":\"DIY\",\"Baz\":7}", "pattern");
        assertKeyword(schema, "{\"Bar\":\"DIY\",\"Baz\":7}", "required");
        assertKeyword(schema, "{\"Foo\":\"123\",\"Bar\":7,\"Baz\":7}", "type");
        assertKeyword(schema, "{\"Foo\":\"123\",\"Bar\":\"DIY\",\"Baz\":\"bad\"}", "type");
        assertEquals(new HashSet<>(Arrays.asList("typoKeyword", "nestedTypoKeyword", "externalTypoKeyword")),
                new HashSet<>(keywords));
        assertTrue(Collections.disjoint(DOCUMENT_FIELDS, keywords));
        assertSame(schema, registry.getSchema(SchemaLocation.of(document(version) + "#/components/schemas/Item")));

        // A real schema using a document field name must still reach the factory.
        registry.getSchema("{\"type\":\"string\",\"info\":\"unknown schema keyword\"}");
        assertTrue(keywords.contains("info"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "3.0", "3.1" })
    void directDocumentLoadStillReportsUnknownKeywords(String version) {
        List<String> keywords = new ArrayList<>();
        SchemaRegistry registry = openApi(version, true, keywords);
        registry.getSchema(SchemaLocation.of(document(version)));
        assertEquals(new HashSet<>(DOCUMENT_FIELDS), new HashSet<>(keywords));

        // A cached direct document load must not change fragment diagnostics.
        keywords.clear();
        Schema fragment = registry.getSchema(SchemaLocation.of(document(version) + "#/components/schemas/Item"));
        fragment.initializeValidators();
        assertTrue(keywords.contains("typoKeyword"));
        assertTrue(Collections.disjoint(DOCUMENT_FIELDS, keywords));
    }

    @ParameterizedTest
    @ValueSource(strings = { "3.0", "3.1" })
    void fragmentLoadDoesNotSilenceLaterDirectDocumentLoad(String version) {
        List<String> keywords = new ArrayList<>();
        SchemaRegistry registry = openApi(version, true, keywords);
        registry.getSchema(SchemaLocation.of(document(version) + "#/components/schemas/Item"));
        keywords.clear();
        registry.getSchema(SchemaLocation.of(document(version)));
        assertEquals(new HashSet<>(DOCUMENT_FIELDS), new HashSet<>(keywords));
    }

    @Test
    void anchorAndIdRegistrationStillProcessKnownDocumentKeywords() {
        List<String> keywords = new ArrayList<>();
        String uri = "https://example.com/document.json";
        String data = "{\"$id\":\"https://example.com/base/root.json\",\"containerField\":true,"
                + "\"$defs\":{\"value\":{\"$anchor\":\"value\",\"type\":\"integer\",\"typoKeyword\":true},"
                + "\"child\":{\"$id\":\"child.json\",\"type\":\"string\"},"
                + "\"item\":{\"type\":\"object\",\"properties\":{"
                + "\"a\":{\"$ref\":\"#value\"},\"b\":{\"$ref\":\"child.json\"}}}}}";
        SchemaRegistry registry = SchemaRegistry.withDialect(recording(Dialects.getDraft202012(), keywords),
                builder -> builder.resourceLoaders(loaders -> loaders.resources(Collections.singletonMap(uri, data))));
        Schema item = registry.getSchema(SchemaLocation.of(uri + "#/$defs/item"));
        assertTrue(item.validate("{\"a\":1,\"b\":\"ok\"}", InputFormat.JSON).isEmpty());
        assertKeyword(item, "{\"a\":\"bad\",\"b\":\"ok\"}", "type");
        assertKeyword(item, "{\"a\":1,\"b\":2}", "type");
        assertEquals(Collections.singleton("typoKeyword"), new HashSet<>(keywords));
        Schema anchor = registry.getSchema(SchemaLocation.of(uri + "#value"));
        assertTrue(anchor.validate("1", InputFormat.JSON).isEmpty());
        assertKeyword(anchor, "\"bad\"", "type");
        assertEquals(new HashSet<>(Arrays.asList("containerField", "typoKeyword")), new HashSet<>(keywords));
    }

    @Test
    void anchorSelectingDocumentRootStillReportsUnknownKeywords() {
        List<String> keywords = new ArrayList<>();
        String uri = "https://example.com/root.json";
        SchemaRegistry registry = SchemaRegistry.withDialect(recording(Dialects.getDraft202012(), keywords),
                builder -> builder.resourceLoaders(loaders -> loaders.resources(Collections.singletonMap(uri,
                        "{\"$anchor\":\"root\",\"type\":\"integer\",\"typoKeyword\":true}"))));
        Schema schema = registry.getSchema(SchemaLocation.of(uri + "#root"));
        assertTrue(schema.validate("1", InputFormat.JSON).isEmpty());
        assertKeyword(schema, "\"bad\"", "type");
        assertEquals(Collections.singletonList("typoKeyword"), keywords);
    }

    @Test
    void nestedIdAndDialectChangesRetainTheirContext() {
        List<String> draft4Keywords = new ArrayList<>();
        List<String> draft202012Keywords = new ArrayList<>();
        String uri = "https://example.com/document.json";
        Map<String, String> resources = new HashMap<>();
        resources.put(uri, "{\"containerField\":true,\"$defs\":{\"scope\":{"
                + "\"$id\":\"https://example.com/scoped/root.json\",\"$defs\":{\"item\":{"
                + "\"$schema\":\"http://json-schema.org/draft-04/schema#\",\"typoKeyword\":true,"
                + "\"type\":\"object\",\"properties\":{\"amount\":{\"type\":\"number\","
                + "\"minimum\":5,\"exclusiveMinimum\":true},\"code\":{\"$ref\":\"code.json\"}}}}}}}");
        resources.put("https://example.com/scoped/code.json", "{\"type\":\"string\"}");
        SchemaRegistry registry = SchemaRegistry.withDialects(Arrays.asList(
                recording(Dialects.getDraft202012(), draft202012Keywords),
                recording(Dialects.getDraft4(), draft4Keywords)),
                builder -> builder.resourceLoaders(loaders -> loaders.resources(resources)));
        Schema schema = registry.getSchema(SchemaLocation.of(uri + "#/$defs/scope/$defs/item"));
        assertTrue(schema.validate("{\"amount\":6,\"code\":\"ok\"}", InputFormat.JSON).isEmpty());
        assertFalse(schema.validate("{\"amount\":5,\"code\":\"ok\"}", InputFormat.JSON).isEmpty());
        assertKeyword(schema, "{\"amount\":6,\"code\":1}", "type");
        assertTrue(draft4Keywords.contains("typoKeyword"));
        assertTrue(draft202012Keywords.isEmpty());
    }

    private static void assertKeyword(Schema schema, String input, String keyword) {
        List<Error> errors = schema.validate(input, InputFormat.JSON);
        assertEquals(1, errors.size(), errors.toString());
        assertEquals(keyword, errors.get(0).getKeyword());
    }
}
