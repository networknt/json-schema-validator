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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.DecimalNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.FloatNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

class NumericFollowUpTest {
    @ParameterizedTest
    @CsvSource({"0.3, 0.1, 0", "-0.3, 0.1, 0", "0.31, 0.1, 1", "0.0, 0.1, 0",
            "1.0, 0.1, 0", "0.3, 0.2, 1", "1.4E-45, 1.4E-45, 0", "3.4028235E38, 0.1, 0"})
    void multipleOfUsesFloatTextForBothOperands(float dividend, float divisor, int errors) {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        JsonNode floatDividend = FloatNode.valueOf(dividend);
        JsonNode decimalDividend = DecimalNode.valueOf(new BigDecimal(Float.toString(dividend)));
        for (JsonNode divisorNode : new JsonNode[]{FloatNode.valueOf(divisor),
                DecimalNode.valueOf(new BigDecimal(Float.toString(divisor)))}) {
            ObjectNode document = JsonNodeFactory.instance.objectNode();
            document.set("multipleOf", divisorNode);
            Schema schema = registry.getSchema(document);
            assertEquals(errors, schema.validate(floatDividend).size(), "float dividend: " + divisorNode);
            assertEquals(errors, schema.validate(decimalDividend).size(), "decimal dividend: " + divisorNode);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"minimum", "maximum", "exclusiveMinimum", "exclusiveMaximum"})
    void invalidNonFiniteBoundsFailDuringInitialization(String keyword) {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        for (JsonNode bound : new JsonNode[]{DoubleNode.valueOf(Double.NaN), FloatNode.valueOf(Float.NaN),
                FloatNode.valueOf(Float.POSITIVE_INFINITY), FloatNode.valueOf(Float.NEGATIVE_INFINITY)}) {
            ObjectNode document = JsonNodeFactory.instance.objectNode();
            document.set(keyword, bound);
            SchemaException error = assertThrows(SchemaException.class,
                    () -> registry.getSchema(document).initializeValidators());
            assertEquals(keyword + " value must be finite", error.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"minimum", "maximum", "exclusiveMinimum", "exclusiveMaximum"})
    void doubleInfinityBoundsKeepTheirExistingBehavior(String keyword) {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        for (double bound : new double[]{Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            ObjectNode document = JsonNodeFactory.instance.objectNode();
            document.put(keyword, bound);
            Schema schema = registry.getSchema(document);
            schema.initializeValidators();
            boolean minimum = keyword.equals("minimum") || keyword.equals("exclusiveMinimum");
            int errors = minimum == (bound > 0) ? 1 : 0;
            assertEquals(errors, schema.validate(IntNode.valueOf(0)).size());
        }
    }
}
