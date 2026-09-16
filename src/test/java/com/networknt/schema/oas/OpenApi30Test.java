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
package com.networknt.schema.oas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.OutputFormat;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.path.PathType;
import com.networknt.schema.Error;

/**
 * OpenApi30Test.
 */
class OpenApi30Test {
    /** The trailing components block shared by the nullable tests below. */
    private static final String MONEY_COMPONENT = """
              "components": {
                "schemas": {
                  "Money": {
                    "type": "object",
                    "required": ["amount"],
                    "properties": {
                      "amount": { "type": "integer" }
                    }
                  }
                }
              }
            }
            """;

    private static Schema openApi30Schema(String schemaData) {
        return SchemaRegistry.withDialect(Dialects.getOpenApi30()).getSchema(schemaData);
    }

    /**
     * Test with the explicitly configured OpenApi30 instance.
     */
    @Test
    void validateMetaSchema() {
        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi30());
        Schema schema = factory.getSchema(SchemaLocation.of(
                "classpath:schema/oas/3.0/petstore.yaml#/paths/~1pet/post/requestBody/content/application~1json/schema"));
        String input = "{\r\n"
                + "  \"petType\": \"dog\",\r\n"
                + "  \"bark\": \"woof\"\r\n"
                + "}";
        List<Error> messages = schema.validate(input, InputFormat.JSON);
        assertEquals(0, messages.size());

