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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.networknt.schema.dialect.Dialects;

class AnyOfValidatorTest {
    @Test
    void invalidTypeShouldThrowJsonSchemaException() {
        String schemaData = "{\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"User\": true\r\n"
                + "  },\r\n"
                + "  \"anyOf\": {\r\n"
                + "    \"$ref\": \"#/defs/User\"\r\n"
                + "  }\r\n"
                + "}";
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12);
        SchemaException ex = assertThrows(SchemaException.class, () -> factory.getSchema(schemaData));
        assertEquals("type", ex.getError().getMessageKey());
    }

    @Test
    void walkValidationWithNullNodeShouldNotValidate() {
        String schemaContents = "            {\r\n"
                + "                \"type\": \"object\",\r\n"
                + "                \"properties\": {\r\n"
                + "                    \"prop1\": {\r\n"
                + "                        \"anyOf\": [\r\n"
                + "                            {\r\n"
                + "                            \"type\": \"string\"\r\n"
                + "                            },\r\n"
                + "                            {\r\n"
                + "                            \"type\": \"integer\"\r\n"
                + "                            }\r\n"
                + "                        ]\r\n"
                + "                    }\r\n"
                + "                },\r\n"
                + "                \"additionalProperties\": false\r\n"
                + "            }";

        String jsonContents = "{}";

        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
        Schema schema = factory.getSchema(schemaContents);
        Result result = schema.walk(jsonContents, InputFormat.JSON, true);
        assertEquals(true, result.getErrors().isEmpty());
    }

    @Test
    void failFastStopsAtFailingAnyOf() {
        // The second allOf branch would add a type error if evaluation went on
        // after the anyOf failed.
        String schemaData = """
                {
                  "allOf": [
                    { "anyOf": [ { "required": ["a"] }, { "required": ["b"] } ] },
                    { "properties": { "c": { "type": "string" } } }
                  ]
                }
                """;
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().failFast(true).build();
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemaRegistryConfig(config)).getSchema(schemaData);
        List<Error> messages = schema.validate("{ \"c\": 1 }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("required", messages.get(0).getKeyword());
    }

    @Test
    void failFastReturnsFirstBranchErrorOfFailingAnyOf() {
        String schemaData = """
                {
                  "anyOf": [ { "type": "string" }, { "maximum": 0 } ]
                }
                """;
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().failFast(true).build();
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemaRegistryConfig(config)).getSchema(schemaData);
        List<Error> messages = schema.validate("1", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void reportsFailingAnyOfBranchErrorsWithoutFailFast() {
        String schemaData = """
                {
                  "anyOf": [ { "type": "string" }, { "maximum": 0 } ]
                }
                """;
        Schema schema = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12).getSchema(schemaData);
        List<Error> messages = schema.validate("1", InputFormat.JSON);
        assertEquals(List.of("type", "maximum"), messages.stream().map(Error::getKeyword).toList());
    }

    // The BedRoom branch matches the second room, but its discriminator value
    // maps to no schema, so the anyOf adds a discriminator error. The maxItems
    // keyword would add another error if evaluation went on after it.
    private static final String DISCRIMINATOR_SCHEMA = """
            {
              "type": "array",
              "allOf": [
                {
                  "items": {
                    "anyOf": [
                      { "$ref": "#/components/schemas/Kitchen" },
                      { "$ref": "#/components/schemas/BedRoom" }
                    ]
                  }
                },
                { "maxItems": 1 }
              ],
              "components": {
                "schemas": {
                  "Room": {
                    "type": "object",
                    "properties": { "@type": { "type": "string" } },
                    "required": ["@type"],
                    "discriminator": { "propertyName": "@type" }
                  },
                  "BedRoom": {
                    "type": "object",
                    "allOf": [
                      { "$ref": "#/components/schemas/Room" },
                      { "type": "object", "required": ["numberOfBeds"] }
                    ]
                  },
                  "Kitchen": {
                    "type": "object",
                    "allOf": [
                      { "$ref": "#/components/schemas/Room" },
                      { "type": "object", "required": ["hasMicrowaveOven"] }
                    ]
                  }
                }
              }
            }
            """;

    private static final String DISCRIMINATOR_INPUT = """
            [
              { "@type": "Kitchen", "hasMicrowaveOven": true },
              { "@type": "BedRooooom", "numberOfBeds": 4 }
            ]
            """;

    @Test
    void failFastStopsAtDiscriminatorError() {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().failFast(true).build();
        Schema schema = SchemaRegistry.withDialect(Dialects.getOpenApi31(),
                builder -> builder.schemaRegistryConfig(config)).getSchema(DISCRIMINATOR_SCHEMA);
        List<Error> messages = schema.validate(DISCRIMINATOR_INPUT, InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("discriminator", messages.get(0).getKeyword());
    }

    @Test
    void reportsDiscriminatorErrorThroughExecutionContext() {
        Schema schema = SchemaRegistry.withDialect(Dialects.getOpenApi31()).getSchema(DISCRIMINATOR_SCHEMA);
        List<Error> reported = new ArrayList<>();
        ExecutionContext executionContext = new ExecutionContext() {
            @Override
            public void addError(Error error) {
                reported.add(error);
                super.addError(error);
            }
        };
        List<Error> messages = schema.validate(executionContext, DISCRIMINATOR_INPUT, InputFormat.JSON);
        assertTrue(messages.stream().anyMatch(message -> "discriminator".equals(message.getKeyword())));
        assertTrue(reported.containsAll(messages));
    }
}
