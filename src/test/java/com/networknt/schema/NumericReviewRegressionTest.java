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
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

import com.networknt.schema.keyword.MultipleOfValidator;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.path.PathType;
import com.networknt.schema.serialization.JsonMapperFactory;
import com.networknt.schema.serialization.YamlMapperFactory;
import com.networknt.schema.serialization.NodeReader;
import com.networknt.schema.utils.DecimalUtils;
import com.networknt.schema.utils.JsonNodes;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.DecimalNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

class NumericReviewRegressionTest {
    private SchemaRegistry registry() {
        return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
    }

    @Test
    void extremeInputDoesNotCrashOtherKeywords() {
        for (InputFormat format : InputFormat.values()) {
            if (format != InputFormat.JSON && format != InputFormat.YAML) continue;
            assertEquals(1, registry().getSchema("{\"multipleOf\":3}").validate("100e2147483647", format).size());
            assertEquals(0, registry().getSchema("{\"multipleOf\":2}").validate("100e2147483647", format).size());
            assertEquals(1, registry().getSchema("{\"enum\":[1]}").validate("100e2147483647", format).size());
            assertEquals(0, registry().getSchema("{\"enum\":[100e2147483647]}").validate("100e2147483647", format).size());
            assertEquals(1, registry().getSchema("{\"uniqueItems\":true}").validate("[100e2147483647,100e2147483647]", format).size());
            assertEquals(1, registry().getSchema("{\"uniqueItems\":true}").validate("[{\"n\":[100e2147483647]},{\"n\":[100e2147483647]}]", format).size());
        }
    }

    @Test
    void numericPrecisionIsNeededWithoutMultipleOf() throws Exception {
        String[] schemas = {
                "{\"const\":9007199254740993}",
                "{\"enum\":[9007199254740993]}",
                "{\"$defs\":{\"n\":{\"enum\":[9007199254740993]}},\"$ref\":\"#/$defs/n\"}",
                "{\"allOf\":[{\"enum\":[9007199254740993]}]}"
        };
        for (String schema : schemas) {
            for (InputFormat format : new InputFormat[]{InputFormat.JSON, InputFormat.YAML}) {
                assertTrue(registry().getSchema(schema).validate("9007199254740993.0", format).isEmpty(), schema);
            }
        }
        assertTrue(registry().getSchema("{\"uniqueItems\":true}")
                .validate("[9007199254740992.0,9007199254740993.0]", InputFormat.JSON).isEmpty());
        assertTrue(registry().getSchema("{\"type\":\"integer\"}").validate("1e400", InputFormat.JSON).isEmpty());
    }