        String invalid = "{\r\n"
                + "  \"petType\": \"dog\",\r\n"
                + "  \"meow\": \"meeeooow\"\r\n"
                + "}";
        messages = schema.validate(invalid, InputFormat.JSON);
        assertEquals(2, messages.size());
        List<Error> list = messages.stream().collect(Collectors.toList());
        assertEquals("oneOf", list.get(0).getKeyword());
        assertEquals("required", list.get(1).getKeyword());
        assertEquals("bark", list.get(1).getProperty());
    }

    /**
     * Tests that schema location with number in fragment can resolve.
     */
    @Test
    void jsonPointerWithNumberInFragment() {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().pathType(PathType.JSON_PATH).build();
        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi30(), builder -> builder.schemaRegistryConfig(config));
        Schema schema = factory.getSchema(SchemaLocation.of(
                "classpath:schema/oas/3.0/petstore.yaml#/paths/~1pet/post/responses/200/content/application~1json/schema")
                );
        assertNotNull(schema);
        //assertEquals("$.paths['/pet'].post.responses['200'].content['application/json'].schema",
        //        schema.getEvaluationPath().toString());
    }

    /**
     * Exclusive maximum true.
     */
    @Test
    void exclusiveMaximum() {
        String schemaData = """
                {
                  "type": "number",
                  "minimum": 0,
                  "maximum": 100,
                  "exclusiveMaximum": true
                }
                """;
        Schema schema = openApi30Schema(schemaData);
        assertFalse(schema.validate("100", InputFormat.JSON, OutputFormat.BOOLEAN));
    }

    /**
     * Exclusive minimum true.
     */
    @Test
    void exclusiveMinimum() {
        String schemaData = """
                {
                  "type": "number",
                  "minimum": 0,
                  "maximum": 100,
                  "exclusiveMinimum": true
                }
                """;
        Schema schema = openApi30Schema(schemaData);
        assertFalse(schema.validate("0", InputFormat.JSON, OutputFormat.BOOLEAN));
    }

    @ParameterizedTest
    @ValueSource(strings = { "allOf", "oneOf", "anyOf" })
    void nullableComposingKeywordRefAcceptsNull(String composingKeyword) {
        String schemaData = """
                {
                  "type": "object",
                  "required": ["value"],
                  "properties": {
                    "value": {
                      "%s": [ { "$ref": "#/components/schemas/Money" } ],
                      "nullable": true
                    }
                  },
                """.formatted(composingKeyword) + MONEY_COMPONENT;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    /**
     * A {@code nullable: true} declared directly alongside a {@code $ref} is a
     * sibling property on a Reference Object, which the OpenAPI 3.0
     * specification requires to be ignored. This must remain rejected even
     * though {@link #nullableComposingKeywordRefAcceptsNull(String)} accepts
     * the same {@code nullable} declared alongside a keyword that composes a
     * {@code $ref}.
     */
    @Test
    void nullableDirectRefSiblingIsIgnored() {
        String schemaData = """
                {
                  "type": "object",
                  "required": ["value"],
                  "properties": {
                    "value": {
                      "$ref": "#/components/schemas/Money",
                      "nullable": true
                    }
                  },
                """ + MONEY_COMPONENT;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableContainerNotLeakedIntoRefProperty() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "order": {
                      "nullable": true,
                      "type": "object",
                      "required": ["customer"],
                      "properties": {
                        "customer": { "$ref": "#/components/schemas/Customer" }
                      }
                    }
                  },
                  "components": {
                    "schemas": {
                      "Customer": {
                        "type": "object",
                        "required": ["name"],
                        "properties": {
                          "name": { "type": "string" }
                        }
                      }
                    }
                  }
                }
                """;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"order\": { \"customer\": null } }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableAllOfRefWithTwoLevelsOfComposingDepth() {
        String schemaData = """
                {
                  "type": "object",
                  "required": ["value"],
                  "properties": {
                    "value": {
                      "allOf": [ { "$ref": "#/components/schemas/Money" } ],
                      "nullable": true
                    }
                  },
                  "components": {
                    "schemas": {
                      "Money": {
                        "allOf": [
                          {
                            "allOf": [
                              {
                                "type": "object",
                                "required": ["amount"],
                                "properties": {
                                  "amount": { "type": "integer" }
                                }
                              }
                            ]
                          }
                        ]
                      }
                    }
                  }
                }
                """;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableAllOfRefWithTwoLevelsOfReferenceDepth() {
        String schemaData = """
                {
                  "type": "object",
                  "required": ["value"],
                  "properties": {
                    "value": {
                      "allOf": [ { "$ref": "#/components/schemas/Money" } ],
                      "nullable": true
                    }
                  },
                  "components": {
                    "schemas": {
                      "Money": {
                        "allOf": [ { "$ref": "#/components/schemas/Amount" } ]
                      },
                      "Amount": {
                        "type": "object",
                        "required": ["amount"],
                        "properties": {
                          "amount": { "type": "integer" }
                        }
                      }
                    }
                  }
                }
                """;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableNotDoesNotPropagateThroughNot() {
        String schemaData = """
                {
                  "type": "object",
                  "required": ["value"],
                  "properties": {
                    "value": {
                      "not": { "$ref": "#/components/schemas/Impossible" },
                      "nullable": true
                    }
                  },
                  "components": {
                    "schemas": {
                      "Impossible": { "type": "string" }
                    }
                  }
                }
                """;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableContainerNotLeakedIntoRefItem() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "list": {
                      "nullable": true,
                      "type": "array",
                      "items": { "$ref": "#/components/schemas/Money" }
                    }
                  },
                """ + MONEY_COMPONENT;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"list\": [ null ] }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableContainerNotLeakedIntoRefAdditionalProperties() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "meta": {
                      "nullable": true,
                      "type": "object",
                      "additionalProperties": { "$ref": "#/components/schemas/Money" }
                    }
                  },
                """ + MONEY_COMPONENT;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"meta\": { \"extra\": null } }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableAdditionalPropertiesNotLeakedWhenPropertyNamedAllOf() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "allOf": {
                      "nullable": true,
                      "type": "object",
                      "additionalProperties": { "$ref": "#/components/schemas/Money" }
                    }
                  },
                """ + MONEY_COMPONENT;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"allOf\": { \"extra\": null } }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableItemsNotLeakedWhenPropertyNamedAllOf() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "allOf": {
                      "nullable": true,
                      "type": "array",
                      "items": { "$ref": "#/components/schemas/Money" }
                    }
                  },
                """ + MONEY_COMPONENT;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"allOf\": [ null ] }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableNotLeakedThroughAncestorWhenPropertyNamedAllOfHoldsRefDirectly() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "outer": {
                      "nullable": true,
                      "type": "object",
                      "properties": {
                        "allOf": { "$ref": "#/components/schemas/Money" }
                      }
                    }
                  },
                """ + MONEY_COMPONENT;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"outer\": { \"allOf\": null } }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableAllOfRefNotLostWhenBranchDeclaresId() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "value": {
                      "allOf": [ { "id": "http://example.com/inner", "type": "string" } ],
                      "nullable": true
                    }
                  }
                }
                """;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableMultiBranchOneOfRef() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "value": {
                      "oneOf": [ { "$ref": "#/components/schemas/A" }, { "$ref": "#/components/schemas/B" } ],
                      "nullable": true
                    }
                  },
                  "components": {
                    "schemas": {
                      "A": { "type": "object", "properties": { "a": { "type": "string" } } },
                      "B": { "type": "object", "properties": { "b": { "type": "string" } } }
                    }
                  }
                }
                """;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableContainerNotLeakedIntoInlineProperty() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "order": {
                      "nullable": true,
                      "type": "object",
                      "properties": { "name": { "type": "string" } }
                    }
                  }
                }
                """;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"order\": { \"name\": null } }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableContainerNotLeakedIntoInlineItem() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "list": {
                      "nullable": true,
                      "type": "array",
                      "items": { "type": "string" }
                    }
                  }
                }
                """;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"list\": [ null ] }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableContainerNotLeakedIntoInlineAdditionalProperties() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "meta": {
                      "nullable": true,
                      "type": "object",
                      "additionalProperties": { "type": "string" }
                    }
                  }
                }
                """;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"meta\": { \"extra\": null } }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableContainerStillAllowsNullForItself() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "order": {
                      "nullable": true,
                      "type": "object",
                      "properties": { "name": { "type": "string" } }
                    }
                  }
                }
                """;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"order\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void nullableAllOfRefToEnumComponent() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "value": {
                      "allOf": [ { "$ref": "#/components/schemas/E" } ],
                      "nullable": true
                    }
                  },
                  "components": {
                    "schemas": {
                      "E": { "type": "string", "enum": ["a", "b"] }
                    }
                  }
                }
                """;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }

    @Test
    void typeLooseNullableEnumRejectsStringsThatAreNotInTheEnum() {
        // Under typeLoose, enum falls back to comparing values as text. The
        // accepted set no longer holds a NullNode, whose text form is the empty
        // string, so "" is no longer silently one of the permitted values.
        String schemaData = """
                {
                  "properties": {
                    "v": { "enum": ["a"], "nullable": true }
                  }
                }
                """;
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().typeLoose(true).build();
        Schema schema = SchemaRegistry
                .withDialect(Dialects.getOpenApi30(), builder -> builder.schemaRegistryConfig(config))
                .getSchema(schemaData);

        List<Error> messages = schema.validate("{ \"v\": \"\" }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("enum", messages.get(0).getKeyword());

        assertEquals(1, schema.validate("{ \"v\": \"null\" }", InputFormat.JSON).size());
        assertEquals(0, schema.validate("{ \"v\": null }", InputFormat.JSON).size());
        assertEquals(0, schema.validate("{ \"v\": \"a\" }", InputFormat.JSON).size());
    }

    @Test
    void malformedNullableOnAComposingAncestorIsTreatedAsAbsent() {
        // The walk reads nullable on every composing ancestor, so a member that
        // cannot be coerced to a boolean has to be treated as absent rather
        // than aborting the validation.
        String schemaData = """
                {
                  "properties": {
                    "v": {
                      "nullable": [1],
                      "allOf": [ { "allOf": [ { "type": "object" } ] } ]
                    }
                  }
                }
                """;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"v\": null }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("type", messages.get(0).getKeyword());
    }

    @Test
    void nullableEnumStillRejectsNonNullValueOutsideEnum() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "value": {
                      "allOf": [ { "$ref": "#/components/schemas/E" } ],
                      "nullable": true
                    }
                  },
                  "components": {
                    "schemas": {
                      "E": { "type": "string", "enum": ["a", "b"] }
                    }
                  }
                }
                """;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": \"c\" }", InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("enum", messages.get(0).getKeyword());
    }

    @Test
    void nullableMultiBranchOneOfRefWrappedInAllOf() {
        String schemaData = """
                {
                  "type": "object",
                  "properties": {
                    "value": {
                      "allOf": [ {
                        "oneOf": [ { "$ref": "#/components/schemas/A" }, { "$ref": "#/components/schemas/B" } ]
                      } ],
                      "nullable": true
                    }
                  },
                  "components": {
                    "schemas": {
                      "A": { "type": "object", "properties": { "a": { "type": "string" } } },
                      "B": { "type": "object", "properties": { "b": { "type": "string" } } }
                    }
                  }
                }
                """;
        Schema schema = openApi30Schema(schemaData);

        List<Error> messages = schema.validate("{ \"value\": null }", InputFormat.JSON);
        assertEquals(0, messages.size());
    }
}
