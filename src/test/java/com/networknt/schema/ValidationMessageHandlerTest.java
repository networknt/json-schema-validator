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
 * ValidationMessageHandlerTest.
 */
class ValidationMessageHandlerTest {
    @Test
    void errorMessage() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"required\": [\r\n"
                + "    \"foo\"\r\n"
                + "  ],\r\n"
                + "  \"properties\": {\r\n"
                + "    \"foo\": {\r\n"
                + "      \"type\": \"integer\"\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"additionalProperties\": false,\r\n"
                + "  \"errorMessage\": {\r\n"
                + "    \"type\": \"should be an object\",\r\n"
                + "    \"required\": \"should have property foo\",\r\n"
                + "    \"additionalProperties\": \"should not have properties other than foo\"\r\n"
                + "  }\r\n"
                + "}";
        String inputData = "{\r\n"
                + "  \"foo\": \"a\",\r\n"
                + "  \"bar\": 2\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData,
                SchemaValidatorsConfig.builder().errorMessageKeyword("errorMessage").build());
        List<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON).stream().collect(Collectors.toList());
        assertFalse(messages.isEmpty());
        assertEquals("/foo", messages.get(0).getInstanceLocation().toString());
        assertEquals("should be an object", messages.get(0).getMessage());
        assertEquals("", messages.get(1).getInstanceLocation().toString());
        assertEquals("should not have properties other than foo", messages.get(1).getMessage());
    }

    @Test
    void errorMessageUnionType() {
        String schemaData = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"keyword1\": {\r\n"
                + "      \"type\": [\r\n"
                + "        \"string\",\r\n"
                + "        \"null\"\r\n"
                + "      ],\r\n"
                + "      \"errorMessage\": {\r\n"
                + "        \"type\": \"关键字1必须为字符串\"\r\n"
                + "      },\r\n"
                + "      \"title\": \"关键字\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        String inputData = "{\r\n"
                + "  \"keyword1\": 2\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData,
                SchemaValidatorsConfig.builder().errorMessageKeyword("errorMessage").build());
        List<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON).stream().collect(Collectors.toList());
        assertFalse(messages.isEmpty());
        assertEquals("/keyword1", messages.get(0).getInstanceLocation().toString());
        assertEquals("关键字1必须为字符串", messages.get(0).getMessage());
    }

}
