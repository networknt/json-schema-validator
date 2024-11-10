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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * UnevaluatedItemsValidatorTest.
 */
class UnevaluatedItemsValidatorTest {
    @Test
    void unevaluatedItemsFalse() {
        String schemaData = "{\r\n"
                + "  \"oneOf\": [\r\n"
                + "    { \r\n"
                + "      \"type\" : \"array\" ,\r\n"
                + "      \"prefixItems\" : [\r\n"
                + "        { \"type\" : \"integer\" }\r\n"
                + "      ]\r\n"
                + "    }\r\n"
                + "  ],\r\n"
                + "  \"unevaluatedItems\" : false\r\n"
                + "}";
        String inputData = "[1,2,3]";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData);
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(2, messages.size());
        List<ValidationMessage> assertions = messages.stream().collect(Collectors.toList());
        assertEquals("unevaluatedItems", assertions.get(0).getType());
        assertEquals("$", assertions.get(0).getInstanceLocation().toString());
        assertEquals("$.unevaluatedItems", assertions.get(0).getEvaluationPath().toString());
        assertEquals("unevaluatedItems", assertions.get(1).getType());
        assertEquals("$", assertions.get(1).getInstanceLocation().toString());
        assertEquals("$.unevaluatedItems", assertions.get(1).getEvaluationPath().toString());
    }

    @Test
    void unevaluatedItemsSchema() {
        String schemaData = "{\r\n"
                + "  \"oneOf\": [\r\n"
                + "    { \r\n"
                + "      \"type\" : \"array\" ,\r\n"
                + "      \"prefixItems\" : [\r\n"
                + "        { \"type\" : \"integer\" }\r\n"
                + "      ]\r\n"
                + "    }\r\n"
                + "  ],\r\n"
                + "  \"unevaluatedItems\" : { \"type\" : \"string\" }\r\n"
                + "}";
        String inputData = "[1,2,3]";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData);
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(2, messages.size());
        List<ValidationMessage> assertions = messages.stream().collect(Collectors.toList());
        assertEquals("type", assertions.get(0).getType());
        assertEquals("$[1]", assertions.get(0).getInstanceLocation().toString());
        assertEquals("$.unevaluatedItems.type", assertions.get(0).getEvaluationPath().toString());
        assertEquals("type", assertions.get(1).getType());
        assertEquals("$[2]", assertions.get(1).getInstanceLocation().toString());
        assertEquals("$.unevaluatedItems.type", assertions.get(1).getEvaluationPath().toString());
    }
}
