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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.networknt.schema.keyword.MinLengthValidator;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.path.PathType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class CountLimitsReviewTest {
    private SchemaRegistry registry(boolean loose) {
        return SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemaRegistryConfig(SchemaRegistryConfig.builder().typeLoose(loose).build()));
    }

    private Schema schema(SchemaRegistry registry, String keyword, JsonNode limit) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.set(keyword, limit);
        return registry.getSchema(node);
    }

    static Stream<JsonNode> invalidLimits() {
        return Stream.of(DecimalNode.valueOf(new BigDecimal("1.5")), DoubleNode.valueOf(1.5),
                FloatNode.valueOf(1.5f), DoubleNode.valueOf(Double.NaN),
                DoubleNode.valueOf(Double.POSITIVE_INFINITY), DoubleNode.valueOf(Double.NEGATIVE_INFINITY),
                FloatNode.valueOf(Float.NaN), FloatNode.valueOf(Float.POSITIVE_INFINITY),
                FloatNode.valueOf(Float.NEGATIVE_INFINITY));
    }

    @ParameterizedTest
    @MethodSource("invalidLimits")
    void nonIntegralAndNonFiniteCountsUseUnrestrictiveDefaults(JsonNode limit) throws Exception {
        SchemaRegistry registry = registry(false);
        for (String suffix : new String[] { "Items", "Length", "Properties" }) {
            String instance = suffix.equals("Items") ? "[1]"
                    : suffix.equals("Length") ? "\"a\"" : "{\"a\":1}";
            assertTrue(schema(registry, "max" + suffix, limit).validate(instance, InputFormat.JSON).isEmpty(), suffix);
            assertTrue(schema(registry, "min" + suffix, limit).validate(instance, InputFormat.JSON).isEmpty(), suffix);
        }
        assertTrue(schema(registry(true), "maxItems", limit).validate("1", InputFormat.JSON).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "Items", "Length", "Properties" })
    void saturatedMaximaKeepTheOriginalLimitInErrors(String suffix) throws Exception {
        SchemaRegistry registry = registry(false);
        String instance = suffix.equals("Items") ? "[1]"
                : suffix.equals("Length") ? "\"a\"" : "{\"a\":1}";
        JsonNode[] limits = { LongNode.valueOf(-2147483649L), DoubleNode.valueOf(-2147483649d),
                DecimalNode.valueOf(new BigDecimal("-2147483649.0")),
                DecimalNode.valueOf(new BigDecimal("-1e400")) };
        for (JsonNode limit : limits) {
            List<Error> errors = schema(registry, "max" + suffix, limit).validate(instance, InputFormat.JSON);
            assertEquals(1, errors.size());
            assertEquals(limit.asText(), errors.get(0).getArguments()[0]);
            assertTrue(errors.get(0).getMessage().contains(limit.asText()));
        }
    }

    @Test
    void saturatedMaxItemsKeepsTheOriginalLimitForLooseScalars() throws Exception {
        JsonNode limit = LongNode.valueOf(-2147483649L);
        List<Error> errors = schema(registry(true), "maxItems", limit).validate("1", InputFormat.JSON);
        assertEquals(1, errors.size());
        assertEquals(limit.asText(), errors.get(0).getArguments()[0]);
    }

    @Test
    void minLengthStillToleratesANullSchemaNode() throws Exception {
        Schema parent = registry(false).getSchema("{}");
        MinLengthValidator validator = assertDoesNotThrow(() -> new MinLengthValidator(
                SchemaLocation.of("#/minLength"), null, parent, parent.getSchemaContext()));
        ExecutionContext context = new ExecutionContext();
        context.evaluationPath = new NodePath(PathType.JSON_POINTER);
        JsonNode instance = JsonNodeFactory.instance.textNode("");
        validator.validate(context, instance, instance, context.evaluationPath);
        assertTrue(context.getErrors().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "Items", "Properties" })
    void minimumAboveIntRangeStillRejectsTheLargestIntCount(String suffix) throws Exception {
        // Exercise the boundary without allocating billions of entries.
        JsonNode instance = suffix.equals("Items") ? new ArrayNode(JsonNodeFactory.instance) {
            @Override
            public int size() {
                return Integer.MAX_VALUE;
            }
        } : new ObjectNode(JsonNodeFactory.instance) {
            @Override
            public int size() {
                return Integer.MAX_VALUE;
            }
        };
        SchemaRegistry registry = registry(false);
        assertTrue(registry.getSchema("{\"min" + suffix + "\":2147483647}").validate(instance).isEmpty());
        List<Error> errors = registry.getSchema("{\"min" + suffix + "\":2147483648}").validate(instance);
        assertEquals(1, errors.size());
        assertEquals("2147483648", errors.get(0).getArguments()[0]);
        assertTrue(registry.getSchema("{\"max" + suffix + "\":2147483647}").validate(instance).isEmpty());
        assertTrue(registry.getSchema("{\"max" + suffix + "\":2147483648}").validate(instance).isEmpty());
    }
}
