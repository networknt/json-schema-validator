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

import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.AnnotationKeyword;
import com.networknt.schema.keyword.DisallowUnknownKeywordFactory;
import com.networknt.schema.keyword.DiscriminatorValidator;
import tools.jackson.databind.JsonNode;

class Issue1325Test {
    private static final List<String> DOCUMENT_FIELDS = Arrays.asList(
            "openapi", "info", "security", "servers", "paths", "components");

    private static Dialect dialect(String version) {
        return "3.0".equals(version) ? Dialects.getOpenApi30() : Dialects.getOpenApi31();
    }

    private static SchemaRegistry registry(String version, boolean preload, boolean cache, List<String> keywords) {
        Dialect dialect = Dialect.builder(dialect(version)).unknownKeywordFactory((keyword, context) -> {
            keywords.add(keyword);
            return new AnnotationKeyword(keyword);
        }).build();
        return SchemaRegistry.withDialect(dialect, builder -> builder.schemaCacheEnabled(cache)
                .schemaRegistryConfig(SchemaRegistryConfig.builder().preloadSchema(preload).cacheRefs(cache).build()));
    }

    private static String document(String version) {
        return "classpath:schema/issue1205/openapi-" + version + ".yaml";
    }

    private static Schema reference(SchemaRegistry registry, String uri) {
        return registry.getSchema("{\"$ref\":\"" + uri + "\"}");
    }

    @ParameterizedTest
    @CsvSource({ "3.0,true,true", "3.0,true,false", "3.0,false,true", "3.0,false,false",
            "3.1,true,true", "3.1,true,false", "3.1,false,true", "3.1,false,false" })
    void externalPointerReferenceOnlyReportsSchemaKeywords(String version, boolean preload, boolean cache) {
        List<String> keywords = new ArrayList<>();
        SchemaRegistry registry = registry(version, preload, cache, keywords);
        Schema schema = reference(registry, document(version) + "#/components/schemas/Item");
        assertTrue(schema.validate("{\"Foo\":\"123\",\"Bar\":\"ok\",\"Baz\":7}", InputFormat.JSON).isEmpty());
        assertKeyword(schema, "{\"Foo\":\"bad\",\"Bar\":\"ok\",\"Baz\":7}", "pattern");
        assertKeyword(schema, "{\"Bar\":\"ok\",\"Baz\":7}", "required");
        assertKeyword(schema, "{\"Foo\":\"123\",\"Bar\":1,\"Baz\":7}", "type");
        assertKeyword(schema, "{\"Foo\":\"123\",\"Bar\":\"ok\",\"Baz\":\"bad\"}", "type");
        assertEquals(new HashSet<>(Arrays.asList("typoKeyword", "nestedTypoKeyword", "externalTypoKeyword")),
                new HashSet<>(keywords));
    }

    @ParameterizedTest
    @CsvSource({ "3.0,true", "3.0,false", "3.1,true", "3.1,false" })
    void directAndContainerCachesKeepTheirOwnDiagnostics(String version, boolean directFirst) {
        List<String> keywords = new ArrayList<>();
        SchemaRegistry registry = registry(version, true, true, keywords);
        SchemaLocation location = SchemaLocation.of(document(version));
        if (directFirst) {
            registry.getSchema(location);
            assertEquals(new HashSet<>(DOCUMENT_FIELDS), new HashSet<>(keywords));
            keywords.clear();
        }
        reference(registry, document(version) + "#/components/schemas/Item").initializeValidators();
        assertTrue(Collections.disjoint(DOCUMENT_FIELDS, keywords));
        assertTrue(keywords.contains("typoKeyword"));
        keywords.clear();
        Schema direct = registry.getSchema(location);
        assertSame(direct, registry.getSchema(location));
        if (!directFirst) {
            assertEquals(new HashSet<>(DOCUMENT_FIELDS), new HashSet<>(keywords));
        }
        Schema container = registry.loadSchema(location, false);
        assertSame(container, registry.loadSchema(location, false));
        assertNotSame(direct, container);
    }

    @ParameterizedTest
    @CsvSource({ "3.0,true", "3.0,false", "3.1,true", "3.1,false" })
    void strictFactoryAcceptsContainerButRejectsDirectLoadsAndSchemaTypos(String version, boolean directFirst) {
        String uri = "https://example.com/openapi.json";
        String data = "{\"openapi\":\"" + version + ".0\",\"components\":{\"schemas\":{"
                + "\"Item\":{\"type\":\"string\"},\"Bad\":{\"type\":\"string\",\"typoKeyword\":true}}}}";
        Dialect strict = Dialect.builder(dialect(version))
                .unknownKeywordFactory(DisallowUnknownKeywordFactory.getInstance()).build();
        SchemaRegistry registry = SchemaRegistry.withDialect(strict, builder -> builder
                .resourceLoaders(loaders -> loaders.resources(Collections.singletonMap(uri, data))));
        if (directFirst) {
            assertThrows(InvalidSchemaException.class, () -> registry.getSchema(SchemaLocation.of(uri)));
        }
        Schema schema = reference(registry, uri + "#/components/schemas/Item");
        schema.initializeValidators();
        assertTrue(schema.validate("\"ok\"", InputFormat.JSON).isEmpty());
        assertKeyword(schema, "1", "type");
        assertThrows(InvalidSchemaException.class, () -> registry.getSchema(SchemaLocation.of(uri)));
        assertThrows(InvalidSchemaException.class,
                () -> reference(registry, uri + "#/components/schemas/Bad").initializeValidators());
    }