    @Test
    void objectReadersPreserveNamesValuesAndLocations() throws Exception {
        String input = "{\"한글\":1.25,\"a\\\"b\":9007199254740993.0,\"nested\":{\"n\":2.5},"
                + "\"empty\":{},\"values\":[{},3.5],\"tail\":7,\"duplicate\":1,\"duplicate\":2}";
        SchemaRegistry ordinary = registry();
        SchemaRegistry located = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                b -> b.nodeReader(NodeReader.builder().locationAware().build()));
        for (SchemaRegistry reader : new SchemaRegistry[]{ordinary, located}) {
            for (InputFormat format : new InputFormat[]{InputFormat.JSON, InputFormat.YAML}) {
                for (boolean stream : new boolean[]{false, true}) {
                    JsonNode tree = stream ? reader.readTree(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), format)
                            : reader.readTree(input, format);
                    assertEquals(0, new BigDecimal("1.25").compareTo(tree.get("한글").decimalValue()));
                    assertEquals(0, new BigDecimal("9007199254740993.0").compareTo(tree.get("a\"b").decimalValue()));
                    assertEquals(0, new BigDecimal("2.5").compareTo(tree.get("nested").get("n").decimalValue()));
                    assertEquals(0, tree.get("empty").size());
                    assertEquals(0, tree.get("values").get(0).size());
                    assertEquals(7, tree.get("tail").intValue());
                    assertEquals(2, tree.get("duplicate").intValue());
                    if (reader == located) {
                        assertNotNull(JsonNodes.tokenStreamLocationOf(tree.get("a\"b")));
                        assertNotNull(JsonNodes.tokenStreamLocationOf(tree.get("tail")));
                    }
                }
            }
        }
    }

    @Test
    void publicMappersAndOrdinaryNodesRemainCompact() throws Exception {
        assertEquals(Double.class, JsonMapperFactory.getInstance().readValue("{\"n\":1.25}", Map.class).get("n").getClass());
        assertEquals(Double.class, YamlMapperFactory.getInstance().readValue("n: 1.25", Map.class).get("n").getClass());
        for (InputFormat format : new InputFormat[]{InputFormat.JSON, InputFormat.YAML}) {
            assertTrue(registry().readTree("1.25", format).isDouble());
            assertTrue(registry().readTree("9007199254740993.0", format).isBigDecimal());
            assertEquals(0, new BigDecimal("9007199254740993.0").compareTo(
                    registry().readTree("9007199254740993.0", format).decimalValue()));
        }
    }

    @Test
    void compactReaderNeverChangesTheDecimalValue() throws Exception {
        Random random = new Random(129014);
        for (int i = 0; i < 10000; i++) {
            long coefficient = random.nextLong() % 1000000000000000L;
            BigDecimal value = BigDecimal.valueOf(coefficient, random.nextInt(16));
            assertEquals(0, value.compareTo(registry().readTree(value.toString(), InputFormat.JSON).decimalValue()));
        }
        for (String text : new String[]{"1e23", "1e-324", "4.9406564584124654e-324", "1.0000000000000001",
                "2.2250738585072014e-308", "999999999999999.9", "-9007199254740993.0"}) {
            assertEquals(0, new BigDecimal(text).compareTo(registry().readTree(text, InputFormat.JSON).decimalValue()));
        }
    }

    @Test
    void mixedNumbersRetainValuesAcrossReaders() throws Exception {
        String[] values = {"0.0", "-0.0", "1.25", "-99999999999999.9", "0.00000000000001",
                "99999999999999.99", "1.0000000000000001", "9007199254740993.0",
                "1e2", "1e-400", "1e400", "100e2147483647", "2.5", "-2.5",
                "12345.6789e-2", "-12.5E+3", "-12.5E-3", "1e15", "1e14", "1e-15", "1e-16"};
        String input = "{\"values\":[" + String.join(",", values) + "],\"empty\":null}";
        SchemaRegistry ordinary = registry();
        SchemaRegistry located = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                b -> b.nodeReader(NodeReader.builder().locationAware().build()));
        for (SchemaRegistry reader : new SchemaRegistry[]{ordinary, located}) {
            for (InputFormat format : new InputFormat[]{InputFormat.JSON, InputFormat.YAML}) {
                for (boolean stream : new boolean[]{false, true}) {
                    JsonNode tree = stream ? reader.readTree(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)), format)
                            : reader.readTree(input, format);
                    assertTrue(tree.get("empty").isNull());
                    for (int i = 0; i < values.length; i++) {
                        JsonNode number = tree.get("values").get(i);
                        assertEquals(0, new BigDecimal(values[i]).compareTo(number.decimalValue()), values[i]);
                        if (reader == located) {
                            assertNotNull(JsonNodes.tokenStreamLocationOf(number));
                        }
                    }
                }
                assertTrue(reader.readTree("null", format).isNull());
            }
        }
    }

    @Test
    void compactNumbersMatchDecimalOracleInContainers() throws Exception {
        Random random = new Random(129011);
        StringBuilder input = new StringBuilder("[");
        BigDecimal[] expected = new BigDecimal[2000];
        for (int i = 0; i < expected.length; i++) {
            long coefficient = random.nextLong() % 1000000000000000L;
            expected[i] = BigDecimal.valueOf(coefficient, random.nextInt(16));
            if (i > 0) input.append(',');
            input.append(expected[i].toPlainString());
        }
        input.append(']');
        for (InputFormat format : new InputFormat[]{InputFormat.JSON, InputFormat.YAML}) {
            JsonNode actual = registry().readTree(input.toString(), format);
            for (int i = 0; i < expected.length; i++) {
                assertEquals(0, expected[i].compareTo(actual.get(i).decimalValue()), "number " + i);
            }
        }
    }

    @Test
    void callerSuppliedDoubleMapperRetainsLegacyUnderflowBehavior() {
        SchemaRegistry custom = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                b -> b.nodeReader(NodeReader.builder().jsonMapper(JsonMapper.builder()
                        .disable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build()).build()));
        assertEquals(0, custom.getSchema("{\"multipleOf\":1e-400}").validate("1", InputFormat.JSON).size());
        assertEquals(0, custom.getSchema("{\"multipleOf\":2}").validate("9007199254740993.0", InputFormat.JSON).size());
        assertEquals(1, registry().getSchema("{\"multipleOf\":2}").validate("9007199254740993.0", InputFormat.JSON).size());
    }

    @Test
    void locationAwareAndStreamReadersRetainPrecision() {
        SchemaRegistry located = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                b -> b.nodeReader(NodeReader.builder().locationAware().build()));
        for (InputFormat format : new InputFormat[]{InputFormat.JSON, InputFormat.YAML}) {
            JsonNode value = located.readTree(new ByteArrayInputStream("9007199254740993.0".getBytes(StandardCharsets.UTF_8)), format);
            assertEquals(0, new BigDecimal("9007199254740993.0").compareTo(value.decimalValue()));
            assertNotNull(JsonNodes.tokenStreamLocationOf(value));
            assertEquals(1, located.getSchema("{\"multipleOf\":2}").validate(value).size());
        }
    }

    @Test
    void largeIntegersAreComparedWithoutNarrowing() {
        for (String keyword : new String[]{"minimum", "maximum", "exclusiveMinimum", "exclusiveMaximum"}) {
            for (String type : new String[]{"", "\"type\":\"integer\","}) {
            Schema schema = registry().getSchema("{"+type+"\""+keyword+"\":0}");
            boolean lower = keyword.toLowerCase().contains("minimum");
            for (String magnitude : new String[]{"1e19", "1e400", "100e2147483647"}) {
                assertEquals(lower ? 0 : 1, schema.validate(magnitude, InputFormat.JSON).size());
                assertEquals(lower ? 1 : 0, schema.validate("-"+magnitude, InputFormat.JSON).size());
            }
            Schema extremeLimit = registry().getSchema("{"+type+"\""+keyword+"\":100e2147483647}");
            assertEquals(keyword.startsWith("exclusive") ? 1 : 0,
                    extremeLimit.validate("100e2147483647", InputFormat.JSON).size());
            }
        }
    }

    @Test
    void largeCountLimitsDoNotOverflow() {
        for (String suffix : new String[]{"Items", "Properties", "Length"}) {
            String input = suffix.equals("Items") ? "[1]" : suffix.equals("Properties") ? "{\"a\":1}" : "\"a\"";
            for (String limit : new String[]{"2147483648.0", "1e10", "1e400"}) {
                assertEquals(0, registry().getSchema("{\"max"+suffix+"\":"+limit+"}").validate(input, InputFormat.JSON).size());
                assertEquals(1, registry().getSchema("{\"min"+suffix+"\":"+limit+"}").validate(input, InputFormat.JSON).size());
            }
        }
    }

    @Test
    void looseExponentOverflowProducesAnError() {
        SchemaRegistry loose = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                b -> b.schemaRegistryConfig(SchemaRegistryConfig.builder().typeLoose(true).build()));
        for (String text : new String[]{"1e2147483648", "1e-2147483649", "1e99999999999999"}) {
            assertEquals(1, loose.getSchema("{\"multipleOf\":3}").validate("\""+text+"\"", InputFormat.JSON).size());
        }
    }

    @Test
    void conversionOverridesAreNotBypassed() {
        Schema parent = registry().getSchema("{\"multipleOf\":2}");
        MultipleOfValidator divisorOverride = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                IntNode.valueOf(2), parent, parent.getSchemaContext()) {
            @Override protected BigDecimal getDivisor(JsonNode node) { return BigDecimal.valueOf(3); }
        };
        ExecutionContext context = new ExecutionContext();
        context.evaluationPath = new NodePath(PathType.JSON_POINTER);
        divisorOverride.validate(context, IntNode.valueOf(3), IntNode.valueOf(3), context.evaluationPath);
        assertTrue(context.getErrors().isEmpty());
        divisorOverride.validate(context, IntNode.valueOf(2), IntNode.valueOf(2), context.evaluationPath);
        assertEquals("must be multiple of 3", context.getErrors().get(0).getMessage());

        final boolean[] called = {false};
        MultipleOfValidator dividendOverride = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                IntNode.valueOf(2), parent, parent.getSchemaContext()) {
            @Override protected BigDecimal getDividend(JsonNode node) { called[0]=true; return BigDecimal.valueOf(6); }
        };
        context = new ExecutionContext();
        context.evaluationPath = new NodePath(PathType.JSON_POINTER);
        dividendOverride.validate(context, IntNode.valueOf(3), IntNode.valueOf(3), context.evaluationPath);
        assertTrue(called[0]);
        assertTrue(context.getErrors().isEmpty());
    }

    @Test
    void messagesDoNotExposeNormalizationDetails() {
        for (String divisor : new String[]{"100", "100.0", "1e2"}) {
            assertEquals("must be multiple of 100", registry().getSchema("{\"multipleOf\":"+divisor+"}")
                    .validate("3", InputFormat.JSON).get(0).getMessage());
        }
    }

    @Test
    void longTrailingZeroInputAndEquivalentDivisorsStayBounded() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            String digits = "1" + new String(new char[199999]).replace('\0','0');
            SchemaRegistry loose = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                    b -> b.schemaRegistryConfig(SchemaRegistryConfig.builder().typeLoose(true).build()));
            assertEquals(1, loose.getSchema("{\"multipleOf\":3}").validate("\""+digits+"\"", InputFormat.JSON).size());
            ObjectNode schema = JsonNodeFactory.instance.objectNode();
            schema.set("multipleOf", DecimalNode.valueOf(new BigDecimal(BigInteger.TEN.pow(10000),10000)));
            Schema unit = registry().getSchema(schema);
            for (int i=0;i<100;i++) assertTrue(unit.validate(DecimalNode.valueOf(new BigDecimal("1e5000000"))).isEmpty());
        });
    }

    @Test
    void normalizationPreservesValueEqualityAndHashAtScaleLimits() {
        for (int zeros : new int[]{0,1,17,18,19,100}) {
            for (int scale : new int[]{Integer.MIN_VALUE,Integer.MIN_VALUE+1,Integer.MIN_VALUE+18,-1,0,1,Integer.MAX_VALUE-1}) {
                for (int sign : new int[]{-1,1}) {
                    BigInteger coefficient=BigInteger.valueOf(3*sign).multiply(BigInteger.TEN.pow(zeros));
                    BigDecimal a=new BigDecimal(coefficient,scale);
                    BigDecimal b=new BigDecimal(coefficient.multiply(BigInteger.TEN),scale+1);
                    BigDecimal key=DecimalUtils.normalize(a);
                    assertEquals(0,a.compareTo(key));
                    assertEquals(key,DecimalUtils.normalize(b));
                    assertEquals(key.hashCode(),DecimalUtils.normalize(b).hashCode());
                    assertEquals(key,DecimalUtils.normalize(key));
                }
            }
        }
        assertEquals(BigDecimal.ZERO,DecimalUtils.normalize(new BigDecimal(BigInteger.ZERO,Integer.MIN_VALUE)));
    }
}
