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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * EnumValidator test. 
 */
class EnumValidatorTest {

    @Test
    void enumWithObjectNodes() {
        String schemaData = "{\r\n"
                + "    \"title\": \"Severity\",\r\n"
                + "    \"type\": \"object\",\r\n"
                + "    \"properties\": {\r\n"
                + "        \"name\": {\r\n"
                + "            \"title\": \"Name\",\r\n"
                + "            \"description\": \"The human readable name of the severity\",\r\n"
                + "            \"type\": \"string\"\r\n"
                + "        },\r\n"
                + "        \"cardinality\": {\r\n"
                + "            \"title\": \"Cardinality\",\r\n"
                + "            \"description\": \"The severities cardinality, the higher the worse it gets\",\r\n"
                + "            \"type\": \"integer\",\r\n"
                + "            \"minimum\": 0,\r\n"
                + "            \"maximum\": 50,\r\n"
                + "            \"multipleOf\": 10\r\n"
                + "        }\r\n"
                + "    },\r\n"
                + "    \"additionalProperties\": false,\r\n"
                + "    \"required\": [\r\n"
                + "        \"name\",\r\n"
                + "        \"cardinality\"\r\n"
                + "    ],\r\n"
                + "    \"enum\": [\r\n"
                + "        {\r\n"
                + "            \"name\": \"EMPTY\",\r\n"
                + "            \"cardinality\": 0\r\n"
                + "        },\r\n"
                + "        {\r\n"
                + "            \"name\": \"OK\",\r\n"
                + "            \"cardinality\": 20\r\n"
                + "        },\r\n"
                + "        {\r\n"
                + "            \"name\": \"UNKNOWN\",\r\n"
                + "            \"cardinality\": 30\r\n"
                + "        },\r\n"
                + "        {\r\n"
                + "            \"name\": \"WARNING\",\r\n"
                + "            \"cardinality\": 40\r\n"
                + "        },\r\n"
                + "        {\r\n"
                + "            \"name\": \"CRITICAL\",\r\n"
                + "            \"cardinality\": 50\r\n"
                + "        }\r\n"
                + "    ],\r\n"
                + "    \"default\": {\r\n"
                + "        \"name\": \"UNKNOWN\",\r\n"
                + "        \"cardinality\": 30\r\n"
                + "    }\r\n"
                + "}";
        String inputData = "{\r\n"
                + "   \"name\": \"FOO\",\r\n"
                + "   \"cardinality\": 50\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData,
                SchemaValidatorsConfig.builder().build());
        List<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON).stream().collect(Collectors.toList());
        assertEquals(1, messages.size());
        ValidationMessage message = messages.get(0);
        assertEquals(
                ": does not have a value in the enumeration [{\"name\":\"EMPTY\",\"cardinality\":0}, {\"name\":\"OK\",\"cardinality\":20}, {\"name\":\"UNKNOWN\",\"cardinality\":30}, {\"name\":\"WARNING\",\"cardinality\":40}, {\"name\":\"CRITICAL\",\"cardinality\":50}]",
                message.toString());
    }

    @Test
    void enumWithHeterogenousNodes() {
        String schemaData = "{\r\n"
                + "            \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "            \"enum\": [6, \"foo\", [], true, {\"foo\": 12}]\r\n"
                + "        }";
        String inputData = "{\r\n"
                + "   \"name\": \"FOO\",\r\n"
                + "   \"cardinality\": 50\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData,
                SchemaValidatorsConfig.builder().build());
        List<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON).stream().collect(Collectors.toList());
        assertEquals(1, messages.size());
        ValidationMessage message = messages.get(0);
        assertEquals(": does not have a value in the enumeration [6, \"foo\", [], true, {\"foo\":12}]", message.toString());
    }
}
