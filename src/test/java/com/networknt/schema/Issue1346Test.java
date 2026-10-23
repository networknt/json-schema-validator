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
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.networknt.schema.serialization.BasicNodeReader;
import com.networknt.schema.serialization.NodeReader;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
    void hugeCountsWorkInReferencedSchemasOutsideStandardContainers(NodeReader reader) {
        SchemaRegistry registry = registry(reader);
        String document = "{\"$ref\":\"#/components/schemas/Target\","
                + "\"components\":{\"schemas\":{\"Target\":{\"minItems\":1e400}}}}";
        assertEquals(1, registry.getSchema(document).validate("[]", InputFormat.JSON).size());
        String arbitrary = "{\"$ref\":\"#/arbitrary/location\","
                + "\"arbitrary\":{\"location\":{\"minProperties\":1e400}}}";
        assertEquals(1, registry.getSchema(arbitrary).validate("{}", InputFormat.JSON).size());
    }

    private enum InputPath {
        DEFAULT_READER, LEGACY_READER, JSON_NODE
    }

    static Stream<InputPath> inputPaths() {
        return Stream.of(InputPath.values());
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> draftInputs() {
        return Stream.of(SpecificationVersion.DRAFT_4, SpecificationVersion.DRAFT_6,
                SpecificationVersion.DRAFT_7, SpecificationVersion.DRAFT_2019_09,
                SpecificationVersion.DRAFT_2020_12)
                .flatMap(version -> inputPaths().map(path -> org.junit.jupiter.params.provider.Arguments.of(version, path)));
    }

    private static NodeReader legacyReader(NodeReader delegate) {
        // Deliberately implement only the established readTree API.
        return new NodeReader() {
            @Override
            public JsonNode readTree(String content, InputFormat inputFormat) {
                try {
                    return delegate.readTree(content, inputFormat);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }

            @Override
            public JsonNode readTree(java.io.InputStream content, InputFormat inputFormat) {
                try {
                    return delegate.readTree(content, inputFormat);
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        };
    }

    private static Schema schema(SpecificationVersion version, InputPath path, String text,
            boolean allowNonFiniteLiterals) throws Exception {
        NodeReader reader = allowNonFiniteLiterals
                ? NodeReader.builder().jsonMapper(JsonMapper.builder()
                        .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS).build()).build()
                : BasicNodeReader.getInstance();
        if (path == InputPath.JSON_NODE) {
            return SchemaRegistry.withDefaultDialect(version).getSchema(reader.readTree(text, InputFormat.JSON));
        }
        NodeReader selected = path == InputPath.LEGACY_READER ? legacyReader(reader) : reader;
        return SchemaRegistry.withDefaultDialect(version, builder -> builder.nodeReader(selected)).getSchema(text);
    }

    private static String instance(String kind, boolean empty) {
        return "Properties".equals(kind) ? (empty ? "{}" : "{\"a\":1}")
                : "Length".equals(kind) ? (empty ? "\"\"" : "\"a\"") : (empty ? "[]" : "[1]");
    }

    @ParameterizedTest
    @MethodSource("draftInputs")
    void overflowSignsAreEnforcedAcrossEveryInputPath(SpecificationVersion version, InputPath path) throws Exception {
        for (String kind : new String[] { "Items", "Properties", "Length" }) {
            for (boolean positive : new boolean[] { true, false }) {
                String limit = positive ? "1e400" : "-1e400";
                for (String prefix : new String[] { "min", "max" }) {
                    String keyword = prefix + kind;
                    List<Error> errors = schema(version, path, "{\"" + keyword + "\":" + limit + "}", false)
                            .validate(instance(kind, true), InputFormat.JSON);
                    boolean violates = "min".equals(prefix) == positive;
                    assertEquals(violates ? 1 : 0, errors.size(), keyword + ": " + limit);
                    if (violates) {
                        String argument = path == InputPath.DEFAULT_READER ? (positive ? "1E+400" : "-1E+400")
                                : (positive ? "Infinity" : "-Infinity");
                        assertEquals(argument, errors.get(0).getArguments()[0]);
                        assertTrue(errors.get(0).getMessage().contains(argument));
                    }
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("draftInputs")
    void nanKeepsTheExistingFallbacks(SpecificationVersion version, InputPath path) throws Exception {
        for (String kind : new String[] { "Items", "Properties", "Length" }) {
            String text = "{\"min" + kind + "\":NaN,\"max" + kind + "\":NaN}";
            assertTrue(schema(version, path, text, true).validate(instance(kind, false), InputFormat.JSON).isEmpty());
        }
    }

    @ParameterizedTest
    @MethodSource("inputPaths")
    void finiteExponentMessagesStayShort(InputPath path) throws Exception {
        for (String kind : new String[] { "Items", "Properties", "Length", "Contains" }) {
            for (String limit : new String[] { "1e30", "1e308" }) {
                String contains = "Contains".equals(kind) ? "\"contains\":true," : "";
                String text = "{" + contains + "\"min" + kind + "\":" + limit + "}";
                Error error = schema(SpecificationVersion.DRAFT_2020_12, path, text, false)
                        .validate(instance(kind, true), InputFormat.JSON).get(0);
                String expected = "1e30".equals(limit) ? "1E+30" : "1E+308";
                assertEquals(expected, error.getArguments()[0]);
                assertTrue(error.getMessage().contains(expected));
                assertTrue(error.getMessage().length() < 200);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("inputPaths")
    void equivalentFiniteSpellingsHaveTheSameMessageArgument(InputPath path) throws Exception {
        for (String limit : new String[] { "2147483648", "2147483648.0", "2.147483648e9" }) {
            Error error = schema(SpecificationVersion.DRAFT_2020_12, path, "{\"minItems\":" + limit + "}", false)
                    .validate("[]", InputFormat.JSON).get(0);
            assertEquals("2147483648", error.getArguments()[0]);
        }
    }

    private static Result annotated(Schema schema) {
        return schema.validate("[1]", InputFormat.JSON, OutputFormat.RESULT,
                context -> context.executionConfig(config -> config.annotationCollectionEnabled(true)
                        .annotationCollectionFilter(keyword -> true)));
    }

    private static Object annotation(Result result, String keyword) {
        return result.getExecutionContext().getAnnotations().asMap().values().stream().flatMap(List::stream)
                .filter(value -> keyword.equals(value.getKeyword())).findFirst().get().getValue();
    }

    @ParameterizedTest
    @MethodSource("inputPaths")
    void overflowedContainsBoundsKeepTheirOwnValue(InputPath path) throws Exception {
        for (String keyword : new String[] { "minContains", "maxContains" }) {
            Result result = annotated(schema(SpecificationVersion.DRAFT_2020_12, path,
                    "{\"contains\":true,\"" + keyword + "\":1e400}", false));
            assertEquals("minContains".equals(keyword) ? 1 : 0, result.getErrors().size());
            if (!result.getErrors().isEmpty()) {
                assertEquals(path == InputPath.DEFAULT_READER ? "1E+400" : "Infinity",
                        result.getErrors().get(0).getArguments()[0]);
            }
            assertEquals(path == InputPath.DEFAULT_READER ? new BigDecimal("1e400")
                    : Double.valueOf(Double.POSITIVE_INFINITY), annotation(result, keyword));
        }
    }

    @ParameterizedTest
    @MethodSource("inputPaths")
    void negativeAndNanContainsBoundsKeepTheirSchemaErrors(InputPath path) throws Exception {
        for (String keyword : new String[] { "minContains", "maxContains" }) {
            for (String limit : new String[] { "-1e400", "NaN" }) {
                List<Error> errors = schema(SpecificationVersion.DRAFT_2020_12, path,
                        "{\"contains\":true,\"" + keyword + "\":" + limit + "}", true)
                        .validate("[1]", InputFormat.JSON);
                assertTrue(errors.stream().anyMatch(error -> keyword.equals(error.getKeyword())
                        && error.getArguments()[0].toString().startsWith("{")));
            }
        }
    }

    @Test
    void programmaticFloatAndDoubleNodesUseSaturationAndNanFallbacks() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        for (boolean useFloat : new boolean[] { true, false }) {
            for (double number : new double[] { Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN }) {
                for (String kind : new String[] { "Items", "Properties", "Length" }) {
                    ObjectNode node = JsonNodeFactory.instance.objectNode();
                    JsonNode limit = useFloat ? JsonNodeFactory.instance.numberNode((float) number)
                            : JsonNodeFactory.instance.numberNode(number);
                    node.set("min" + kind, limit);
                    node.set("max" + kind, limit);
                    List<Error> errors = registry.getSchema(node).validate(instance(kind, true), InputFormat.JSON);
                    assertEquals(Double.isNaN(number) ? 0 : 1, errors.size());
                    if (!errors.isEmpty()) {
                        assertEquals((number > 0 ? "min" : "max") + kind, errors.get(0).getKeyword());
                        assertEquals(number > 0 ? "Infinity" : "-Infinity", errors.get(0).getArguments()[0]);
                    }
                }
            }
        }
    }

    @Test
    void programmaticBigDecimalContainsAnnotationStaysExactWithoutExpansion() {
        for (BigDecimal limit : new BigDecimal[] { new BigDecimal("1e1000000000"), new BigDecimal("2147483648.000") }) {
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("contains", true);
            node.put("maxContains", limit);
            Result result = annotated(SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(node));
            assertTrue(result.getErrors().isEmpty());
            assertEquals(limit, annotation(result, "maxContains"));
        }
    }

    @Test
    void programmaticBigDecimalAtTheScaleBoundaryDoesNotExpandOrThrow() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("minItems", new BigDecimal(java.math.BigInteger.valueOf(100), Integer.MIN_VALUE + 1));
        List<Error> errors = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema(node).validate("[]", InputFormat.JSON);
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMessage().length() < 200);
    }

    @ParameterizedTest
    @MethodSource("inputPaths")
    void integerLimitsUseCanonicalExactMessages(InputPath path) throws Exception {
        for (String limit : new String[] { "3000000000", "12345678901234567890", "10000000000" }) {
            for (String kind : new String[] { "Items", "Properties", "Length", "Contains" }) {
                String contains = "Contains".equals(kind) ? "\"contains\":true," : "";
                Schema schema = schema(SpecificationVersion.DRAFT_2020_12, path,
                        "{" + contains + "\"min" + kind + "\":" + limit + "}", false);
                List<Error> errors = schema.validate(instance(kind, true), InputFormat.JSON);
                assertEquals(1, errors.size());
                String expected = new BigDecimal(limit).stripTrailingZeros().toString();
                assertEquals(expected, errors.get(0).getArguments()[0]);
                assertTrue(errors.get(0).getMessage().contains(expected));
            }
        }
    }

    @Test
    void programmaticBigDecimalLimitsUseCanonicalExactMessages() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        for (String limit : new String[] { "3000000000", "12345678901234567890", "10000000000" }) {
            for (String kind : new String[] { "Items", "Properties", "Length", "Contains" }) {
                ObjectNode node = JsonNodeFactory.instance.objectNode();
                node.put("min" + kind, new BigDecimal(limit));
                if ("Contains".equals(kind)) {
                    node.put("contains", true);
                }
                List<Error> errors = registry.getSchema(node).validate(instance(kind, true), InputFormat.JSON);
                assertEquals(1, errors.size());
                String expected = new BigDecimal(limit).stripTrailingZeros().toString();
                assertEquals(expected, errors.get(0).getArguments()[0]);
                assertTrue(errors.get(0).getMessage().contains(expected));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("readers")
    void yamlOverflowLimitsAreEnforcedAcrossReaderAndNodeInputs(NodeReader reader) throws Exception {
        for (String kind : new String[] { "Items", "Properties", "Length", "Contains" }) {
            String text = ("Contains".equals(kind) ? "contains: true\n" : "")
                    + "min" + kind + ": 1.0e+400\n";
            for (NodeReader selected : new NodeReader[] { reader, legacyReader(reader) }) {
                SchemaRegistry registry = registry(selected);
                Schema[] schemas = {
                        registry.getSchema(text, InputFormat.YAML),
                        registry.getSchema(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), InputFormat.YAML),
                        registry.getSchema(selected.readTree(text, InputFormat.YAML)) };
                for (Schema schema : schemas) {
                    List<Error> errors = schema.validate(instance(kind, true), InputFormat.JSON);
                    assertEquals(1, errors.size(), kind);
                    assertEquals("min" + kind, errors.get(0).getKeyword());
                }
            }
        }
    }

    @Test
    void exactDecimalExponentSpellingsHaveTheSameMessageWithoutChangingAnnotations() {
        NodeReader reader = NodeReader.builder().jsonMapper(JsonMapper.builder()
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build()).build();
        for (String limit : new String[] { "1e400", "1.0e400", "10.00e399" }) {
            Schema schema = registry(reader).getSchema("{\"contains\":true,\"minContains\":" + limit + "}");
            Result result = annotated(schema);
            assertEquals("1E+400", result.getErrors().get(0).getArguments()[0]);
            assertEquals(schema.getSchemaNode().get("minContains").numberValue(), annotation(result, "minContains"));
        }
    }

    @Test
    void largeIntegralFloatsAreEnforcedWithoutRoundingFractionalFloats() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        for (String kind : new String[] { "Items", "Properties", "Length" }) {
            for (float limit : new float[] { 3.0e9f, -3.0e9f }) {
                ObjectNode node = JsonNodeFactory.instance.objectNode();
                node.put("min" + kind, limit);
                node.put("max" + kind, limit);
                List<Error> errors = registry.getSchema(node).validate(instance(kind, true), InputFormat.JSON);
                assertEquals(1, errors.size());
                assertEquals((limit > 0 ? "min" : "max") + kind, errors.get(0).getKeyword());
            }
            ObjectNode fractional = JsonNodeFactory.instance.objectNode();
            fractional.put("min" + kind, 1.5f);
            fractional.put("max" + kind, 1.5f);
            assertTrue(registry.getSchema(fractional).validate(instance(kind, true), InputFormat.JSON).isEmpty());
            assertTrue(registry.getSchema(fractional).validate(instance(kind, false), InputFormat.JSON).isEmpty());
        }
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("contains", true);
        node.put("minContains", 3.0e9f);
        List<Error> errors = registry.getSchema(node).validate("[1]", InputFormat.JSON);
        assertEquals(1, errors.size());
        assertEquals("minContains", errors.get(0).getKeyword());
        assertEquals("3E+9", errors.get(0).getArguments()[0]);
    }
}
