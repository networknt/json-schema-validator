/*
 * Copyright (c) 2016 Network New Technologies Inc.
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.networknt.schema.serialization.BasicNodeReader;
import com.networknt.schema.serialization.NodeReader;
import com.networknt.schema.utils.JsonNodes;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

class Issue1346Test {
    private static final String[] COUNT_NAMES = { "minItems", "maxItems", "minProperties", "maxProperties",
            "minLength", "maxLength", "minContains", "maxContains" };

    static Stream<NodeReader> readers() {
        return Stream.of(BasicNodeReader.getInstance(), NodeReader.builder().build(),
                NodeReader.builder().locationAware().build(),
                NodeReader.builder().jsonMapper(JsonMapper.builder()
                        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build()).build());
    }

    private static SchemaRegistry registry(NodeReader reader) {
        return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.nodeReader(reader));
    }

    @ParameterizedTest
    @MethodSource("readers")
    void literalCountNamesHaveTheSameEqualityAsOrdinaryNames(NodeReader reader) {
        SchemaRegistry registry = registry(reader);
        for (String name : COUNT_NAMES) {
            for (String number : new String[] { "1.0", "1.00", "2.50" }) {
                String data = "{\"" + name + "\":" + number + "}";
                for (String keyword : new String[] { "const", "enum" }) {
                    String value = "enum".equals(keyword) ? "[" + data + "]" : data;
                    Schema schema = registry.getSchema("{\"" + keyword + "\":" + value + "}");
                    assertTrue(schema.validate(data, InputFormat.JSON).isEmpty(), keyword + ": " + data);
                    assertEquals(1, schema.validate("{\"" + name + "\":7}", InputFormat.JSON).size());
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("readers")
    void opaqueDataKeepsTheConfiguredReadersNumericRepresentation(NodeReader reader) throws Exception {
        SchemaRegistry registry = registry(reader);
        for (String keyword : new String[] { "const", "enum", "default", "examples", "x-annotation" }) {
            for (String name : COUNT_NAMES) {
                // Schema-looking objects remain data throughout a literal or annotation subtree.
                String data = "{\"properties\":{\"p\":{\"" + name + "\":2.50}}}";
                String value = "enum".equals(keyword) || "examples".equals(keyword) ? "[" + data + "]" : data;
                String text = "{\"" + keyword + "\":" + value + "}";
                assertEquals(reader.readTree(text, InputFormat.JSON), registry.getSchema(text).getSchemaNode(), text);
                assertEquals(reader.readTree(text, InputFormat.JSON).toString(),
                        registry.getSchema(text).getSchemaNode().toString(), text);
                assertEquals(reader.readTree(text, InputFormat.JSON),
                        registry.getSchema(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)),
                                InputFormat.JSON).getSchemaNode(), text);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("readers")
    void keywordNamesCanAlsoBePropertyNames(NodeReader reader) {
        SchemaRegistry registry = registry(reader);
        for (String name : new String[] { "minItems", "const", "properties", "default", "items" }) {
            Schema schema = registry.getSchema("{\"properties\":{\"" + name + "\":{\"const\":1.0}}}");
            assertTrue(schema.validate("{\"" + name + "\":1.00}", InputFormat.JSON).isEmpty(), name);
            Schema minimum = registry.getSchema("{\"properties\":{\"" + name + "\":{\"minItems\":1e400}}}");
            assertEquals(1, minimum.validate("{\"" + name + "\":[]}", InputFormat.JSON).size(), name);
        }
    }

    @ParameterizedTest
    @MethodSource("readers")
    void supportedSubschemaContainersPreserveHugeCountLimits(NodeReader reader) {
        SchemaRegistry registry = registry(reader);
        for (String keyword : new String[] { "not", "if", "then", "else", "items", "contains",
                "additionalItems", "additionalProperties", "unevaluatedItems", "unevaluatedProperties",
                "propertyNames", "contentSchema" }) {
            String text = "{\"" + keyword + "\":{\"minItems\":1e400}}";
            assertEquals(0, new BigDecimal("1e400").compareTo(JsonNodes.decimalValueForCountLimit(registry.getSchema(text)
                    .getSchemaNode().get(keyword).get("minItems"))), keyword);
        }
        for (String keyword : new String[] { "allOf", "anyOf", "oneOf", "prefixItems", "items" }) {
            String text = "{\"" + keyword + "\":[{\"minItems\":1e400}]}";
            assertEquals(0, new BigDecimal("1e400").compareTo(JsonNodes.decimalValueForCountLimit(("items".equals(keyword)
                    ? SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7, builder -> builder.nodeReader(reader))
                    : registry).getSchema(text).getSchemaNode().get(keyword).get(0).get("minItems"))), keyword);
        }
        for (String keyword : new String[] { "$defs", "definitions", "properties", "patternProperties",
                "dependentSchemas", "dependencies" }) {
            String text = "{\"" + keyword + "\":{\"const\":{\"minItems\":1e400}}}";
            assertEquals(0, new BigDecimal("1e400").compareTo(JsonNodes.decimalValueForCountLimit(registry.getSchema(text)
                    .getSchemaNode().get(keyword).get("const").get("minItems"))), keyword);
        }
    }

    @ParameterizedTest
    @MethodSource("readers")
    void hugeCountsWorkInReferencedSchemasOutsideStandardContainers(NodeReader reader) {
        SchemaRegistry registry = registry(reader);
        String document = "{\"$ref\":\"#/components/schemas/Target\","
                + "\"components\":{\"schemas\":{\"Target\":{\"minItems\":1e400}}}}";
        assertEquals(1, registry.getSchema(document).validate("[]", InputFormat.JSON).size());
        String arbitrary = "{\"$ref\":\"#/arbitrary/location\","
                + "\"arbitrary\":{\"location\":{\"minProperties\":1e400}}}";
        assertEquals(1, registry.getSchema(arbitrary).validate("{}", InputFormat.JSON).size());
    }
}
