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

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.networknt.schema.InputFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.JsonParserDelegate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/** Checked-in classification oracle and allocation-sensitive fast-path boundaries. */
@Tag("jackson-upgrade")
class Issue1368Test {
    enum Format {
        JSON, YAML;

        ObjectMapper selective() {
            return this == JSON ? JsonMapperFactory.getInstance() : YamlMapperFactory.getInstance();
        }

        ObjectMapper stock() {
            return this == JSON ? JsonMapper.builder().build() : YAMLMapper.builder().build();
        }
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void fixedPointDigitCharacterAndExponentBoundaries(Format format) throws Exception {
        // Include equivalent but noncanonical spellings: the value alone does not pin shortcut selection.
        String[] lossless = { "12345678901234.5", "1234567890123.40", "-1234567890123.40",
                "12345678901234.50", "0.123456789012345", "1.234567890123456", "1.2345678901234567",
                "0.10000000000000000000", "-0.0", "5.0", "1.25e+2", "1.2345E7", "1e-300" };
        String[] lossy = { "9007199254740993.0", "-9007199254740993.0", "1.23456789012345678",
                "1.00000000000000001e0", "1e-400", "-1e-400" };
        for (String token : lossless) assertClassification(format, token, token, false);
        for (String token : lossy) assertClassification(format, token, token, true);
        assertEquals(16, "1234567890123.40".length());
        assertEquals(17, "-1234567890123.40".length());
    }

    @Test
    void yamlNumericSpellingsRetainTheirValuesAndTypes() throws Exception {
        String[][] lossless = { { "+123.25", "123.25" }, { ".5", "0.5" }, { "5.", "5.0" },
                { "1_000.25", "1000.25" }, { "!!float 123456789012345", "123456789012345" },
                { "!!float -123456789012345", "-123456789012345" }, { "!!float +123.25", "123.25" },
                { "!!float .5", "0.5" }, { "!!float 1_000.25", "1000.25" }, { "!!float 5.", "5.0" } };
        String[][] lossy = { { "9_007_199_254_740_993.0", "9007199254740993.0" },
                { "!!float 9007199254740993", "9007199254740993" },
                { "!!float 9_007_199_254_740_993.0", "9007199254740993.0" },
                { "!!float +9007199254740993", "9007199254740993" } };
        for (String[] token : lossless) assertClassification(Format.YAML, token[0], token[1], false);
        for (String[] token : lossy) assertClassification(Format.YAML, token[0], token[1], true);
    }

