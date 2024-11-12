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

import static com.networknt.schema.TypeFactory.getSchemaNodeType;
import static com.networknt.schema.TypeFactory.getValueNodeType;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.networknt.schema.serialization.JsonMapperFactory;

/**
 * Test for TypeFactory.
 */
class TypeFactoryTest {

    private static final String[] validIntegralValues = { "1", "-1", "0E+1", "0E1", "-0E+1", "-0E1", "10.1E+1",
            "10.1E1", "-10.1E+1", "-10.1E1", "1E+0", "1E-0", "1E0", "1E18", "9223372036854775807",
            "-9223372036854775808", "1.0", "1.00", "-1.0", "-1.00" };

    private static final String[] validNonIntegralNumberValues = { "1.1", "-1.1", "1.10" };

    @Test
    void testIntegralValuesWithJavaSemantics() {
        SchemaValidatorsConfig schemaValidatorsConfig = SchemaValidatorsConfig.builder().javaSemantics(true).build();
        for (String validValue : validIntegralValues) {
            assertSame(JsonType.INTEGER,
                    getValueNodeType(DecimalNode.valueOf(new BigDecimal(validValue)), schemaValidatorsConfig),
                    validValue);
        }
        for (String validValue : validNonIntegralNumberValues) {
            assertSame(JsonType.NUMBER,
                    getValueNodeType(DecimalNode.valueOf(new BigDecimal(validValue)), schemaValidatorsConfig),
                    validValue);
        }
    }

    @Test
    void testIntegralValuesWithoutJavaSemantics() {
        SchemaValidatorsConfig schemaValidatorsConfig = SchemaValidatorsConfig.builder().javaSemantics(false).build();
        for (String validValue : validIntegralValues) {
            assertSame(JsonType.NUMBER,
                    getValueNodeType(DecimalNode.valueOf(new BigDecimal(validValue)), schemaValidatorsConfig),
                    validValue);
        }
        for (String validValue : validNonIntegralNumberValues) {
            assertSame(JsonType.NUMBER,
                    getValueNodeType(DecimalNode.valueOf(new BigDecimal(validValue)), schemaValidatorsConfig),
                    validValue);
        }
    }

    @Test
    void testWithLosslessNarrowing() {
        SchemaValidatorsConfig schemaValidatorsConfig = SchemaValidatorsConfig.builder().losslessNarrowing(true).build();
        for (String validValue : validIntegralValues) {
            assertSame(JsonType.INTEGER,
                    getValueNodeType(DecimalNode.valueOf(new BigDecimal("1.0")), schemaValidatorsConfig), validValue);

            assertSame(JsonType.NUMBER,
                    getValueNodeType(DecimalNode.valueOf(new BigDecimal("1.5")), schemaValidatorsConfig), validValue);
        }
    }

    @Test
    void testWithoutLosslessNarrowing() {
        SchemaValidatorsConfig schemaValidatorsConfig = SchemaValidatorsConfig.builder().losslessNarrowing(false).build();
        for (String validValue : validIntegralValues) {
            assertSame(JsonType.NUMBER,
                    getValueNodeType(DecimalNode.valueOf(new BigDecimal("1.0")), schemaValidatorsConfig), validValue);

            assertSame(JsonType.NUMBER,
                    getValueNodeType(DecimalNode.valueOf(new BigDecimal("1.5")), schemaValidatorsConfig), validValue);
        }

    }

    @Test
    void testObjectValue() {
        assertSame(JsonType.OBJECT, getValueNodeType(JsonMapperFactory.getInstance().getNodeFactory().objectNode(),
                SchemaValidatorsConfig.builder().build()));
    }

    @Test
    void testArrayValue() {
        assertSame(JsonType.ARRAY,
                getValueNodeType(JsonMapperFactory.getInstance().getNodeFactory().arrayNode(), SchemaValidatorsConfig.builder().build()));
    }

    @Test
    void testBooleanValue() {
        assertSame(JsonType.BOOLEAN, getValueNodeType(
                JsonMapperFactory.getInstance().getNodeFactory().booleanNode(true), SchemaValidatorsConfig.builder().build()));
    }

    @Test
    void testNullValue() {
        assertSame(JsonType.NULL,
                getValueNodeType(JsonMapperFactory.getInstance().getNodeFactory().nullNode(), SchemaValidatorsConfig.builder().build()));
    }

    @Test
    void testMissingValue() {
        assertSame(JsonType.UNKNOWN, getValueNodeType(JsonMapperFactory.getInstance().getNodeFactory().missingNode(),
                SchemaValidatorsConfig.builder().build()));
    }

    @Test
    void testIntegerValue() {
        assertSame(JsonType.INTEGER, getValueNodeType(JsonMapperFactory.getInstance().getNodeFactory().numberNode(10),
                SchemaValidatorsConfig.builder().build()));
    }

    @Test
    void testBinaryValue() {
        assertSame(JsonType.STRING, getValueNodeType(
                JsonMapperFactory.getInstance().getNodeFactory().binaryNode("test".getBytes(StandardCharsets.UTF_8)),
                SchemaValidatorsConfig.builder().build()));
    }

    @Test
    void testUnknownSchema() {
        assertSame(JsonType.UNKNOWN, getSchemaNodeType(TextNode.valueOf("unexpected")));
    }

    @Test
    void testMissingSchema() {
        assertSame(JsonType.UNKNOWN, getSchemaNodeType(MissingNode.getInstance()));
    }

    @Test
    void testStringSchema() {
        assertSame(JsonType.STRING, getSchemaNodeType(TextNode.valueOf(JsonType.STRING.toString())));
    }

    @Test
    void testObjectSchema() {
        assertSame(JsonType.OBJECT, getSchemaNodeType(TextNode.valueOf(JsonType.OBJECT.toString())));
    }

    @Test
    void testArraySchema() {
        assertSame(JsonType.ARRAY, getSchemaNodeType(TextNode.valueOf(JsonType.ARRAY.toString())));
    }

    @Test
    void testBooleanSchema() {
        assertSame(JsonType.BOOLEAN, getSchemaNodeType(TextNode.valueOf(JsonType.BOOLEAN.toString())));
    }

    @Test
    void testNumberSchema() {
        assertSame(JsonType.NUMBER, getSchemaNodeType(TextNode.valueOf(JsonType.NUMBER.toString())));
    }

    @Test
    void testIntegerSchema() {
        assertSame(JsonType.INTEGER, getSchemaNodeType(TextNode.valueOf(JsonType.INTEGER.toString())));
    }

    @Test
    void testAnySchema() {
        assertSame(JsonType.ANY, getSchemaNodeType(TextNode.valueOf(JsonType.ANY.toString())));
    }

    @Test
    void testNullSchema() {
        assertSame(JsonType.NULL, getSchemaNodeType(TextNode.valueOf(JsonType.NULL.toString())));
    }

    @Test
    void testUnionSchema() {
        assertSame(JsonType.UNION, getSchemaNodeType(JsonMapperFactory.getInstance().getNodeFactory().arrayNode()));
    }
}
