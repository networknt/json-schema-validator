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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.networknt.schema.keyword.MultipleOfValidator;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.path.PathType;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.DecimalNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

class MultipleOfExponentReviewTest {
    private SchemaRegistry looseRegistry() {
        return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemaRegistryConfig(SchemaRegistryConfig.builder().typeLoose(true).build()));
    }

    @ParameterizedTest
    @CsvSource({
            "2, 1e2147483648, 0",
            "3, 3e2147483648, 0",
            "3, -3e2147483648, 0",
            "3, 1e2147483648, 1",
            "12, 3e2147483648, 0",
            "25, 1e2147483648, 0",
            "0.2, 1e2147483648, 0",
            "3, 3e999999999999999999999999, 0",
            "9, 3e999999999999999999999999, 1",
            "3, 0e2147483648, 0",
            "3, -0.00e-999999999999999999999999, 0",
            "3, 1e-2147483649, 1"
    })
    void looseNumbersAreCheckedBeyondTheBigDecimalExponentRange(String divisor, String dividend,
            int expectedErrors) {
        Schema schema = looseRegistry().getSchema("{\"multipleOf\":" + divisor + "}");
        for (InputFormat format : new InputFormat[]{InputFormat.JSON, InputFormat.YAML}) {
            assertEquals(expectedErrors, schema.validate("\"" + dividend + "\"", format).size());
        }
    }

    @Test
    void coefficientAndExponentCanCancelAtScaleLimits() {
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        schemaNode.set("multipleOf", DecimalNode.valueOf(new BigDecimal(BigInteger.ONE, Integer.MIN_VALUE)));
        Schema hugeDivisor = looseRegistry().getSchema(schemaNode);
        assertTrue(hugeDivisor.validate(StringNode.valueOf("1e2147483648")).isEmpty());
        assertTrue(hugeDivisor.validate(StringNode.valueOf("1.0e2147483648")).isEmpty());
        assertEquals(1, hugeDivisor.validate(StringNode.valueOf("0.1e2147483648")).size());

        schemaNode.set("multipleOf", DecimalNode.valueOf(new BigDecimal(BigInteger.ONE, Integer.MAX_VALUE)));
        Schema tinyDivisor = looseRegistry().getSchema(schemaNode);
        assertTrue(tinyDivisor.validate(StringNode.valueOf("100e-2147483649")).isEmpty());
        assertTrue(tinyDivisor.validate(StringNode.valueOf("100.0e-2147483649")).isEmpty());
        assertEquals(1, tinyDivisor.validate(StringNode.valueOf("10e-2147483649")).size());
    }

    @Test
    void longExponentsDoNotExpandPowers() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            String exponent = "9".repeat(2000);
            Schema sixes = looseRegistry().getSchema("{\"multipleOf\":6}");
            assertTrue(sixes.validate(StringNode.valueOf("3e" + exponent)).isEmpty());
            assertEquals(1, sixes.validate(StringNode.valueOf("1e" + exponent)).size());
            assertEquals(1, sixes.validate(StringNode.valueOf("3e-" + exponent)).size());
            assertTrue(sixes.validate(StringNode.valueOf("0e-" + exponent)).isEmpty());
        });
    }

    @Test
    void conversionHookRetainsItsOwnOverflowingInput() {
        Schema parent = looseRegistry().getSchema("{\"multipleOf\":3}");
        MultipleOfValidator validator = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                IntNode.valueOf(3), parent, parent.getSchemaContext()) {
            @Override
            protected BigDecimal getDividend(JsonNode node) {
                return super.getDividend(StringNode.valueOf("3e2147483648"));
            }
        };
        ExecutionContext context = new ExecutionContext();
        context.evaluationPath = new NodePath(PathType.JSON_POINTER);
        JsonNode instance = IntNode.valueOf(2);
        validator.validate(context, instance, instance, context.evaluationPath);
        assertTrue(context.getErrors().isEmpty());
    }

    @Test
    void unrelatedConversionHookExceptionsStillPropagate() {
        Schema parent = looseRegistry().getSchema("{\"multipleOf\":3}");
        MultipleOfValidator validator = new MultipleOfValidator(SchemaLocation.of("#/multipleOf"),
                IntNode.valueOf(3), parent, parent.getSchemaContext()) {
            @Override
            protected BigDecimal getDividend(JsonNode node) {
                throw new NumberFormatException("custom conversion failed");
            }
        };
        ExecutionContext context = new ExecutionContext();
        context.evaluationPath = new NodePath(PathType.JSON_POINTER);
        JsonNode instance = IntNode.valueOf(2);
        NumberFormatException exception = assertThrows(NumberFormatException.class,
                () -> validator.validate(context, instance, instance, context.evaluationPath));
        assertEquals("custom conversion failed", exception.getMessage());
    }
}