    @Test
    void yamlOnlySpellingsRemainInvalidJson() {
        for (String token : new String[] { "+123.25", ".5", "5.", "1_000.25", "!!float 123.25" }) {
            assertThrows(JsonProcessingException.class, () -> Format.JSON.selective().readTree(token), token);
        }
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void fixedSeedClassificationMatchesIndependentDecimalOracle(Format format) throws Exception {
        Random random = new Random(1368);
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < 4096; i++) {
            String token;
            switch (i % 6) {
            case 0:
                token = BigDecimal.valueOf(random.nextLong() % 1000000000000000L,
                        1 + random.nextInt(14)).toPlainString();
                break;
            case 1:
                token = Double.toString(Math.scalb(random.nextDouble() - 0.5, random.nextInt(1800) - 900));
                break;
            case 2:
                StringBuilder digits = new StringBuilder().append(1 + random.nextInt(9));
                int length = 16 + random.nextInt(25);
                for (int j = 1; j < length; j++) digits.append(random.nextInt(10));
                digits.insert(1 + random.nextInt(length - 1), '.');
                token = (random.nextBoolean() ? "-" : "") + digits;
                break;
            case 3:
                token = "1." + Math.abs(random.nextLong() % 1000000000000000L) + "e-" + (400 + random.nextInt(100));
                break;
            case 4:
                token = (9007199254740990L + random.nextInt(16)) + ".0";
                break;
            default:
                token = BigDecimal.valueOf(random.nextLong(), random.nextInt(500) - 250).toString();
                break;
            }
            if (token.indexOf('.') < 0 && token.indexOf('E') < 0 && token.indexOf('e') < 0) token += ".0";
            tokens.add(token);
        }
        String input = "[" + String.join(",", tokens) + "]";
        JsonNode[] trees = { format.selective().readTree(input),
                NodeReader.builder().build().readTree(input, InputFormat.valueOf(format.name())) };
        for (JsonNode tree : trees) {
            assertEquals(tokens.size(), tree.size());
            for (int i = 0; i < tokens.size(); i++) {
                String token = tokens.get(i);
                double number = Double.parseDouble(token);
                assertTrue(Double.isFinite(number), token);
                BigDecimal decimal = new BigDecimal(token);
                boolean lossy = decimal.compareTo(BigDecimal.valueOf(number)) != 0;
                assertValue(tree.get(i), decimal, number, lossy, format + ": " + token);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(Format.class)
    void shortcutAndShortestComparisonAvoidRepeatedTextMaterialization(Format format) throws Exception {
        // 16 vs 17 chars: widening the conservative limit is numerically safe for this
        // negative 15-digit value, so also pin whether the fallback reads its full text.
        assertAccesses(format, "1234567890123.40", 1, 0);
        assertAccesses(format, "-1234567890123.40", 0, 1);
        // A canonical exponent fails the fixed-point shortcut, then reuses its characters.
        assertAccesses(format, "1.2345E7", 1, 0);
        // Long canonical machine doubles skip the shortcut and still avoid token Strings.
        assertAccesses(format, "1.234567890123456", 1, 0);
    }

    private static void assertClassification(Format format, String input, String normalized, boolean lossy) throws Exception {
        BigDecimal decimal = new BigDecimal(normalized);
        double number = Double.parseDouble(normalized);
        JsonNode factoryTree = format.selective().readTree(input);
        JsonNode readerTree = NodeReader.builder().build().readTree(input, InputFormat.valueOf(format.name()));
        assertValue(factoryTree, decimal, number, lossy, format + ": " + input);
        assertValue(readerTree, decimal, number, lossy, format + ": " + input);
    }

    private static void assertValue(JsonNode node, BigDecimal decimal, double number, boolean lossy, String label) {
        assertEquals(lossy ? DecimalNode.class : DoubleNode.class, node.getClass(), label);
        if (lossy) {
            assertEquals(0, decimal.compareTo(node.decimalValue()), label);
        } else {
            assertEquals(Double.doubleToRawLongBits(number), Double.doubleToRawLongBits(node.doubleValue()), label);
        }
    }

    private static void assertAccesses(Format format, String token, int characters, int text) throws Exception {
        ObjectMapper mapper = format.stock();
        // Put numbers inside an object so JSON exercises a nonzero text-buffer offset.
        CountingParser source = new CountingParser(mapper.getFactory().createParser("{\"value\": " + token + "}"));
        try (JsonParser parser = SchemaNodeReader.precisionParser(source, false)) {
            parser.nextToken();
            parser.nextToken();
            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
            parser.getNumberType();
            parser.getNumberTypeFP();
            JsonNode node = mapper.reader().without(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_TRAILING_TOKENS).readTree(parser);
            assertValue(node, new BigDecimal(token), Double.parseDouble(token), false, token);
            assertEquals(characters, source.characterReads, format + ": " + token);
            assertEquals(text, source.textReads, format + ": " + token);
        }
    }

    private static class CountingParser extends JsonParserDelegate {
        int characterReads;
        int textReads;

        CountingParser(JsonParser parser) { super(parser); }

        @Override
        public char[] getTextCharacters() throws java.io.IOException {
            characterReads++;
            return super.getTextCharacters();
        }

        @Override
        public String getText() throws java.io.IOException {
            textReads++;
            return super.getText();
        }
    }
}
