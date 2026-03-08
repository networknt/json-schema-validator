/*
 * Copyright (c) 2023 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.networknt.schema.dialect.Dialects;

/**
 * Test for discriminator.
 */
class DiscriminatorValidatorTest {
    /**
     * Issue 609.
     */
    @Test
    void discriminatorInArray() {
        String schemaData = "{\r\n"
                + "  \"type\": \"array\",\r\n"
                + "  \"items\": {\r\n"
                + "    \"anyOf\": [\r\n"
                + "      {\r\n"
                + "        \"$ref\": \"#/components/schemas/Kitchen\"\r\n"
                + "      },\r\n"
                + "      {\r\n"
                + "        \"$ref\": \"#/components/schemas/BedRoom\"\r\n"
                + "      }\r\n"
                + "    ]\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"Room\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"properties\": {\r\n"
                + "          \"@type\": {\r\n"
                + "            \"type\": \"string\"\r\n"
                + "          }\r\n"
                + "        },\r\n"
                + "        \"required\": [\r\n"
                + "          \"@type\"\r\n"
                + "        ],\r\n"
                + "        \"discriminator\": {\r\n"
                + "          \"propertyName\": \"@type\"\r\n"
                + "        }\r\n"
                + "      },\r\n"
                + "      \"BedRoom\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"$ref\": \"#/components/schemas/Room\"\r\n"
                + "          },\r\n"
                + "          {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "              \"numberOfBeds\": {\r\n"
                + "                \"type\": \"integer\"\r\n"
                + "              }\r\n"
                + "            },\r\n"
                + "            \"required\": [\r\n"
                + "              \"numberOfBeds\"\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      },\r\n"
                + "      \"Kitchen\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"$ref\": \"#/components/schemas/Room\"\r\n"
                + "          },\r\n"
                + "          {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "              \"hasMicrowaveOven\": {\r\n"
                + "                \"type\": \"boolean\"\r\n"
                + "              }\r\n"
                + "            },\r\n"
                + "            \"required\": [\r\n"
                + "              \"hasMicrowaveOven\"\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        
        String inputData = "[\r\n"
                + "  {\r\n"
                + "    \"@type\": \"Kitchen\",\r\n"
                + "    \"hasMicrowaveOven\": true\r\n"
                + "  },\r\n"
                + "  {\r\n"
                + "    \"@type\": \"BedRoom\",\r\n"
                + "    \"numberOfBeds\": 4\r\n"
                + "  }\r\n"
                + "]";
        
        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi31());
        Schema schema = factory.getSchema(schemaData);
        List<Error> messages =  schema.validate(inputData, InputFormat.JSON);
        assertTrue(messages.isEmpty());
    }

    /**
     * Issue 588.
     */
    @Test
    void anyOfWithConfigEnabledButNoDiscriminator() {
        String schemaData = "{\r\n"
                + "           \"type\": \"object\",\r\n"
                + "           \"properties\": {\r\n"
                + "               \"intOrStringType\": {\r\n"
                + "                   \"anyOf\": [\r\n"
                + "                       {\r\n"
                + "                           \"type\": \"integer\"\r\n"
                + "                       },\r\n"
                + "                       {\r\n"
                + "                           \"type\": \"string\"\r\n"
                + "                       }\r\n"
                + "                   ]\r\n"
                + "               }\r\n"
                + "           }\r\n"
                + "       }";
        
        String inputData = "{\r\n"
                + "                   \"intOrStringType\": 4\r\n"
                + "               }";
        
        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi31());
        Schema schema = factory.getSchema(schemaData);
        List<Error> messages =  schema.validate(inputData, InputFormat.JSON);
        assertTrue(messages.isEmpty());
    }

    /**
     * Issue 609.
     */
    @Test
    void discriminatorInArrayInvalidDiscriminatorPropertyAnyOf() {
        String schemaData = "{\r\n"
                + "  \"type\": \"array\",\r\n"
                + "  \"items\": {\r\n"
                + "    \"anyOf\": [\r\n"
                + "      {\r\n"
                + "        \"$ref\": \"#/components/schemas/Kitchen\"\r\n"
                + "      },\r\n"
                + "      {\r\n"
                + "        \"$ref\": \"#/components/schemas/BedRoom\"\r\n"
                + "      }\r\n"
                + "    ]\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"Room\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"properties\": {\r\n"
                + "          \"@type\": {\r\n"
                + "            \"type\": \"string\"\r\n"
                + "          }\r\n"
                + "        },\r\n"
                + "        \"required\": [\r\n"
                + "          \"@type\"\r\n"
                + "        ],\r\n"
                + "        \"discriminator\": {\r\n"
                + "          \"propertyName\": \"@type\"\r\n"
                + "        }\r\n"
                + "      },\r\n"
                + "      \"BedRoom\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"$ref\": \"#/components/schemas/Room\"\r\n"
                + "          },\r\n"
                + "          {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "              \"numberOfBeds\": {\r\n"
                + "                \"type\": \"integer\"\r\n"
                + "              }\r\n"
                + "            },\r\n"
                + "            \"required\": [\r\n"
                + "              \"numberOfBeds\"\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      },\r\n"
                + "      \"Kitchen\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"$ref\": \"#/components/schemas/Room\"\r\n"
                + "          },\r\n"
                + "          {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "              \"hasMicrowaveOven\": {\r\n"
                + "                \"type\": \"boolean\"\r\n"
                + "              }\r\n"
                + "            },\r\n"
                + "            \"required\": [\r\n"
                + "              \"hasMicrowaveOven\"\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        String inputData = "[\r\n"
                + "  {\r\n"
                + "    \"@type\": \"Kitchen\",\r\n"
                + "    \"hasMicrowaveOven\": true\r\n"
                + "  },\r\n"
                + "  {\r\n"
                + "    \"@type\": \"BedRooooom\",\r\n"
                + "    \"numberOfBeds\": 4\r\n"
                + "  }\r\n"
                + "]";

        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi31());
        Schema schema = factory.getSchema(schemaData);
        List<Error> messages =  schema.validate(inputData, InputFormat.JSON);
        assertEquals(1, messages.size()); // RECHECK THIS
    }

    /**
     * Issue 609.
     */
    @Test
    void discriminatorInArrayInvalidDiscriminatorPropertyOneOf() {
        String schemaData = "{\r\n"
                + "  \"type\": \"array\",\r\n"
                + "  \"items\": {\r\n"
                + "    \"oneOf\": [\r\n"
                + "      {\r\n"
                + "        \"$ref\": \"#/components/schemas/Kitchen\"\r\n"
                + "      },\r\n"
                + "      {\r\n"
                + "        \"$ref\": \"#/components/schemas/BedRoom\"\r\n"
                + "      }\r\n"
                + "    ]\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"Room\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"properties\": {\r\n"
                + "          \"@type\": {\r\n"
                + "            \"type\": \"string\"\r\n"
                + "          }\r\n"
                + "        },\r\n"
                + "        \"required\": [\r\n"
                + "          \"@type\"\r\n"
                + "        ],\r\n"
                + "        \"discriminator\": {\r\n"
                + "          \"propertyName\": \"@type\"\r\n"
                + "        }\r\n"
                + "      },\r\n"
                + "      \"BedRoom\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"$ref\": \"#/components/schemas/Room\"\r\n"
                + "          },\r\n"
                + "          {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "              \"numberOfBeds\": {\r\n"
                + "                \"type\": \"integer\"\r\n"
                + "              }\r\n"
                + "            },\r\n"
                + "            \"required\": [\r\n"
                + "              \"numberOfBeds\"\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      },\r\n"
                + "      \"Kitchen\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"$ref\": \"#/components/schemas/Room\"\r\n"
                + "          },\r\n"
                + "          {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "              \"hasMicrowaveOven\": {\r\n"
                + "                \"type\": \"boolean\"\r\n"
                + "              }\r\n"
                + "            },\r\n"
                + "            \"required\": [\r\n"
                + "              \"hasMicrowaveOven\"\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        String inputData = "[\r\n"
                + "  {\r\n"
                + "    \"@type\": \"Kitchen\",\r\n"
                + "    \"hasMicrowaveOven\": true\r\n"
                + "  },\r\n"
                + "  {\r\n"
                + "    \"@type\": \"BedRooooom\",\r\n"
                + "    \"numberOfBeds\": 4\r\n"
                + "  }\r\n"
                + "]";

        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi31());
        Schema schema = factory.getSchema(schemaData);
        List<Error> messages =  schema.validate(inputData, InputFormat.JSON);
        assertEquals(1, messages.size());
    }

    @Test
    void discriminatorInArrayOneOfShouldOnlyReportErrorsInMatchingDiscriminator() {
        String schemaData = "{\r\n"
                + "  \"type\": \"array\",\r\n"
                + "  \"items\": {\r\n"
                + "    \"oneOf\": [\r\n"
                + "      {\r\n"
                + "        \"$ref\": \"#/components/schemas/Kitchen\"\r\n"
                + "      },\r\n"
                + "      {\r\n"
                + "        \"$ref\": \"#/components/schemas/BedRoom\"\r\n"
                + "      }\r\n"
                + "    ]\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"Room\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"properties\": {\r\n"
                + "          \"@type\": {\r\n"
                + "            \"type\": \"string\"\r\n"
                + "          }\r\n"
                + "        },\r\n"
                + "        \"required\": [\r\n"
                + "          \"@type\"\r\n"
                + "        ],\r\n"
                + "        \"discriminator\": {\r\n"
                + "          \"propertyName\": \"@type\"\r\n"
                + "        }\r\n"
                + "      },\r\n"
                + "      \"BedRoom\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"$ref\": \"#/components/schemas/Room\"\r\n"
                + "          },\r\n"
                + "          {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "              \"numberOfBeds\": {\r\n"
                + "                \"type\": \"integer\"\r\n"
                + "              }\r\n"
                + "            },\r\n"
                + "            \"required\": [\r\n"
                + "              \"numberOfBeds\"\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      },\r\n"
                + "      \"Kitchen\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"$ref\": \"#/components/schemas/Room\"\r\n"
                + "          },\r\n"
                + "          {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "              \"hasMicrowaveOven\": {\r\n"
                + "                \"type\": \"boolean\"\r\n"
                + "              }\r\n"
                + "            },\r\n"
                + "            \"required\": [\r\n"
                + "              \"hasMicrowaveOven\"\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        String inputData = "[\r\n"
                + "  {\r\n"
                + "    \"@type\": \"Kitchen\",\r\n"
                + "    \"hasMicrowaveOven\": true\r\n"
                + "  },\r\n"
                + "  {\r\n"
                + "    \"@type\": \"BedRoom\",\r\n"
                + "    \"incorrectProperty\": 4\r\n"
                + "  }\r\n"
                + "]";

        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi31());
        Schema schema = factory.getSchema(schemaData);
        List<Error> messages =  schema.validate(inputData, InputFormat.JSON);
        // Only the oneOf and the error in the BedRoom discriminator is reported
        // the mismatch in Kitchen is not reported
        assertEquals(2, messages.size());
        List<Error> list = messages.stream().collect(Collectors.toList());
        assertEquals("oneOf", list.get(0).getKeyword());
        assertEquals("required", list.get(1).getKeyword());
        assertEquals("numberOfBeds", list.get(1).getProperty());
    }

    @Test
    void discriminatorInOneOfShouldOnlyReportErrorsInMatchingDiscriminator() {
        String schemaData = "{\r\n"
                + "  \"type\": \"array\",\r\n"
                + "  \"items\": {\r\n"
                + "    \"oneOf\": [\r\n"
                + "      {\r\n"
                + "        \"$ref\": \"#/components/schemas/Kitchen\"\r\n"
                + "      },\r\n"
                + "      {\r\n"
                + "        \"$ref\": \"#/components/schemas/BedRoom\"\r\n"
                + "      }\r\n"
                + "    ],\r\n"
                + "    \"discriminator\": {\r\n"
                + "      \"propertyName\": \"@type\"\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"Room\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"properties\": {\r\n"
                + "          \"@type\": {\r\n"
                + "            \"type\": \"string\"\r\n"
                + "          }\r\n"
                + "        },\r\n"
                + "        \"required\": [\r\n"
                + "          \"@type\"\r\n"
                + "        ]\r\n"
                + "      },\r\n"
                + "      \"BedRoom\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"$ref\": \"#/components/schemas/Room\"\r\n"
                + "          },\r\n"
                + "          {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "              \"numberOfBeds\": {\r\n"
                + "                \"type\": \"integer\"\r\n"
                + "              }\r\n"
                + "            },\r\n"
                + "            \"required\": [\r\n"
                + "              \"numberOfBeds\"\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      },\r\n"
                + "      \"Kitchen\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"$ref\": \"#/components/schemas/Room\"\r\n"
                + "          },\r\n"
                + "          {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "              \"hasMicrowaveOven\": {\r\n"
                + "                \"type\": \"boolean\"\r\n"
                + "              }\r\n"
                + "            },\r\n"
                + "            \"required\": [\r\n"
                + "              \"hasMicrowaveOven\"\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        String inputData = "[\r\n"
                + "  {\r\n"
                + "    \"@type\": \"Kitchen\",\r\n"
                + "    \"hasMicrowaveOven\": true\r\n"
                + "  },\r\n"
                + "  {\r\n"
                + "    \"@type\": \"BedRoom\",\r\n"
                + "    \"incorrectProperty\": 4\r\n"
                + "  }\r\n"
                + "]";

        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi31());
        Schema schema = factory.getSchema(schemaData);
        List<Error> messages =  schema.validate(inputData, InputFormat.JSON);
        // Only the oneOf and the error in the BedRoom discriminator is reported
        // the mismatch in Kitchen is not reported
        assertEquals(2, messages.size());
        List<Error> list = messages.stream().collect(Collectors.toList());
        assertEquals("oneOf", list.get(0).getKeyword());
        assertEquals("required", list.get(1).getKeyword());
        assertEquals("numberOfBeds", list.get(1).getProperty());
    }

    @Test
    void discriminatorMappingInOneOfShouldOnlyReportErrorsInMatchingDiscriminator() {
        String schemaData = "{\r\n"
                + "  \"type\": \"array\",\r\n"
                + "  \"items\": {\r\n"
                + "    \"oneOf\": [\r\n"
                + "      {\r\n"
                + "        \"$ref\": \"#/components/schemas/Kitchen\"\r\n"
                + "      },\r\n"
                + "      {\r\n"
                + "        \"$ref\": \"#/components/schemas/BedRoom\"\r\n"
                + "      }\r\n"
                + "    ],\r\n"
                + "    \"discriminator\": {\r\n"
                + "      \"propertyName\": \"@type\",\r\n"
                + "      \"mapping\": {\r\n"
                + "        \"kitchen\": \"#/components/schemas/Kitchen\",\r\n"
                + "        \"bedroom\": \"#/components/schemas/BedRoom\"\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"Room\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"properties\": {\r\n"
                + "          \"@type\": {\r\n"
                + "            \"type\": \"string\"\r\n"
                + "          }\r\n"
                + "        },\r\n"
                + "        \"required\": [\r\n"
                + "          \"@type\"\r\n"
                + "        ]\r\n"
                + "      },\r\n"
                + "      \"BedRoom\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"$ref\": \"#/components/schemas/Room\"\r\n"
                + "          },\r\n"
                + "          {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "              \"numberOfBeds\": {\r\n"
                + "                \"type\": \"integer\"\r\n"
                + "              }\r\n"
                + "            },\r\n"
                + "            \"required\": [\r\n"
                + "              \"numberOfBeds\"\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      },\r\n"
                + "      \"Kitchen\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"$ref\": \"#/components/schemas/Room\"\r\n"
                + "          },\r\n"
                + "          {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "              \"hasMicrowaveOven\": {\r\n"
                + "                \"type\": \"boolean\"\r\n"
                + "              }\r\n"
                + "            },\r\n"
                + "            \"required\": [\r\n"
                + "              \"hasMicrowaveOven\"\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        String inputData = "[\r\n"
                + "  {\r\n"
                + "    \"@type\": \"kitchen\",\r\n"
                + "    \"hasMicrowaveOven\": true\r\n"
                + "  },\r\n"
                + "  {\r\n"
                + "    \"@type\": \"bedroom\",\r\n"
                + "    \"incorrectProperty\": 4\r\n"
                + "  }\r\n"
                + "]";

        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi31());
        Schema schema = factory.getSchema(schemaData);
        List<Error> messages =  schema.validate(inputData, InputFormat.JSON);
        // Only the oneOf and the error in the BedRoom discriminator is reported
        // the mismatch in Kitchen is not reported
        assertEquals(2, messages.size());
        List<Error> list = messages.stream().collect(Collectors.toList());
        assertEquals("oneOf", list.get(0).getKeyword());
        assertEquals("required", list.get(1).getKeyword());
        assertEquals("numberOfBeds", list.get(1).getProperty());
    }

    /**
     * See issue 436 and 985.
     */
    @Test
    void oneOfMissingDiscriminatorValue() {
        String schemaData = "    {\r\n"
                + "          \"type\": \"object\",\r\n"
                + "          \"discriminator\": { \"propertyName\": \"name\" },\r\n"
                + "          \"oneOf\": [\r\n"
                + "            {\r\n"
                + "              \"$ref\": \"#/defs/Foo\"\r\n"
                + "            },\r\n"
                + "            {\r\n"
                + "              \"$ref\": \"#/defs/Bar\"\r\n"
                + "            }\r\n"
                + "          ],\r\n"
                + "          \"defs\": {\r\n"
                + "            \"Foo\": {\r\n"
                + "              \"type\": \"object\",\r\n"
                + "              \"properties\": {\r\n"
                + "                \"name\": {\r\n"
                + "                  \"const\": \"Foo\"\r\n"
                + "                }\r\n"
                + "              },\r\n"
                + "              \"required\": [ \"name\" ],\r\n"
                + "              \"additionalProperties\": false\r\n"
                + "            },\r\n"
                + "            \"Bar\": {\r\n"
                + "              \"type\": \"object\",\r\n"
                + "              \"properties\": {\r\n"
                + "                \"name\": {\r\n"
                + "                  \"const\": \"Bar\"\r\n"
                + "                }\r\n"
                + "              },\r\n"
                + "              \"required\": [ \"name\" ],\r\n"
                + "              \"additionalProperties\": false\r\n"
                + "            }\r\n"
                + "          }\r\n"
                + "        }";

        String inputData = "{}";

        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi31());
        Schema schema = factory.getSchema(schemaData);
        List<Error> messages =  schema.validate(inputData, InputFormat.JSON);
        assertEquals(3, messages.size());
        List<Error> list = messages.stream().collect(Collectors.toList());
        assertEquals("oneOf", list.get(0).getKeyword());
        assertEquals("required", list.get(1).getKeyword());
        assertEquals("required", list.get(2).getKeyword());
    }

    /**
     * See issue 436.
     */
    @Test
    void anyOfMissingDiscriminatorValue() {
        String schemaData = "{\r\n"
                + "  \"type\": \"array\",\r\n"
                + "  \"items\": {\r\n"
                + "    \"anyOf\": [\r\n"
                + "      {\r\n"
                + "        \"$ref\": \"#/components/schemas/Kitchen\"\r\n"
                + "      },\r\n"
                + "      {\r\n"
                + "        \"$ref\": \"#/components/schemas/BedRoom\"\r\n"
                + "      }\r\n"
                + "    ]\r\n"
                + "  },\r\n"
                + "  \"components\": {\r\n"
                + "    \"schemas\": {\r\n"
                + "      \"Room\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"properties\": {\r\n"
                + "          \"@type\": {\r\n"
                + "            \"type\": \"string\"\r\n"
                + "          }\r\n"
                + "        },\r\n"
                + "        \"required\": [\r\n"
                + "          \"@type\"\r\n"
                + "        ],\r\n"
                + "        \"discriminator\": {\r\n"
                + "          \"propertyName\": \"@type\"\r\n"
                + "        }\r\n"
                + "      },\r\n"
                + "      \"BedRoom\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"$ref\": \"#/components/schemas/Room\"\r\n"
                + "          },\r\n"
                + "          {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "              \"numberOfBeds\": {\r\n"
                + "                \"type\": \"integer\"\r\n"
                + "              }\r\n"
                + "            },\r\n"
                + "            \"required\": [\r\n"
                + "              \"numberOfBeds\"\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      },\r\n"
                + "      \"Kitchen\": {\r\n"
                + "        \"type\": \"object\",\r\n"
                + "        \"allOf\": [\r\n"
                + "          {\r\n"
                + "            \"$ref\": \"#/components/schemas/Room\"\r\n"
                + "          },\r\n"
                + "          {\r\n"
                + "            \"type\": \"object\",\r\n"
                + "            \"properties\": {\r\n"
                + "              \"hasMicrowaveOven\": {\r\n"
                + "                \"type\": \"boolean\"\r\n"
                + "              }\r\n"
                + "            },\r\n"
                + "            \"required\": [\r\n"
                + "              \"hasMicrowaveOven\"\r\n"
                + "            ]\r\n"
                + "          }\r\n"
                + "        ]\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        String inputData = "[\r\n"
                + "  {\r\n"
                + "    \"hasMicrowaveOven\": true\r\n"
                + "  },\r\n"
                + "  {\r\n"
                + "    \"@type\": \"BedRoom\",\r\n"
                + "    \"numberOfBeds\": 4\r\n"
                + "  }\r\n"
                + "]";

        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi31());
        Schema schema = factory.getSchema(schemaData);
        List<Error> messages =  schema.validate(inputData, InputFormat.JSON);
        List<Error> list = messages.stream().collect(Collectors.toList());
        assertEquals("required", list.get(0).getKeyword());
    }
    
    /**
     * Mapped to Bedroom with missing number of beds, however the discriminator is
     * not supposed to change the result of anyOf and the data passes for "/anyOf/0"
     * : {"$ref":"#/components/schemas/Room"}.
     * 
     * This case is an example of the actual implementation for undefined behavior
     * eg. multiple discriminators for an instance location.
     */
    @Test
    void anyOfRedefinedDiscriminatorAndDiscriminatorWithMissingPropertyName() {
        String schemaData = "{\r\n"
        		+ "  \"anyOf\": [\r\n"
        		+ "    {\r\n"
        		+ "      \"$ref\": \"#/components/schemas/Room\"\r\n"
        		+ "    },\r\n"
        		+ "    {\r\n"
        		+ "      \"$ref\": \"#/components/schemas/BedRoom\"\r\n"
        		+ "    },\r\n"
        		+ "    {\r\n"
        		+ "      \"$ref\": \"#/components/schemas/KidsBedRoom\"\r\n"
        		+ "    },\r\n"
        		+ "    {\r\n"
        		+ "      \"$ref\": \"#/components/schemas/Kitchen\"\r\n"
        		+ "    },\r\n"
        		+ "    {\r\n"
        		+ "      \"$ref\": \"#/components/schemas/GuestRoom\"\r\n"
        		+ "    }\r\n"
        		+ "  ],\r\n"
        		+ "  \"components\": {\r\n"
        		+ "    \"schemas\": {\r\n"
        		+ "      \"Room\": {\r\n"
        		+ "        \"type\": \"object\",\r\n"
        		+ "        \"properties\": {\r\n"
        		+ "          \"@type\": {\r\n"
        		+ "            \"type\": \"string\"\r\n"
        		+ "          },\r\n"
        		+ "          \"floor\": {\r\n"
        		+ "            \"type\": \"integer\"\r\n"
        		+ "          }\r\n"
        		+ "        },\r\n"
        		+ "        \"required\": [\r\n"
        		+ "          \"@type\"\r\n"
        		+ "        ],\r\n"
        		+ "        \"discriminator\": {\r\n"
        		+ "          \"propertyName\": \"@type\",\r\n"
        		+ "          \"mapping\": {\r\n"
        		+ "            \"bed\": \"#/components/schemas/BedRoom\",\r\n"
        		+ "            \"guest\": \"#/components/schemas/GuestRoom\"\r\n"
        		+ "          }\r\n"
        		+ "        }\r\n"
        		+ "      },\r\n"
        		+ "      \"BedRoom\": {\r\n"
        		+ "        \"type\": \"object\",\r\n"
        		+ "        \"allOf\": [\r\n"
        		+ "          {\r\n"
        		+ "            \"$ref\": \"#/components/schemas/Room\"\r\n"
        		+ "          },\r\n"
        		+ "          {\r\n"
        		+ "            \"type\": \"object\",\r\n"
        		+ "            \"properties\": {\r\n"
        		+ "              \"numberOfBeds\": {\r\n"
        		+ "                \"type\": \"integer\"\r\n"
        		+ "              }\r\n"
        		+ "            },\r\n"
        		+ "            \"required\": [\r\n"
        		+ "              \"numberOfBeds\"\r\n"
        		+ "            ]\r\n"
        		+ "          }\r\n"
        		+ "        ],\r\n"
        		+ "        \"discriminator\": {\r\n"
        		+ "          \"mapping\": {\r\n"
        		+ "            \"guest\": \"#/components/schemas/GuestRoom\"\r\n"
        		+ "          }\r\n"
        		+ "        }\r\n"
        		+ "      },\r\n"
        		+ "      \"KidsBedRoom\": {\r\n"
        		+ "        \"type\": \"object\",\r\n"
        		+ "        \"allOf\": [\r\n"
        		+ "          {\r\n"
        		+ "            \"$ref\": \"#/components/schemas/BedRoom\"\r\n"
        		+ "          },\r\n"
        		+ "          {\r\n"
        		+ "            \"type\": \"object\",\r\n"
        		+ "            \"properties\": {\r\n"
        		+ "              \"isTidy\": {\r\n"
        		+ "                \"type\": \"boolean\"\r\n"
        		+ "              }\r\n"
        		+ "            },\r\n"
        		+ "            \"required\": [\r\n"
        		+ "              \"isTidy\"\r\n"
        		+ "            ]\r\n"
        		+ "          }\r\n"
        		+ "        ]\r\n"
        		+ "      },\r\n"
        		+ "      \"GuestRoom\": {\r\n"
        		+ "        \"type\": \"object\",\r\n"
        		+ "        \"allOf\": [\r\n"
        		+ "          {\r\n"
        		+ "            \"$ref\": \"#/components/schemas/BedRoom\"\r\n"
        		+ "          },\r\n"
        		+ "          {\r\n"
        		+ "            \"type\": \"object\",\r\n"
        		+ "            \"properties\": {\r\n"
        		+ "              \"guest\": {\r\n"
        		+ "                \"type\": \"string\"\r\n"
        		+ "              }\r\n"
        		+ "            },\r\n"
        		+ "            \"required\": [\r\n"
        		+ "              \"guest\"\r\n"
        		+ "            ]\r\n"
        		+ "          }\r\n"
        		+ "        ]\r\n"
        		+ "      },\r\n"
        		+ "      \"Kitchen\": {\r\n"
        		+ "        \"type\": \"object\",\r\n"
        		+ "        \"allOf\": [\r\n"
        		+ "          {\r\n"
        		+ "            \"$ref\": \"#/components/schemas/Room\"\r\n"
        		+ "          },\r\n"
        		+ "          {\r\n"
        		+ "            \"type\": \"object\",\r\n"
        		+ "            \"properties\": {\r\n"
        		+ "              \"hasMicrowaveOven\": {\r\n"
        		+ "                \"type\": \"boolean\"\r\n"
        		+ "              },\r\n"
        		+ "              \"equipment\": {\r\n"
        		+ "                \"type\": \"array\",\r\n"
        		+ "                \"items\": {\r\n"
        		+ "                  \"anyOf\": [\r\n"
        		+ "                    {\r\n"
        		+ "                      \"$ref\": \"#/components/schemas/Pot\"\r\n"
        		+ "                    },\r\n"
        		+ "                    {\r\n"
        		+ "                      \"$ref\": \"#/components/schemas/Blender\"\r\n"
        		+ "                    }\r\n"
        		+ "                  ]\r\n"
        		+ "                }\r\n"
        		+ "              }\r\n"
        		+ "            },\r\n"
        		+ "            \"required\": [\r\n"
        		+ "              \"hasMicrowaveOven\"\r\n"
        		+ "            ]\r\n"
        		+ "          }\r\n"
        		+ "        ]\r\n"
        		+ "      },\r\n"
        		+ "      \"KitchenEquipment\": {\r\n"
        		+ "        \"type\": \"object\",\r\n"
        		+ "        \"properties\": {\r\n"
        		+ "          \"@type\": {\r\n"
        		+ "            \"type\": \"string\"\r\n"
        		+ "          }\r\n"
        		+ "        },\r\n"
        		+ "        \"required\": [\r\n"
        		+ "          \"@type\"\r\n"
        		+ "        ],\r\n"
        		+ "        \"discriminator\": {\r\n"
        		+ "          \"propertyName\": \"@type\"\r\n"
        		+ "        }\r\n"
        		+ "      },\r\n"
        		+ "      \"Pot\": {\r\n"
        		+ "        \"allOf\": [\r\n"
        		+ "          {\r\n"
        		+ "            \"$ref\": \"#/components/schemas/KitchenEquipment\"\r\n"
        		+ "          },\r\n"
        		+ "          {\r\n"
        		+ "            \"type\": \"object\",\r\n"
        		+ "            \"properties\": {\r\n"
        		+ "              \"capacity\": {\r\n"
        		+ "                \"type\": \"integer\"\r\n"
        		+ "              }\r\n"
        		+ "            },\r\n"
        		+ "            \"required\": [\r\n"
        		+ "              \"capacity\"\r\n"
        		+ "            ]\r\n"
        		+ "          }\r\n"
        		+ "        ]\r\n"
        		+ "      },\r\n"
        		+ "      \"Blender\": {\r\n"
        		+ "        \"allOf\": [\r\n"
        		+ "          {\r\n"
        		+ "            \"$ref\": \"#/components/schemas/KitchenEquipment\"\r\n"
        		+ "          },\r\n"
        		+ "          {\r\n"
        		+ "            \"type\": \"object\",\r\n"
        		+ "            \"properties\": {\r\n"
        		+ "              \"maxSpeed\": {\r\n"
        		+ "                \"type\": \"integer\"\r\n"
        		+ "              }\r\n"
        		+ "            },\r\n"
        		+ "            \"required\": [\r\n"
        		+ "              \"maxSpeed\"\r\n"
        		+ "            ]\r\n"
        		+ "          }\r\n"
        		+ "        ]\r\n"
        		+ "      }\r\n"
        		+ "    }\r\n"
        		+ "  }\r\n"
        		+ "}";

        String inputData = "{\"@type\":\"bed\"}";

        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi31());
        Schema schema = factory.getSchema(schemaData);
        List<Error> messages =  schema.validate(inputData, InputFormat.JSON);
        List<Error> list = messages.stream().collect(Collectors.toList());
        // There should be no errors as discriminator should not affect the validation result of anyOf
        // Although the matched schema has the following error
        // : required property 'numberOfBeds' not found
        // There is still a schema in the anyOf that matches
        assertTrue(list.isEmpty());
    }

    /**
     * Issue 1225.
     * <p>
     * When oneOf with discriminator mapping is used and the discriminating value
     * maps to a specific schema (e.g., type=string -> $defs/string), but the
     * data only validates against a different schema (e.g., $defs/number),
     * validation should fail because the discriminator-indicated schema is not
     * the one that matches.
     */
    @Test
    void oneOfDiscriminatorEnabledWithDiscriminatorMismatch() {
        String schemaData = "{\r\n"
                + "  \"discriminator\": {\r\n"
                + "    \"propertyName\": \"type\",\r\n"
                + "    \"mapping\": {\r\n"
                + "      \"string\": \"#/$defs/string\",\r\n"
                + "      \"number\": \"#/$defs/number\"\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"oneOf\": [\r\n"
                + "    {\r\n"
                + "      \"$ref\": \"#/$defs/string\"\r\n"
                + "    },\r\n"
                + "    {\r\n"
                + "      \"$ref\": \"#/$defs/number\"\r\n"
                + "    }\r\n"
                + "  ],\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"string\": {\r\n"
                + "      \"properties\": {\r\n"
                + "        \"type\": {\r\n"
                + "          \"type\": \"string\"\r\n"
                + "        },\r\n"
                + "        \"value\": {\r\n"
                + "          \"type\": \"string\"\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    },\r\n"
                + "    \"number\": {\r\n"
                + "      \"properties\": {\r\n"
                + "        \"type\": {\r\n"
                + "          \"type\": \"string\"\r\n"
                + "        },\r\n"
                + "        \"value\": {\r\n"
                + "          \"type\": \"number\"\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";

        SchemaRegistry factory = SchemaRegistry.withDialect(Dialects.getOpenApi31());
        Schema schema = factory.getSchema(schemaData);

        // type=string maps to $defs/string via explicit discriminator mapping.
        // However, value=1 is a number, so $defs/string fails (value must be string).
        // $defs/number succeeds (value=1 is a valid number).
        // oneOf passes because exactly one schema matches, but it's the WRONG schema.
        // The discriminator says type=string should map to $defs/string, so this should fail.
        String inputData = "{\r\n"
                + "  \"type\": \"string\",\r\n"
                + "  \"value\": 1\r\n"
                + "}";
        List<Error> messages = schema.validate(inputData, InputFormat.JSON);
        // This should be invalid because type=string maps to $defs/string via discriminator,
        // but the data does NOT validate against $defs/string (value:1 is not a string).
        // The discriminator mapping mismatch means the wrong schema matched.
        assertEquals(1, messages.size());
        assertEquals("discriminator", messages.get(0).getKeyword());
    }

}
