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

import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.serialization.*;

class Issue1357Test {
    static Stream<NodeReader> readers() {
        return Stream.of(BasicNodeReader.getInstance(), NodeReader.builder().build(),
                NodeReader.builder().locationAware().build(),
                NodeReader.builder().jsonMapper(JsonMapper.builder().build()).build(),
                NodeReader.builder().yamlMapper(com.fasterxml.jackson.dataformat.yaml.YAMLMapper.builder().build()).build(),
                NodeReader.builder().yamlMapper(com.fasterxml.jackson.dataformat.yaml.YAMLMapper.builder()
                        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build()).build(),
                NodeReader.builder().jsonMapper(JsonMapper.builder()
                        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build()).build());
    }
    private static SchemaRegistry registry(NodeReader reader) {
        return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12, b -> b.nodeReader(reader));
    }
    private static JsonNode stream(NodeReader reader, String text, InputFormat format) throws Exception {
        return reader.readTree(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), format);
    }

    @ParameterizedTest
    @MethodSource("readers")
    void issueExamplesUseTheExactWrittenDecimal(NodeReader reader) throws Exception {
        for (InputFormat format : new InputFormat[] { InputFormat.JSON, InputFormat.YAML }) {
            String[][] cases = { { "{\"multipleOf\":2}", "9007199254740993.0", "false" },
                    { "{\"maximum\":9007199254740992}", "9007199254740993.0", "false" },
                    { "{\"maximum\":9007199254740993.0}", "9007199254740993", "true" },
                    { "{\"minimum\":9007199254740993.0}", "9007199254740992", "false" } };
            for (String[] c : cases) {
                Schema[] schemas = { registry(reader).getSchema(c[0], format), registry(reader).getSchema(
                        new ByteArrayInputStream(c[0].getBytes(StandardCharsets.UTF_8)), format) };
                for (Schema schema : schemas) {
                    assertEquals(Boolean.parseBoolean(c[2]), schema.validate(c[1], format).isEmpty(), c[0]);
                    assertEquals(Boolean.parseBoolean(c[2]), schema.validate(stream(reader, c[1], format)).isEmpty(), c[0]);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("readers")
    void schemasInstancesLiteralsAndCopiesUseOneDecimalPolicy(NodeReader reader) throws Exception {
        for (InputFormat format : new InputFormat[] { InputFormat.JSON, InputFormat.YAML }) {
            String literal = "{\"foo\":0.99999999999999999999,\"minItems\":0.99999999999999999999}";
            JsonNode tree = reader.readTree(literal, format);
            assertTrue(tree.get("foo").isBigDecimal());
            assertEquals(0, tree.get("foo").decimalValue().compareTo(tree.get("minItems").decimalValue()));
            for (String text : new String[] { "{\"const\":" + literal + "}", "{\"enum\":[" + literal + "]}" }) {
                Schema schema = registry(reader).getSchema(text, format);
                assertTrue(schema.validate(literal, format).isEmpty());
                assertTrue(registry(reader).getSchema(schema.getSchemaNode().deepCopy()).validate(literal, format).isEmpty());
                assertFalse(schema.validate("{\"foo\":1.0,\"minItems\":1.0}", format).isEmpty());
            }
            Schema unique = registry(reader).getSchema("{\"uniqueItems\":true}");
            assertTrue(unique.validate("[" + literal + ",{\"foo\":1.0,\"minItems\":1.0}]", format).isEmpty());
            assertEquals(1, unique.validate("[" + literal + "," + literal + "]", format).size());
            JsonNode data = registry(reader).getSchema("{\"default\":" + literal + ",\"examples\":[" + literal + "]}", format).getSchemaNode();
            assertTrue(data.get("default").get("foo").isBigDecimal());
            assertTrue(data.get("examples").get(0).get("foo").isBigDecimal());
        }
    }

    @Test
    void losslessDoublesAndSignedZerosKeepTypeSignAndSerialization() throws Exception {
        NodeReader reader = NodeReader.builder().build();
        for (InputFormat format : new InputFormat[] { InputFormat.JSON, InputFormat.YAML }) {
            for (String value : new String[] { "0.0", "-0.0", "0.1", "1.5", "3000000000.0" }) {
                JsonNode node = reader.readTree(value, format);
                assertTrue(node.isDouble(), value);
                if (value.equals("0.0") || value.equals("-0.0")) {
                    assertEquals(value, node.toString());
                    assertEquals(value.startsWith("-") ? Long.MIN_VALUE : 0L, Double.doubleToRawLongBits(node.doubleValue()));
                    assertTrue(registry(reader).getSchema("{\"const\":0.0}").validate(value, format).isEmpty());
                    assertTrue(registry(reader).getSchema("{\"minimum\":0.0,\"maximum\":0.0}").validate(value, format).isEmpty());
                }
            }
        }
    }

    @Test
    void publicMapperFactoriesReadExactTreesIncludingTypedRoots() throws Exception {
        for (ObjectMapper mapper : new ObjectMapper[] { JsonMapperFactory.getInstance(), YamlMapperFactory.getInstance() }) {
            String text = "{\"value\":9007199254740993.0}";
            assertTrue(mapper.readTree(text).get("value").isBigDecimal());
            assertTrue(mapper.readTree(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))).get("value").isBigDecimal());
            assertTrue(mapper.readValue(text, ObjectNode.class).get("value").isBigDecimal());
            assertTrue(mapper.readValue("[9007199254740993.0]", ArrayNode.class).get(0).isBigDecimal());
            assertTrue(mapper.readTree("null").isNull());
            assertEquals(new BigDecimal("1E+400"), mapper.valueToTree(new BigDecimal("1E+400")).decimalValue());
        }
    }

    @Test
    void overflowAndExistingDivisorPolicyRemainUnchanged() throws Exception {
        NodeReader reader = NodeReader.builder().build();
        for (InputFormat format : new InputFormat[] { InputFormat.JSON, InputFormat.YAML }) {
            assertTrue(reader.readTree("1e400", format).isDouble());
            assertEquals(Double.POSITIVE_INFINITY, reader.readTree("1e400", format).doubleValue());
            assertTrue(registry(reader).getSchema("{\"multipleOf\":0}", format).validate("3", format).isEmpty());
            assertTrue(registry(reader).getSchema("{\"multipleOf\":-2}", format).validate("4", format).isEmpty());
            assertFalse(registry(reader).getSchema("{\"multipleOf\":-2}", format).validate("3", format).isEmpty());
            assertTrue(registry(reader).getSchema("{\"multipleOf\":1e400}", format).validate("3", format).isEmpty());
            assertFalse(registry(reader).getSchema("{\"minimum\":1e400}", format).validate("3", format).isEmpty());
            assertTrue(registry(reader).getSchema("{\"minItems\":1e400}", format).getSchemaNode().get("minItems").isBigDecimal());
        }
    }

    @Test
    void yamlExplicitFloatsAndSeparatorsKeepTheirMeaning() throws Exception {
        NodeReader reader = NodeReader.builder().build();
        for (String text : new String[] { "!!float 1_000", "!!float 1_000.0" }) {
            JsonNode value = reader.readTree(text, InputFormat.YAML);
            assertTrue(value.isNumber(), text);
            assertEquals(0, value.decimalValue().compareTo(new BigDecimal("1000")), text);
        }
        JsonNode value = reader.readTree("!!float 9_007_199_254_740_993.0", InputFormat.YAML);
        assertEquals(new BigDecimal("9007199254740993"), value.decimalValue().stripTrailingZeros());
    }
    @Test
    void finiteUnderflowAndLongMantissasKeepTheirWrittenValue() throws Exception {
        NodeReader reader = NodeReader.builder().build();
        for (InputFormat format : new InputFormat[] { InputFormat.JSON, InputFormat.YAML }) {
            for (String text : new String[] { "1e-400", "-1e-400", "0." + new String(new char[500]).replace('\0', '1') }) {
                JsonNode value = reader.readTree(text, format);
                assertTrue(value.isBigDecimal(), text);
                assertEquals(0, value.decimalValue().compareTo(new BigDecimal(text)));
            }
            assertFalse(registry(reader).getSchema("{\"multipleOf\":2}").validate("1e-400", format).isEmpty());
            assertFalse(registry(reader).getSchema("{\"minimum\":1e-400}", format).validate("0", format).isEmpty());
        }
    }

    @Test
    void underflowDivisorsUseTheirNonzeroWrittenValue() throws Exception {
        NodeReader reader = NodeReader.builder().build();
        for (InputFormat format : new InputFormat[] { InputFormat.JSON, InputFormat.YAML }) {
            Schema schema = registry(reader).getSchema("{\"multipleOf\":1e-400}", format);
            assertTrue(schema.validate("1e-400", format).isEmpty());
            assertFalse(schema.validate("5e-401", format).isEmpty());
        }
    }

    @Test
    void exponentsOutsideDecimalRangeRetainNativeParsing() throws Exception {
        NodeReader reader = NodeReader.builder().build();
        String exponent = new String(new char[900]).replace('\0', '9');
        for (InputFormat format : new InputFormat[] { InputFormat.JSON, InputFormat.YAML }) {
            for (String sign : new String[] { "", "-" }) {
                String text = "1e" + sign + exponent;
                for (JsonNode node : new JsonNode[] { reader.readTree(text, format), stream(reader, text, format) }) {
                    assertTrue(node.isDouble());
                    assertEquals(sign.isEmpty() ? Double.POSITIVE_INFINITY : 0.0, node.doubleValue());
                }
            }
        }
    }

    @Test
    void mapperReaderFlagsUpdatesAndNativeNumericNodesRemainSupported() throws Exception {
        for (ObjectMapper mapper : new ObjectMapper[] { JsonMapperFactory.getInstance(), YamlMapperFactory.getInstance() }) {
            assertTrue(mapper.reader().with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                    .readTree("0.1").isBigDecimal());
            assertTrue(mapper.reader().with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                    .readTree("1e400").isBigDecimal());
            assertTrue(mapper.reader().with(DeserializationFeature.USE_LONG_FOR_INTS).readTree("1").isLong());
            assertTrue(mapper.valueToTree(Float.valueOf(0.1f)).isFloat());
            ObjectNode target = mapper.createObjectNode().put("keep", true);
            mapper.readerForUpdating(target).readValue("{\"value\":9007199254740993.0}");
            assertTrue(target.get("keep").booleanValue());
            assertTrue(target.get("value").isBigDecimal());
            assertThrows(Exception.class, () -> mapper.reader().with(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
                    .readTree("{\"value\":1,\"value\":2}"));
            assertEquals(Double.valueOf(9007199254740992d), mapper.readValue(
                    "{\"value\":9007199254740993.0}", DoubleBean.class).value);
        }
    }
    public static class DoubleBean {
        public Double value;
    }

    @Test
    void locationAwareAndRebuildingFactoriesPreservePromotion() throws Exception {
        String text = "{\"value\":9007199254740993.0}";
        NodeReader location = NodeReader.builder().locationAware().build();
        NodeReader exactLocation = NodeReader.builder().locationAware().jsonMapper(JsonMapper.builder()
                .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build()).build();
        JsonNode value = location.readTree(text, InputFormat.JSON).get("value");
        assertTrue(value.isBigDecimal());
        assertNotNull(com.networknt.schema.utils.JsonNodes.tokenStreamLocationOf(value));
        assertEquals(com.networknt.schema.utils.JsonNodes.tokenStreamLocationOf(
                exactLocation.readTree(text, InputFormat.JSON).get("value")).toString(),
                com.networknt.schema.utils.JsonNodes.tokenStreamLocationOf(value).toString());
        NodeReader rebuilding = NodeReader.builder().jsonNodeFactoryFactory(parser -> new com.fasterxml.jackson.databind.node.JsonNodeFactory() {
            private static final long serialVersionUID = 1L;
            @Override
            public com.fasterxml.jackson.databind.node.NumericNode numberNode(BigDecimal decimal) {
                return com.fasterxml.jackson.databind.node.DecimalNode.valueOf(new BigDecimal(decimal.toString()));
            }
        }).build();
        assertEquals(0, rebuilding.readTree(text, InputFormat.JSON).get("value").decimalValue()
                .compareTo(new BigDecimal("9007199254740993")));
        NodeReader forcingDouble = NodeReader.builder().jsonNodeFactoryFactory(parser -> new com.fasterxml.jackson.databind.node.JsonNodeFactory() {
            private static final long serialVersionUID = 1L;
            @Override
            public com.fasterxml.jackson.databind.node.NumericNode numberNode(BigDecimal decimal) {
                return com.fasterxml.jackson.databind.node.DoubleNode.valueOf(decimal.doubleValue());
            }
        }).build();
        assertTrue(forcingDouble.readTree(text, InputFormat.JSON).get("value").isDouble());
    }

    @Test
    void promotedDefaultsAndAnnotationsKeepTheirShapeAndValue() throws Exception {
        NodeReader reader = NodeReader.builder().build();
        String literal = "{\"value\":0.99999999999999999999}";
        Schema schema = registry(reader).getSchema("{\"properties\":{\"data\":{\"default\":" + literal + "}}}");
        JsonNode instance = reader.readTree("{}", InputFormat.JSON);
        com.networknt.schema.walk.WalkConfig config = com.networknt.schema.walk.WalkConfig.builder()
                .applyDefaultsStrategy(new com.networknt.schema.walk.ApplyDefaultsStrategy(true, true, true)).build();
        assertTrue(schema.walk(instance, true, c -> c.setWalkConfig(config)).getErrors().isEmpty());
        assertEquals(literal, instance.get("data").toString());
        assertTrue(instance.get("data").get("value").isBigDecimal());
        Schema annotations = registry(reader).getSchema("{\"default\":" + literal + ",\"examples\":[" + literal + "]}");
        Object output = annotations.validate("{}", InputFormat.JSON, OutputFormat.LIST,
                context -> context.executionConfig(c -> c.annotationCollectionEnabled(true).annotationCollectionFilter(k -> true)));
        assertTrue(JsonMapperFactory.getInstance().writeValueAsString(output).contains(literal));
        JsonNode zeroDefault = reader.readTree("{\"default\":{\"value\":-0.0}}", InputFormat.JSON);
        assertEquals("{\"default\":{\"value\":-0.0}}", zeroDefault.toString());
    }

    @Test
    void sharedFactoryDeserializersDoNotLeakParserOrFeatureStateAcrossThreads() throws Exception {
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(8);
        try {
            java.util.List<java.util.concurrent.Callable<Boolean>> tasks = new java.util.ArrayList<>();
            for (int task = 0; task < 32; task++) {
                final boolean explicitDecimals = task % 2 == 0;
                tasks.add(() -> {
                    for (ObjectMapper mapper : new ObjectMapper[] { JsonMapperFactory.getInstance(), YamlMapperFactory.getInstance() }) {
                        for (int i = 0; i < 50; i++) {
                            JsonNode node = explicitDecimals ? mapper.reader().with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                                    .readTree("0.1") : mapper.readTree("0.1");
                            if (node.isBigDecimal() != explicitDecimals) return false;
                            if (!mapper.readTree("9007199254740993.0").isBigDecimal()) return false;
                        }
                    }
                    return true;
                });
            }
            for (java.util.concurrent.Future<Boolean> result : pool.invokeAll(tasks)) assertTrue(result.get());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void signedZeroDefaultsAnnotationsAndDiagnosticsKeepTheirPreviousRepresentation() throws Exception {
        NodeReader reader = NodeReader.builder().build();
        SchemaRegistry r = registry(reader);
        ObjectMapper plain = JsonMapper.builder().build();
        for (String zero : new String[] { "0.0", "-0.0" }) {
            String schemaText = "{\"properties\":{\"data\":{\"default\":" + zero + "}}}";
            JsonNode instance = reader.readTree("{}", InputFormat.JSON);
            com.networknt.schema.walk.WalkConfig config = com.networknt.schema.walk.WalkConfig.builder()
                    .applyDefaultsStrategy(new com.networknt.schema.walk.ApplyDefaultsStrategy(true, true, true)).build();
            assertTrue(r.getSchema(schemaText).walk(instance, true, c -> c.setWalkConfig(config)).getErrors().isEmpty());
            assertTrue(instance.get("data").isDouble());
            assertEquals(zero, instance.get("data").toString());
            assertEquals(zero.startsWith("-") ? Long.MIN_VALUE : 0L,
                    Double.doubleToRawLongBits(instance.get("data").doubleValue()));
            Object output = r.getSchema("{\"default\":" + zero + ",\"examples\":[" + zero + "]}")
                    .validate("{}", InputFormat.JSON, OutputFormat.LIST,
                            c -> c.executionConfig(e -> e.annotationCollectionEnabled(true).annotationCollectionFilter(k -> true)));
            assertTrue(JsonMapperFactory.getInstance().writeValueAsString(output).contains("\"default\":" + zero));
            for (String keyword : new String[] { "minimum", "maximum", "const" }) {
                String text = "{\"" + keyword + "\":" + zero + "}";
                String input = keyword.equals("maximum") ? "1" : "-1";
                java.util.List<Error> expected = r.getSchema(plain.readTree(text)).validate(plain.readTree(input));
                java.util.List<Error> actual = r.getSchema(text).validate(input, InputFormat.JSON);
                assertEquals(expected.get(0).getMessage(), actual.get(0).getMessage(), text);
                assertArrayEquals(expected.get(0).getArguments(), actual.get(0).getArguments(), text);
            }
        }
    }

}
