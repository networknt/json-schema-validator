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
import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

class Issue1349Test {
    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;
    private static SchemaRegistry registry(boolean loose) {
        return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemaRegistryConfig(SchemaRegistryConfig.builder().typeLoose(loose).build()));
    }
    private static Schema enumeration(JsonNode value) {
        ObjectNode schema = NODES.objectNode();
        schema.set("enum", NODES.arrayNode().add(value));
        return registry(true).getSchema(schema);
    }
    private static void accepts(Schema schema, String... texts) {
        for (String text : texts) assertTrue(schema.validate(NODES.textNode(text)).isEmpty(), text);
    }
    private static void rejects(Schema schema, String... texts) {
        for (String text : texts) assertFalse(schema.validate(NODES.textNode(text)).isEmpty(), text);
    }

    @Test
    void numericStringsCompareByExactValueAcrossDialects() {
        for (SpecificationVersion version : new SpecificationVersion[] { SpecificationVersion.DRAFT_4,
                SpecificationVersion.DRAFT_6, SpecificationVersion.DRAFT_7,
                SpecificationVersion.DRAFT_2019_09, SpecificationVersion.DRAFT_2020_12 }) {
            SchemaRegistry r = SchemaRegistry.withDefaultDialect(version,
                    b -> b.schemaRegistryConfig(SchemaRegistryConfig.builder().typeLoose(true).build()));
            accepts(r.getSchema("{\"enum\":[100]}"), "100", "100.0", "100.00", "1E+2", "1e2", "0.1e3", "10000e-2");
            accepts(r.getSchema("{\"enum\":[-100]}"), "-100.0", "-1e2", "-0.1e3");
            accepts(r.getSchema("{\"enum\":[0.125]}"), "0.1250", "125e-3", "1.25e-1");
        }
    }

    @Test
    void malformedNumericStringsDoNotMatchNumericMembers() {
        rejects(registry(true).getSchema("{\"enum\":[100,0]}"), "", "+100", "0100", "00", "-00",
                ".0", "100.", "1e", "1e+", "1e-", "--100", " 100", "100 ", "100\n", "1_00", "0x64",
                "NaN", "Infinity", "-Infinity", "1e2x", "١٠٠", "100,0", "100..0");
    }

    @Test
    void strictModeAndLiteralStringMembersKeepTheirSemantics() {
        rejects(registry(false).getSchema("{\"enum\":[100]}"), "100", "100.0", "1e2");
        Schema strings = registry(true).getSchema("{\"enum\":[\"1E+2\",\"001\"]}");
        accepts(strings, "1E+2", "001");
        rejects(strings, "100", "100.0", "1e2", "1");
        assertFalse(strings.validate(NODES.numberNode(100)).isEmpty());
    }

    @Test
    void mixedMembersSkipContainersAndRetainScalarMatching() {
        Schema schema = registry(true).getSchema("{\"enum\":[[],{},[100],{\"n\":100},true,false,\"literal\",100,100.0]}");
        accepts(schema, "true", "false", "literal", "100", "100.0", "1e2");
        rejects(schema, "[]", "{}", "[100]", "{\"n\":100}", "TRUE", "missing");
        assertTrue(schema.validate(NODES.arrayNode().add(100)).isEmpty());
    }

    @Test
    void exactSchemaDecimalsDoNotRoundToDoubles() {
        Schema schema = enumeration(NODES.numberNode(new BigDecimal("9007199254740993.0")));
        accepts(schema, "9007199254740993", "9007199254740993.00", "90071992547409930e-1");
        rejects(schema, "9007199254740992", "9007199254740994", "9007199254740993.0000000000001");
    }

    @Test
    void extremeScalesAndExponentsDoNotExpandZeros() {
        Schema large = enumeration(NODES.numberNode(new BigDecimal(BigInteger.valueOf(100), Integer.MIN_VALUE)));
        accepts(large, "1e2147483650", "100e2147483648", "10e2147483649");
        rejects(large, "1e2147483649", "-1e2147483650");
        Schema small = enumeration(NODES.numberNode(new BigDecimal(BigInteger.ONE, Integer.MAX_VALUE)));
        accepts(small, "1e-2147483647", "10e-2147483648", "0.1e-2147483646");
        rejects(small, "0", "1e-2147483648");
    }

    @Test
    void enormousExponentsAreScannedWithoutBigIntegerParsing() {
        String exponent = new String(new char[100000]).replace('\0', '9');
        Schema zero = enumeration(NODES.numberNode(0));
        accepts(zero, "0e" + exponent, "-0.000e-" + exponent, "0e+" + exponent);
        rejects(zero, "1e-" + exponent, "1e" + exponent, "0e" + exponent + "x");
        Schema one = enumeration(NODES.numberNode(1));
        rejects(one, "1e" + exponent, "1e-" + exponent);
        String zeros = new String(new char[10000]).replace('\0', '0');
        accepts(one, "0." + zeros + "1e10001");
    }
    @Test
    void nonFiniteMembersRetainOnlyTheirExistingTextSpellings() {
        Schema infinity = enumeration(NODES.numberNode(Double.POSITIVE_INFINITY));
        accepts(infinity, "Infinity");
        rejects(infinity, "1e999999999999", "1e400", "NaN", "+Infinity");
        Schema nan = enumeration(NODES.numberNode(Double.NaN));
        accepts(nan, "NaN");
        rejects(nan, "nan", "0", "Infinity");
        Schema zero = enumeration(NODES.numberNode(0));
        accepts(zero, "-0", "0.0", "-0.000", "0e+9999999999999999999999999");
    }

    @Test
    void finiteNumericNodeTypesUseTheirExistingDecimalValue() {
        for (JsonNode value : new JsonNode[] { NODES.numberNode(100), NODES.numberNode(100L),
                NODES.numberNode(100f), NODES.numberNode(100d), NODES.numberNode(BigInteger.valueOf(100)),
                NODES.numberNode(new BigDecimal("100.00")), NODES.numberNode(0.1f), NODES.numberNode(0.1d) }) {
            BigDecimal decimal = value.decimalValue();
            accepts(enumeration(value), decimal.toString(), decimal.toEngineeringString(),
                    decimal.setScale(Math.max(0, decimal.scale()) + 3).toString());
            rejects(enumeration(value), decimal.add(BigDecimal.ONE).toString());
        }
    }

    @Test
    void nonTextNodesClassifiedAsStringsReturnNormalValidationErrors() {
        JsonNode[] instances = { NODES.binaryNode(new byte[] { 1, 2, 3 }),
                new com.fasterxml.jackson.databind.node.POJONode("not numeric") };
        for (String text : new String[] { "{\"enum\":[100]}", "{\"enum\":[1.5]}",
                "{\"enum\":[1E+2]}", "{\"enum\":[null,\"literal\",100]}" }) {
            Schema schema = registry(true).getSchema(text);
            for (JsonNode instance : instances) {
                assertEquals(1, schema.validate(instance).size(), text + ":" + instance.getNodeType());
            }
        }
    }

    @Test
    void schemaReaderPrecisionDefinesTheComparedEnumValue() {
        String schemaText = "{\"enum\":[9007199254740993.0]}";
        Schema rounded = registry(true).getSchema(schemaText);
        accepts(rounded, "9007199254740992");
        rejects(rounded, "9007199254740993");
        com.networknt.schema.serialization.NodeReader exactReader = com.networknt.schema.serialization.NodeReader.builder()
                .jsonMapper(com.fasterxml.jackson.databind.json.JsonMapper.builder()
                        .enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS).build()).build();
        SchemaRegistry exact = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                b -> b.nodeReader(exactReader).schemaRegistryConfig(SchemaRegistryConfig.builder().typeLoose(true).build()));
        accepts(exact.getSchema(schemaText), "9007199254740993");
        rejects(exact.getSchema(schemaText), "9007199254740992");
    }

}
