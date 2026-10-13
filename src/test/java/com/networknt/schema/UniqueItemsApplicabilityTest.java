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
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests the instance types to which uniqueItems applies.
 *
 * @author ParkJaeHong
 */
class UniqueItemsApplicabilityTest {
    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void ignoresNonArrays(SpecificationVersion version) {
        Schema schema = SchemaRegistry.withDefaultDialect(version).getSchema("{\"uniqueItems\":true}");
        for (String instance : new String[] {
                "{\"a\":\"x\",\"b\":\"x\"}",
                "{\"a\":1,\"b\":1.0}",
                "{\"a\":[1],\"b\":[1]}",
                "{\"a\":{\"x\":1},\"b\":{\"x\":1}}",
                "{}",
                "1",
                "true",
                "null",
                "\"text\"" }) {
            assertEquals(0, schema.validate(instance, InputFormat.JSON).size(), instance);
        }
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void ignoresRepeatedObjectValuesInTypeLooseMode(SpecificationVersion version) {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().typeLoose(true).build();
        Schema schema = SchemaRegistry.withDefaultDialect(version, builder -> builder.schemaRegistryConfig(config))
                .getSchema("{\"type\":\"array\",\"uniqueItems\":true}");
        assertEquals(0, schema.validate("{\"a\":\"x\",\"b\":\"x\"}", InputFormat.JSON).size());
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void stillChecksArrayElements(SpecificationVersion version) {
        Schema schema = SchemaRegistry.withDefaultDialect(version).getSchema("{\"uniqueItems\":true}");
        for (String instance : new String[] {
                "[\"x\",\"x\"]",
                "[1,1.0]",
                "[[1],[1]]",
                "[{\"a\":1},{\"a\":1}]" }) {
            assertEquals(1, schema.validate(instance, InputFormat.JSON).size(), instance);
        }
        assertEquals(0, schema.validate("[1,2]", InputFormat.JSON).size());
        assertEquals(0, schema.validate("[]", InputFormat.JSON).size());
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void falseAllowsDuplicateArrayElements(SpecificationVersion version) {
        Schema schema = SchemaRegistry.withDefaultDialect(version).getSchema("{\"uniqueItems\":false}");
        assertEquals(0, schema.validate("[1,1]", InputFormat.JSON).size());
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void explicitArrayTypeStillRejectsObjects(SpecificationVersion version) {
        Schema schema = SchemaRegistry.withDefaultDialect(version)
                .getSchema("{\"type\":\"array\",\"uniqueItems\":true}");
        List<Error> errors = schema.validate("{\"a\":\"x\",\"b\":\"x\"}", InputFormat.JSON);
        assertEquals(1, errors.size());
        assertEquals("type", errors.get(0).getKeyword());
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void checksArrayPropertiesButIgnoresObjectProperties(SpecificationVersion version) {
        Schema schema = SchemaRegistry.withDefaultDialect(version)
                .getSchema("{\"properties\":{\"value\":{\"uniqueItems\":true}}}");
        assertEquals(0, schema.validate("{\"value\":{\"a\":\"x\",\"b\":\"x\"}}", InputFormat.JSON).size());
        List<Error> errors = schema.validate("{\"value\":[\"x\",\"x\"]}", InputFormat.JSON);
        assertEquals(1, errors.size());
        assertEquals("uniqueItems", errors.get(0).getKeyword());
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void rejectsObjectWhenUniqueItemsIsNegated(SpecificationVersion version) {
        Schema schema = SchemaRegistry.withDefaultDialect(version).getSchema("{\"not\":{\"uniqueItems\":true}}");
        List<Error> errors = schema.validate("{\"a\":1,\"b\":1}", InputFormat.JSON);
        assertEquals(1, errors.size());
        assertEquals("not", errors.get(0).getKeyword());
        assertEquals(0, schema.validate("[1,1]", InputFormat.JSON).size());
        assertEquals(1, schema.validate("[1,2]", InputFormat.JSON).size());
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void oneOfAcceptsNonArraysWhenOnlyUniqueItemsMatches(SpecificationVersion version) {
        Schema schema = SchemaRegistry.withDefaultDialect(version)
                .getSchema("{\"oneOf\":[{\"uniqueItems\":true},{\"type\":\"string\"}]}");
        assertEquals(0, schema.validate("{\"a\":1,\"b\":1}", InputFormat.JSON).size());
        assertEquals(0, schema.validate("[1,2]", InputFormat.JSON).size());
        assertFalse(schema.validate("[1,1]", InputFormat.JSON).isEmpty());
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void oneOfRejectsNonArraysWhenBothBranchesMatch(SpecificationVersion version) {
        Schema schema = SchemaRegistry.withDefaultDialect(version)
                .getSchema("{\"oneOf\":[{\"uniqueItems\":true},{\"type\":\"object\"}]}");
        List<Error> errors = schema.validate("{\"a\":1,\"b\":1}", InputFormat.JSON);
        assertEquals(1, errors.size());
        assertEquals("oneOf", errors.get(0).getKeyword());
    }

    @ParameterizedTest
    @EnumSource(SpecificationVersion.class)
    void walkRejectsObjectWhenUniqueItemsIsNegated(SpecificationVersion version) {
        Schema schema = SchemaRegistry.withDefaultDialect(version).getSchema("{\"not\":{\"uniqueItems\":true}}");
        Result result = schema.walk("{\"a\":1,\"b\":1}", InputFormat.JSON, true);
        assertEquals(1, result.getErrors().size());
        assertEquals("not", result.getErrors().get(0).getKeyword());
        assertEquals(0, schema.walk("[1,1]", InputFormat.JSON, true).getErrors().size());
    }
}
