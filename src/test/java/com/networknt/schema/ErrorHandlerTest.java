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

/**
 * ErrorHandlerTest.
 */
class ErrorHandlerTest {
    @Test
    void legacyConstructorDefaultsCustomMessageToFalse() {
        Error error = new Error("type", null, null, null, new Object[0], null,
                "type", () -> "standard message", null, null);

        assertFalse(error.isCustomMessage());
    }

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
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().errorMessageKeyword("errorMessage").build();
        Schema schema = SchemaRegistry
                .withDefaultDialect(SpecificationVersion.DRAFT_2020_12, builder -> builder.schemaRegistryConfig(config))
                .getSchema(schemaData);
        List<Error> messages = schema.validate(inputData, InputFormat.JSON).stream().collect(Collectors.toList());
        assertFalse(messages.isEmpty());
        assertEquals("/foo", messages.get(0).getInstanceLocation().toString());
        assertEquals("should be an object", messages.get(0).getMessage());
        assertTrue(messages.get(0).isCustomMessage());
        assertEquals("", messages.get(1).getInstanceLocation().toString());
        assertEquals("should not have properties other than foo", messages.get(1).getMessage());
        assertTrue(messages.get(1).isCustomMessage());
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
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().errorMessageKeyword("errorMessage").build();
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12, builder -> builder.schemaRegistryConfig(config)).getSchema(schemaData);
        List<Error> messages = schema.validate(inputData, InputFormat.JSON).stream().collect(Collectors.toList());
        assertFalse(messages.isEmpty());
        assertEquals("/keyword1", messages.get(0).getInstanceLocation().toString());
        assertEquals("关键字1必须为字符串", messages.get(0).getMessage());
        assertTrue(messages.get(0).isCustomMessage());
    }

    @Test
    void propertyNamedMessageDoesNotMarkStandardMessageAsCustom() {
        String schemaData = "{\"type\":\"object\",\"properties\":{"
                + "\"message\":{\"type\":\"string\"},"
                + "\"count\":{\"type\":\"integer\"}}}";
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().errorMessageKeyword("message").build();
        Schema schema = SchemaRegistry
                .withDefaultDialect(SpecificationVersion.DRAFT_2020_12, builder -> builder.schemaRegistryConfig(config))
                .getSchema(schemaData);

        List<Error> messages = schema.validate("{\"count\":\"invalid\"}", InputFormat.JSON).stream()
                .collect(Collectors.toList());

        assertEquals(1, messages.size());
        assertFalse(messages.get(0).isCustomMessage());
    }

    @Test
    void propertiesAndItemsPropertyNamesDoNotMarkStandardMessagesAsCustom() {
        String nestedObject = "{\"type\":\"object\",\"properties\":{"
                + "\"message\":{\"type\":\"string\"},"
                + "\"n\":{\"type\":\"integer\"}}}";
        String schemaData = "{\"type\":\"object\",\"properties\":{"
                + "\"properties\":" + nestedObject + ","
                + "\"items\":" + nestedObject + "}}";
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().errorMessageKeyword("message").build();
        Schema schema = SchemaRegistry
                .withDefaultDialect(SpecificationVersion.DRAFT_2020_12, builder -> builder.schemaRegistryConfig(config))
                .getSchema(schemaData);

        List<Error> messages = schema
                .validate("{\"properties\":{\"n\":\"invalid\"},\"items\":{\"n\":\"invalid\"}}",
                        InputFormat.JSON)
                .stream().collect(Collectors.toList());

        assertEquals(2, messages.size());
        assertTrue(messages.stream().noneMatch(Error::isCustomMessage));
    }

}