    @Test
    void anchorAndWholeDocumentReferencesKeepRootDiagnostics() {
        for (String fragment : Arrays.asList("", "#", "#root")) {
            List<String> keywords = new ArrayList<>();
            String uri = "https://example.com/root.json";
            Dialect dialect = Dialect.builder(Dialects.getDraft202012()).unknownKeywordFactory((keyword, context) -> {
                keywords.add(keyword);
                return new AnnotationKeyword(keyword);
            }).build();
            SchemaRegistry registry = SchemaRegistry.withDialect(dialect, builder -> builder.resourceLoaders(loaders ->
                    loaders.resources(Collections.singletonMap(uri,
                            "{\"$anchor\":\"root\",\"type\":\"integer\",\"typoKeyword\":true}"))));
            Schema schema = reference(registry, uri + fragment);
            assertTrue(schema.validate("1", InputFormat.JSON).isEmpty());
            assertKeyword(schema, "\"bad\"", "type");
            assertEquals(Collections.singletonList("typoKeyword"), keywords);
        }
    }

    @Test
    void externalPointerRetainsIdsAnchorsAndNestedDialect() {
        String uri = "https://example.com/document.json";
        List<String> keywords = new ArrayList<>();
        Map<String, String> resources = new HashMap<>();
        resources.put(uri, "{\"$id\":\"https://example.com/base/root.json\",\"containerField\":true,"
                + "\"$defs\":{\"number\":{\"$anchor\":\"number\",\"type\":\"integer\"},"
                + "\"scope\":{\"$id\":\"nested/root.json\",\"$defs\":{\"legacy\":{"
                + "\"$schema\":\"http://json-schema.org/draft-04/schema#\",\"typoKeyword\":true,"
                + "\"type\":\"number\",\"minimum\":5,\"exclusiveMinimum\":true},"
                + "\"item\":{\"type\":\"object\",\"properties\":{"
                + "\"amount\":{\"$ref\":\"#/$defs/legacy\"},"
                + "\"code\":{\"$ref\":\"code.json\"}}}}},"
                + "\"item\":{\"type\":\"object\",\"properties\":{"
                + "\"count\":{\"$ref\":\"#number\"},\"child\":{\"$ref\":\"nested/root.json#/$defs/item\"}}}}}");
        resources.put("https://example.com/base/nested/code.json", "{\"type\":\"string\"}");
        List<Dialect> dialects = new ArrayList<>();
        for (Dialect dialect : Arrays.asList(Dialects.getDraft202012(), Dialects.getDraft4())) {
            dialects.add(Dialect.builder(dialect).unknownKeywordFactory((keyword, context) -> {
                keywords.add(keyword);
                return new AnnotationKeyword(keyword);
            }).build());
        }
        SchemaRegistry registry = SchemaRegistry.withDialects(dialects,
                builder -> builder.resourceLoaders(loaders -> loaders.resources(resources)));
        Schema schema = reference(registry, uri + "#/$defs/item");
        assertTrue(schema.validate("{\"count\":1,\"child\":{\"amount\":6,\"code\":\"ok\"}}", InputFormat.JSON).isEmpty());
        assertKeyword(schema, "{\"count\":\"bad\"}", "type");
        assertFalse(schema.validate("{\"child\":{\"amount\":5}}", InputFormat.JSON).isEmpty());
        assertKeyword(schema, "{\"child\":{\"code\":1}}", "type");
        assertEquals(Collections.singleton("typoKeyword"), new HashSet<>(keywords));
    }

    @Test
    void containerFallbackRunsAfterNullableAndDiscriminatorSpecialCases() throws java.io.IOException {
        SchemaRegistry registry = SchemaRegistry.withDialect(Dialects.getDraft202012());
        SchemaContext context = new SchemaContext(Dialects.getDraft202012(), registry) {
            @Override
            public boolean isNullableKeywordEnabled() {
                return true;
            }

            @Override
            public boolean isDiscriminatorKeywordEnabled() {
                return true;
            }
        };
        JsonNode node = registry.readTree("{\"nullable\":true,\"discriminator\":{\"propertyName\":\"kind\"}}", InputFormat.JSON);
        context.setDocumentContainer(node);
        Schema schema = registry.create(context, SchemaLocation.DOCUMENT, node, null);
        assertEquals(1, schema.getValidators().size());
        assertInstanceOf(DiscriminatorValidator.class, schema.getValidators().get(0));
    }

    private static void assertKeyword(Schema schema, String input, String keyword) {
        List<Error> errors = schema.validate(input, InputFormat.JSON);
        assertEquals(1, errors.size(), errors.toString());
        assertEquals(keyword, errors.get(0).getKeyword());
    }
}
