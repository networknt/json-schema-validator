/*
 * Copyright (c) 2024 the original author or authors.
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Test MultipleOfValidator validator.
 */
class MultipleOfValidatorTest {
    String schemaData = "{" +
            "  \"type\": \"object\"," +
            "  \"properties\": {" +
            "    \"value1\": {" +
            "      \"type\": \"number\"," +
            "      \"multipleOf\": 0.01" +
            "    }," +
            "    \"value2\": {" +
            "      \"type\": \"number\"," +
            "      \"multipleOf\": 0.01" +
            "    }," +
            "    \"value3\": {" +
            "      \"type\": \"number\"," +
            "      \"multipleOf\": 0.01" +
            "    }" +
            "  }" +
            "}";

    @Test
    void test() {
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        String inputData = "{\"value1\":123.892,\"value2\":123456.2934,\"value3\":123.123}";
        String validData = "{\"value1\":123.89,\"value2\":123456,\"value3\":123.010}";
        
        List<Error> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(3, messages.size());
        assertEquals(3, messages.stream().filter(m -> "multipleOf".equals(m.getKeyword())).count());
        
        messages = schema.validate(validData, InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void testTypeLoose() {
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        
        String inputData = "{\"value1\":\"123.892\",\"value2\":\"123456.2934\",\"value3\":123.123}";
        String validTypeLooseInputData = "{\"value1\":\"123.89\",\"value2\":\"123456.29\",\"value3\":123.12}";
        
        // Without type loose this has 2 type and 1 multipleOf errors
        List<Error> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(3, messages.size());
        assertEquals(2, messages.stream().filter(m -> "type".equals(m.getKeyword())).count());
        assertEquals(1, messages.stream().filter(m -> "multipleOf".equals(m.getKeyword())).count());
        
        // 2 type errors
        messages = schema.validate(validTypeLooseInputData, InputFormat.JSON);
        assertEquals(2, messages.size());
        assertEquals(2, messages.stream().filter(m -> "type".equals(m.getKeyword())).count());
        
        // With type loose this has 3 multipleOf errors
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().typeLoose(true).build();
        factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12, builder -> builder.schemaRegistryConfig(config));
        Schema typeLoose = factory.getSchema(schemaData);
        messages = typeLoose.validate(inputData, InputFormat.JSON);
        assertEquals(3, messages.size());
        assertEquals(3, messages.stream().filter(m -> "multipleOf".equals(m.getKeyword())).count());
        
        // No errors
        messages = typeLoose.validate(validTypeLooseInputData, InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void messageFormatPrecision() {
        String schemaData = "{ \"type\": \"object\", \"properties\": { \"value1\": { \"type\": \"number\", \"multipleOf\": 0.00001 } } }";
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = factory.getSchema(schemaData);
        String inputData = "{\"value1\":123.000001}";

        List<Error> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals("must be multiple of 0.00001", messages.get(0).getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "2, 9007199254740993, 1",
            "3, 9007199254740993, 0",
            "2, -9007199254740993, 1",
            "2, 9223372036854775809, 1",
            "2, 9007199254740992, 0",
            "2, 9007199254740994, 0",
            "2, 9223372036854775810, 0",
            "9007199254740993, 9007199254740992, 1",
            "9007199254740993, 9007199254740993, 0",
            "9007199254740993, 18014398509481986, 0",
            "9223372036854775809, 9223372036854775808, 1",
            "9223372036854775809, 9223372036854775809, 0"
    })
    void largeIntegers(String divisor, String dividend, int expectedErrors) {
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema("{\"multipleOf\":" + divisor + "}");
        assertEquals(expectedErrors, schema.validate(dividend, InputFormat.JSON).size());
    }

    @ParameterizedTest
    @CsvSource({
            "0.1, 0.3, 0",
            "0.1, 0.31, 1",
            "1e-400, 1.5e-400, 1",
            "1e-400, 2e-400, 0",
            "1e400, 1, 1",
            "1e400, 2e400, 0",
            "2, 1e400, 0",
            "0, 1, 0"
    })
    void exactDecimals(String divisor, String dividend, int expectedErrors) {
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        schemaNode.set("multipleOf", DecimalNode.valueOf(new BigDecimal(divisor)));
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12)
                .getSchema(schemaNode);
        assertEquals(expectedErrors, schema.validate(DecimalNode.valueOf(new BigDecimal(dividend))).size());
    }

    @Test
    void integersBeyondDoubleRange() {
        BigInteger divisor = BigInteger.TEN.pow(400).add(BigInteger.ONE);
        ObjectNode schemaNode = JsonNodeFactory.instance.objectNode();
        schemaNode.set("multipleOf", BigIntegerNode.valueOf(divisor));
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        Schema schema = registry.getSchema(schemaNode);
        assertEquals(1, schema.validate(BigIntegerNode.valueOf(divisor.subtract(BigInteger.ONE))).size());
        assertEquals(0, schema.validate(BigIntegerNode.valueOf(divisor)).size());
        assertEquals(0, schema.validate(BigIntegerNode.valueOf(divisor.multiply(BigInteger.valueOf(2)))).size());
        assertEquals(0, schema.validate(BigIntegerNode.valueOf(divisor.negate())).size());

        Schema even = registry.getSchema("{\"multipleOf\":2}");
        assertEquals(1, even.validate(BigIntegerNode.valueOf(divisor)).size());
        assertEquals(1, even.validate(BigIntegerNode.valueOf(divisor.negate())).size());
        assertEquals(0, even.validate(BigIntegerNode.valueOf(divisor.add(BigInteger.ONE))).size());
    }
}
