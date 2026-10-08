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

package com.networknt.schema.serialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.utils.JsonNodes;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.JacksonException;
import tools.jackson.core.TokenStreamLocation;
import tools.jackson.core.util.JsonParserDelegate;
import tools.jackson.core.util.JsonParserSequence;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.JsonNodeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

class ExactMapperRegressionTest {
    static Stream<Arguments> readerModes() {
        return Stream.of(InputFormat.JSON, InputFormat.YAML).flatMap(format ->
                Stream.of(false, true).flatMap(located ->
                        Stream.of(false, true).map(stream -> Arguments.of(format, located, stream))));
    }

    @ParameterizedTest
    @MethodSource("readerModes")
    void equivalentDecimalsHaveEqualNestedValues(InputFormat format, boolean located, boolean stream) {
        NodeReader reader = located ? NodeReader.builder().locationAware().build() : NodeReader.builder().build();
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.nodeReader(reader));
        String[][] equivalents = {
                {"1.10", "1.1000000000000000000"},
                {"9007199254740993.0", "9007199254740993.00"},
                {"1.0000000000000001", "1.000000000000000100"},
                {"1e-400", "1.00e-400"},
                {"100e2147483647", "1000e2147483646"}
        };
        for (String[] pair : equivalents) {
            String leftText = "{\"outer\":[{\"n\":" + pair[0] + "}]}";
            String rightText = "{\"outer\":[{\"n\":" + pair[1] + "}]}";
            JsonNode left = read(reader, leftText, format, stream);
            JsonNode right = read(reader, rightText, format, stream);
            assertEquals(left, right, pair[0] + " == " + pair[1]);
            assertEquals(left.hashCode(), right.hashCode(), pair[0]);
            if (located) {
                assertNotNull(JsonNodes.tokenStreamLocationOf(left.get("outer").get(0).get("n")));
                assertNotNull(JsonNodes.tokenStreamLocationOf(right.get("outer").get(0).get("n")));
            }
            for (String keyword : new String[]{"const", "enum"}) {
                String value = "enum".equals(keyword) ? "[" + leftText + "]" : leftText;
                Schema schema = readSchema(registry, "{\"" + keyword + "\":" + value + "}", format, stream);
                assertTrue(schema.validate(right).isEmpty(), keyword + ": " + pair[0]);
            }
            Schema unique = readSchema(registry, "{\"uniqueItems\":true}", format, stream);
            assertEquals(1, unique.validate(read(reader, "[" + leftText + "," + rightText + "]", format, stream)).size(),
                    "uniqueItems: " + pair[0]);
        }
    }

    @Test
    void parserDistinguishesTheSameOffsetInDifferentInputContexts() throws Exception {
        JsonParser sequence = JsonParserSequence.createFlattened(false,
                JsonMapperFactory.getInstance().createParser("1.25"),
                JsonMapperFactory.getInstance().createParser("2.5"));
        try (JsonParser parser = exactParser(sequence)) {
            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            assertEquals(JsonParser.NumberTypeFP.DOUBLE64, parser.getNumberTypeFP());
            assertEquals(0, parser.currentTokenLocation().getCharOffset());
            assertEquals(1.25d, parser.getDoubleValue());
            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            assertEquals(0, parser.currentTokenLocation().getCharOffset());
            assertEquals(2.5d, parser.getDoubleValue());
        }
    }

    @ParameterizedTest
    @MethodSource("readerModes")
    void numericRepresentationAndPrecisionAreIndependentOfNodeFactory(InputFormat format, boolean located,
            boolean stream) {
        NodeReader reader = located ? NodeReader.builder().locationAware().build() : NodeReader.builder().build();
        for (String text : new String[]{"1.25", "1.1000000000000000000", "0.12345678901234568",
                "1.7976931348623157E308"}) {
            JsonNode node = read(reader, text, format, stream);
            assertTrue(node.isDouble(), text);
            assertEquals(0, new BigDecimal(text).compareTo(node.decimalValue()), text);
            if (located) {
                assertNotNull(JsonNodes.tokenStreamLocationOf(node));
            }
        }
        for (String text : new String[]{"0.0", "-0.0", "9007199254740993.0", "1.0000000000000001",
                "1e-400", "1e400", "100e2147483647"}) {
            JsonNode node = read(reader, text, format, stream);
            assertTrue(node.isBigDecimal(), text);
            assertEquals(0, new BigDecimal(text).compareTo(node.decimalValue()), text);
            if (located) {
                assertNotNull(JsonNodes.tokenStreamLocationOf(node));
            }
        }
        if (format == InputFormat.YAML) {
            for (String text : new String[]{".inf", "-.inf", ".NaN"}) {
                JsonNode expected = YamlMapperFactory.getInstance().readTree(text);
                JsonNode actual = read(reader, text, format, stream);
                assertEquals(expected, actual, text);
                if (expected.isNumber()) {
                    assertEquals(expected.doubleValue(), actual.doubleValue(), text);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("readerModes")
    void callerSuppliedMappersKeepTheirNumericConfiguration(InputFormat format, boolean located, boolean stream) {
        DefaultNodeReader.Builder doubleBuilder = NodeReader.builder()
                .jsonMapper(JsonMapperFactory.getInstance()).yamlMapper(YamlMapperFactory.getInstance());
        DefaultNodeReader.Builder decimalBuilder = NodeReader.builder()
                .jsonMapper(JsonMapper.builder().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                        .disable(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES).build())
                .yamlMapper(YAMLMapper.builder().enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
                        .disable(JsonNodeFeature.STRIP_TRAILING_BIGDECIMAL_ZEROES).build());
        if (located) {
            doubleBuilder.locationAware();
            decimalBuilder.locationAware();
        }
        assertTrue(read(doubleBuilder.build(), "9007199254740993.0", format, stream).isDouble());
        JsonNode decimal = read(decimalBuilder.build(), "1.1000000", format, stream);
        assertTrue(decimal.isBigDecimal());
        assertEquals(new BigDecimal("1.1000000"), decimal.decimalValue());
    }

    @Test
    void publicMappersKeepUntypedDoubles() {
        assertEquals(Double.class, JsonMapperFactory.getInstance().readValue("{\"n\":1.25}", Map.class).get("n").getClass());
        assertEquals(Double.class, YamlMapperFactory.getInstance().readValue("n: 1.25", Map.class).get("n").getClass());
    }

    @Test
    void parserDoubleValueFollowsTokenAdvancementWithoutATypeProbe() throws Exception {
        try (JsonParser parser = exactParser(
                JsonMapperFactory.getInstance().createParser("[1.25,2.5,9007199254740993.0,7,\"3.5\"]"))) {
            assertEquals(JsonToken.START_ARRAY, parser.nextToken());
            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            assertEquals(JsonParser.NumberTypeFP.DOUBLE64, parser.getNumberTypeFP());
            assertEquals(1.25d, parser.getDoubleValue());
            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            assertEquals(2.5d, parser.getDoubleValue());
            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextValue());
            assertEquals(9007199254740992d, parser.getDoubleValue());
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(7d, parser.getDoubleValue());
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertThrows(JacksonException.class, parser::getDoubleValue);
        }
    }

    @ParameterizedTest
    @MethodSource("readerModes")
    void parserTracksNameAndContainerAdvancementWithOrWithoutLocations(InputFormat format, boolean hideLocation,
            boolean stream) throws Exception {
        String input = "{\"a\":1.25,\"skip\":[0],\"b\":2.5,\"nested\":{\"n\":3.5},\"tail\":4.5}";
        ObjectMapper mapper = format == InputFormat.JSON ? JsonMapperFactory.getInstance() : YamlMapperFactory.getInstance();
        JsonParser source = stream
                ? mapper.createParser(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)))
                : mapper.createParser(input);
        if (hideLocation) {
            source = new JsonParserDelegate(source) {
                @Override
                public TokenStreamLocation currentTokenLocation() {
                    return TokenStreamLocation.NA;
                }
            };
        }
        try (JsonParser parser = exactParser(source)) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals("a", parser.nextName());
            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            assertEquals(JsonParser.NumberTypeFP.DOUBLE64, parser.getNumberTypeFP());
            assertEquals(1.25d, parser.getDoubleValue());
            assertEquals("skip", parser.nextName());
            assertEquals(JsonToken.START_ARRAY, parser.nextToken());
            parser.skipChildren();
            assertEquals("b", parser.nextName());
            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            assertEquals(2.5d, parser.getDoubleValue());
            assertEquals(JsonParser.NumberTypeFP.DOUBLE64, parser.getNumberTypeFP());
            assertEquals("nested", parser.nextName());
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals("n", parser.nextName());
            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            assertEquals(3.5d, parser.getDoubleValue());
            assertEquals(JsonParser.NumberTypeFP.DOUBLE64, parser.getNumberTypeFP());
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
            assertEquals("tail", parser.nextName());
            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            assertEquals(4.5d, parser.getDoubleValue());
        }
    }

    @ParameterizedTest
    @EnumSource(value = InputFormat.class, names = {"JSON", "YAML"})
    void machineWrittenDoublesAvoidDecimalParsing(InputFormat format) {
        Random random = new Random(129037);
        double[] numbers = new double[1000];
        StringBuilder input = new StringBuilder("[");
        for (int i = 0; i < numbers.length; i++) {
            double number;
            do {
                number = Double.longBitsToDouble(random.nextLong());
            } while (!Double.isFinite(number) || number == 0d);
            numbers[i] = number;
            if (i > 0) {
                input.append(',');
            }
            input.append(Double.toString(number));
        }
        input.append(']');
        ObjectMapper mapper = format == InputFormat.JSON ? ExactMapperFactory.json() : ExactMapperFactory.yaml();
        AtomicInteger decimalReads = new AtomicInteger();
        try (JsonParser parser = new JsonParserDelegate(mapper.createParser(input.toString())) {
            @Override
            public BigDecimal getDecimalValue() {
                decimalReads.incrementAndGet();
                return super.getDecimalValue();
            }
        }) {
            JsonNode result = mapper.readTree(parser);
            assertEquals(numbers.length, result.size());
            for (int i = 0; i < numbers.length; i++) {
                assertTrue(result.get(i).isDouble(), "number " + i);
                assertEquals(numbers[i], result.get(i).doubleValue(), "number " + i);
            }
            assertEquals(0, decimalReads.get(), "canonical machine doubles need no decimal parse");
        }
    }

    @Test
    void cachedNumberReadsDoNotInspectTokenLocationsOrRescanText() throws Exception {
        AtomicInteger scans = new AtomicInteger();
        AtomicInteger locations = new AtomicInteger();
        try (JsonParser parser = exactParser(new JsonParserDelegate(
                JsonMapperFactory.getInstance().createParser("[1.25,2.5]")) {
            @Override
            public int getStringLength() {
                scans.incrementAndGet();
                return super.getStringLength();
            }

            @Override
            public TokenStreamLocation currentTokenLocation() {
                locations.incrementAndGet();
                return super.currentTokenLocation();
            }
        })) {
            parser.nextToken();
            parser.nextToken();
            assertEquals(JsonParser.NumberTypeFP.DOUBLE64, parser.getNumberTypeFP());
            for (int i = 0; i < 3; i++) {
                assertEquals(1.25d, parser.getDoubleValue());
            }
            parser.nextValue();
            assertEquals(2.5d, parser.getDoubleValue());
            assertEquals(1, scans.get());
            assertEquals(0, locations.get());
        }
    }

    @ParameterizedTest
    @EnumSource(value = InputFormat.class, names = {"JSON", "YAML"})
    void convenienceAdvancementCannotReuseThePreviousNumber(InputFormat format) throws Exception {
        ObjectMapper mapper = format == InputFormat.JSON ? JsonMapperFactory.getInstance() : YamlMapperFactory.getInstance();
        for (int method = 0; method < 9; method++) {
            try (JsonParser parser = exactParser(mapper.createParser("[1.25,2.5]"))) {
                parser.nextToken();
                parser.nextToken();
                parser.getNumberTypeFP();
                switch (method) {
                case 0: parser.nextToken(); break;
                case 1: parser.nextValue(); break;
                case 2: parser.nextName(); break;
                case 3: parser.nextName(new tools.jackson.core.io.SerializedString("unused")); break;
                case 4: parser.nextStringValue(); break;
                case 5: parser.nextIntValue(-1); break;
                case 6: parser.nextLongValue(-1); break;
                case 7: parser.nextBooleanValue(); break;
                default:
                    parser.nextNameMatch(mapper.tokenStreamFactory().constructNameMatcher(
                            java.util.Collections.singletonList(tools.jackson.core.util.Named.fromString("unused")),
                            false));
                    break;
                }
                assertEquals(2.5d, parser.getDoubleValue(), "advancement method " + method);
            }
        }
    }

    private static JsonNode read(NodeReader reader, String text, InputFormat format, boolean stream) {
        return stream ? reader.readTree(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), format)
                : reader.readTree(text, format);
    }

    private static JsonParser exactParser(JsonParser delegate) throws ReflectiveOperationException {
        Constructor<?> constructor = Class.forName(ExactMapperFactory.class.getName() + "$ExactNumberParser")
                .getDeclaredConstructor(JsonParser.class);
        constructor.setAccessible(true);
        return (JsonParser) constructor.newInstance(delegate);
    }

    private static Schema readSchema(SchemaRegistry registry, String text, InputFormat format, boolean stream) {
        return stream ? registry.getSchema(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)), format)
                : registry.getSchema(text, format);
    }
}
