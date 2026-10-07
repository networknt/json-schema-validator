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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.networknt.schema.keyword.ExclusiveMaximumValidator;
import com.networknt.schema.keyword.ExclusiveMinimumValidator;
import com.networknt.schema.keyword.KeywordValidator;
import com.networknt.schema.keyword.MaximumValidator;
import com.networknt.schema.keyword.MinimumValidator;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.path.PathType;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.DecimalNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.FloatNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

class NumericBoundsReviewTest {
    private SchemaRegistry registry() {
        return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
    }

    @ParameterizedTest
    @CsvSource({
            "maximum, 0.1, 0.1",
            "minimum, -0.1, -0.1",
            "exclusiveMaximum, 0.100000001, 0.1",
            "exclusiveMinimum, -0.100000001, -0.1"
    })
    void floatInstancesUseTheirDecimalText(String keyword, String threshold, float instance) {
        ObjectNode schema = JsonNodeFactory.instance.objectNode();
        schema.set(keyword, DecimalNode.valueOf(new BigDecimal(threshold)));
        assertTrue(registry().getSchema(schema).validate(FloatNode.valueOf(instance)).isEmpty());
    }

    @ParameterizedTest
    @CsvSource({
            "maximum, -0.1, -0.1",
            "minimum, 0.1, 0.1",
            "exclusiveMaximum, -0.1, -0.100000001",
            "exclusiveMinimum, 0.1, 0.100000001"
    })
    void floatThresholdsUseTheirDecimalText(String keyword, float threshold, String instance) {
        ObjectNode schema = JsonNodeFactory.instance.objectNode();
        schema.set(keyword, FloatNode.valueOf(threshold));
        assertTrue(registry().getSchema(schema).validate(DecimalNode.valueOf(new BigDecimal(instance))).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"minimum", "maximum", "exclusiveMinimum", "exclusiveMaximum"})
    void integerThresholdsAlsoUseFloatText(String keyword) {
        for (float value : new float[]{1e18f, -1e18f}) {
            FloatNode instance = FloatNode.valueOf(value);
            ObjectNode schema = JsonNodeFactory.instance.objectNode();
            schema.putArray("type").add("integer").add("number");
            schema.put(keyword, new BigDecimal(instance.asString()).longValueExact());
            assertEquals(keyword.startsWith("exclusive") ? 1 : 0,
                    registry().getSchema(schema).validate(instance).size(), instance.asString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"minimum", "maximum", "exclusiveMinimum", "exclusiveMaximum"})
    void looseExponentOverflowIsComparedMathematically(String keyword) {
        SchemaRegistry loose = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                b -> b.schemaRegistryConfig(SchemaRegistryConfig.builder().typeLoose(true).build()));
        for (boolean integerThreshold : new boolean[]{false, true}) {
            ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
            if (integerThreshold) {
                // Select the integer comparison while allowing numeric strings through type validation.
                schemaNode.putArray("type").add("integer").add("number");
                schemaNode.put(keyword, 0);
            } else {
                schemaNode.put(keyword, 0.0);
            }
            Schema schema = loose.getSchema(schemaNode);
            for (String text : new String[]{"1e2147483648", "1e-2147483649", "1e999999999999999999999"}) {
                assertComparison(schema, keyword, text, 1);
                assertComparison(schema, keyword, "-" + text, -1);
            }
            assertComparison(schema, keyword, "0e2147483648", 0);
            assertComparison(schema, keyword, "-0.00e-999999999999999999999", 0);
        }
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        schemaNode.put(keyword, 1.0);
        assertComparison(loose.getSchema(schemaNode), keyword, "1e-2147483649", -1);
        schemaNode.put(keyword, -1.0);
        assertComparison(loose.getSchema(schemaNode), keyword, "-1e-2147483649", 1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"minimum", "maximum", "exclusiveMinimum", "exclusiveMaximum"})
    void overflowingExponentCanEqualARepresentableThreshold(String keyword) {
        SchemaRegistry loose = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                b -> b.schemaRegistryConfig(SchemaRegistryConfig.builder().typeLoose(true).build()));
        ObjectNode large = JsonNodeFactory.instance.objectNode();
        large.set(keyword, DecimalNode.valueOf(new BigDecimal(BigInteger.ONE, Integer.MIN_VALUE)));
        Schema largeSchema = loose.getSchema(large);
        assertComparison(largeSchema, keyword, "1e2147483648", 0);
        assertComparison(largeSchema, keyword, "1.0e2147483648", 0);
        assertComparison(largeSchema, keyword, "0.9e2147483648", -1);
        assertComparison(largeSchema, keyword, "1.1e2147483648", 1);

        ObjectNode small = JsonNodeFactory.instance.objectNode();
        small.set(keyword, DecimalNode.valueOf(new BigDecimal(BigInteger.ONE, Integer.MAX_VALUE)));
        Schema smallSchema = loose.getSchema(small);
        assertComparison(smallSchema, keyword, "100e-2147483649", 0);
        assertComparison(smallSchema, keyword, "1e-2147483649", -1);
        assertComparison(smallSchema, keyword, "10000e-2147483649", 1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"minimum", "maximum", "exclusiveMinimum", "exclusiveMaximum"})
    void decimalThresholdIsConvertedOnlyOnce(String keyword) {
        AtomicInteger conversions = new AtomicInteger();
        DoubleNode threshold = new DoubleNode(5.25) {
            private static final long serialVersionUID = 1L;

            @Override
            public BigDecimal decimalValue() {
                conversions.incrementAndGet();
                return super.decimalValue();
            }
        };
        Schema parent = registry().getSchema("{}");
        KeywordValidator validator = validator(keyword, threshold, parent);
        assertEquals(1, conversions.get());
        boolean minimum = keyword.equals("minimum") || keyword.equals("exclusiveMinimum");
        JsonNode instance = IntNode.valueOf(minimum ? 6 : 5);
        ExecutionContext context = new ExecutionContext();
        context.evaluationPath = new NodePath(PathType.JSON_POINTER);
        for (int i = 0; i < 5; i++) {
            validator.validate(context, instance, instance, context.evaluationPath);
        }
        assertTrue(context.getErrors().isEmpty());
        assertEquals(1, conversions.get());
    }

    private void assertComparison(Schema schema, String keyword, String instance, int comparison) {
        boolean minimum = keyword.equals("minimum") || keyword.equals("exclusiveMinimum");
        boolean failure = comparison == 0 ? keyword.startsWith("exclusive")
                : minimum ? comparison < 0 : comparison > 0;
        assertEquals(failure ? 1 : 0, schema.validate(StringNode.valueOf(instance)).size(), instance);
    }

    private KeywordValidator validator(String keyword, JsonNode threshold, Schema parent) {
        SchemaLocation location = SchemaLocation.of("#/" + keyword);
        switch (keyword) {
        case "minimum":
            return new MinimumValidator(location, threshold, parent, parent.getSchemaContext());
        case "maximum":
            return new MaximumValidator(location, threshold, parent, parent.getSchemaContext());
        case "exclusiveMinimum":
            return new ExclusiveMinimumValidator(location, threshold, parent, parent.getSchemaContext());
        case "exclusiveMaximum":
            return new ExclusiveMaximumValidator(location, threshold, parent, parent.getSchemaContext());
        default:
            throw new IllegalArgumentException(keyword);
        }
    }
}
