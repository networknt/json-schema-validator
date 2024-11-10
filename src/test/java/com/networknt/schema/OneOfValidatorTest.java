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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * OneOfValidatorTest.
 */
class OneOfValidatorTest {
    @Test
    void oneOfMultiple() {
        String schemaData = "{\r\n"
                + "  \"oneOf\": [\r\n"
                + "    { \r\n"
                + "      \"type\" : \"object\" ,\r\n"
                + "      \"properties\" : {\r\n"
                + "        \"hello\" : { \"type\" : \"string\" }\r\n"
                + "      },\r\n"
                + "      \"additionalProperties\" : false\r\n"
                + "    },\r\n"
                + "    { \r\n"
                + "      \"type\" : \"object\" ,\r\n"
                + "      \"properties\" : {\r\n"
                + "        \"world\" : { \"type\" : \"string\" }\r\n"
                + "      },\r\n"
                + "      \"additionalProperties\" : { \"type\" : \"string\" }\r\n"
                + "    },\r\n"
                + "    { \r\n"
                + "      \"type\" : \"object\" ,\r\n"
                + "      \"properties\" : {\r\n"
                + "        \"fox\" : { \"type\" : \"string\" }\r\n"
                + "      },\r\n"
                + "      \"additionalProperties\" : { \"type\" : \"string\" }\r\n"
                + "    }\r\n"
                + "  ]\r\n"
                + "}";
        String inputData = "{\r\n"
                + "  \"fox\" : \"test\",\r\n"
                + "  \"world\" : \"test\"\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData);
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(3, messages.size()); // even if more than 1 matches the mismatch errors are still reported
        List<ValidationMessage> assertions = messages.stream().collect(Collectors.toList());
        assertEquals("oneOf", assertions.get(0).getType());
        assertEquals("$", assertions.get(0).getInstanceLocation().toString());
        assertEquals("$.oneOf", assertions.get(0).getEvaluationPath().toString());
        assertEquals("$: must be valid to one and only one schema, but 2 are valid with indexes '1, 2'",
                assertions.get(0).getMessage());
    }

    @Test
    void oneOfZero() {
        String schemaData = "{\r\n"
                + "  \"oneOf\": [\r\n"
                + "    { \r\n"
                + "      \"type\" : \"object\" ,\r\n"
                + "      \"properties\" : {\r\n"
                + "        \"hello\" : { \"type\" : \"string\" }\r\n"
                + "      },\r\n"
                + "      \"additionalProperties\" : false\r\n"
                + "    },\r\n"
                + "    { \r\n"
                + "      \"type\" : \"object\" ,\r\n"
                + "      \"properties\" : {\r\n"
                + "        \"world\" : { \"type\" : \"string\" }\r\n"
                + "      },\r\n"
                + "      \"additionalProperties\" : { \"type\" : \"string\" }\r\n"
                + "    },\r\n"
                + "    { \r\n"
                + "      \"type\" : \"object\" ,\r\n"
                + "      \"properties\" : {\r\n"
                + "        \"fox\" : { \"type\" : \"string\" }\r\n"
                + "      },\r\n"
                + "      \"additionalProperties\" : { \"type\" : \"string\" }\r\n"
                + "    }\r\n"
                + "  ]\r\n"
                + "}";
        String inputData = "{\r\n"
                + "  \"test\" : 1\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData);
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(4, messages.size());
        List<ValidationMessage> assertions = messages.stream().collect(Collectors.toList());
        assertEquals("oneOf", assertions.get(0).getType());
        assertEquals("$", assertions.get(0).getInstanceLocation().toString());
        assertEquals("$.oneOf", assertions.get(0).getEvaluationPath().toString());
        assertEquals("$: must be valid to one and only one schema, but 0 are valid", assertions.get(0).getMessage());

        assertEquals("additionalProperties", assertions.get(1).getType());
        assertEquals("$", assertions.get(1).getInstanceLocation().toString());
        assertEquals("$.oneOf[0].additionalProperties", assertions.get(1).getEvaluationPath().toString());

        assertEquals("type", assertions.get(2).getType());
        assertEquals("$.test", assertions.get(2).getInstanceLocation().toString());
        assertEquals("$.oneOf[1].additionalProperties.type", assertions.get(2).getEvaluationPath().toString());

        assertEquals("type", assertions.get(3).getType());
        assertEquals("$.test", assertions.get(3).getInstanceLocation().toString());
        assertEquals("$.oneOf[2].additionalProperties.type", assertions.get(3).getEvaluationPath().toString());
    }

    @Test
    void invalidTypeShouldThrowJsonSchemaException() {
        String schemaData = "{\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"User\": true\r\n"
                + "  },\r\n"
                + "  \"oneOf\": {\r\n"
                + "    \"$ref\": \"#/defs/User\"\r\n"
                + "  }\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        JsonSchemaException ex = assertThrows(JsonSchemaException.class, () -> factory.getSchema(schemaData));
        assertEquals("type", ex.getValidationMessage().getMessageKey());
    }

    /**
     * This test checks that the oneOf example at
     * https://swagger.io/docs/specification/data-models/oneof-anyof-allof-not/
     * behaves according to the specification instead of the example.
     * <p>
     * https://github.com/swagger-api/swagger.io/issues/253
     * https://github.com/OAI/OpenAPI-Specification/issues/3477
     * https://github.com/networknt/json-schema-validator/issues/110
     */
    @Test
    void invalidSwaggerIoExample() {
        String document = "paths:\r\n"
                + "  /pets:\r\n"
                + "    patch:\r\n"
                + "      requestBody:\r\n"
                + "        content:\r\n"
                + "          application/json:\r\n"
                + "            schema:\r\n"
                + "              oneOf:\r\n"
                + "                - $ref: '#/components/schemas/Cat'\r\n"
                + "                - $ref: '#/components/schemas/Dog'\r\n"
                + "      responses:\r\n"
                + "        '200':\r\n"
                + "          description: Updated\r\n"
                + "components:\r\n"
                + "  schemas:\r\n"
                + "    Dog:\r\n"
                + "      type: object\r\n"
                + "      properties:\r\n"
                + "        bark:\r\n"
                + "          type: boolean\r\n"
                + "        breed:\r\n"
                + "          type: string\r\n"
                + "          enum: [Dingo, Husky, Retriever, Shepherd]\r\n"
                + "    Cat:\r\n"
                + "      type: object\r\n"
                + "      properties:\r\n"
                + "        hunts:\r\n"
                + "          type: boolean\r\n"
                + "        age:\r\n"
                + "          type: integer";
        
        JsonSchema schema = JsonSchemaFactory
                .getInstance(VersionFlag.V202012,
                        builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders
                                .schemas(Collections.singletonMap("http://example.org/example.yaml", document))))
                .getSchema(SchemaLocation.of(
                        "http://example.org/example.yaml#/paths/~1pets/patch/requestBody/content/application~1json/schema"));
        
        String example1 = "{\r\n"
                + "  \"bark\": true,\r\n"
                + "  \"breed\": \"Dingo\" \r\n"
                + "}";
        assertFalse(schema.validate(example1, InputFormat.JSON, OutputFormat.BOOLEAN));
        String example2 = "{\r\n"
                + "  \"bark\": true,\r\n"
                + "  \"hunts\": true\r\n"
                + "}";
        assertFalse(schema.validate(example2, InputFormat.JSON, OutputFormat.BOOLEAN));
        String example3 = "{\r\n"
                + "  \"bark\": true,\r\n"
                + "  \"hunts\": true,\r\n"
                + "  \"breed\": \"Husky\",\r\n"
                + "  \"age\": 3      \r\n"
                + "}";
        assertFalse(schema.validate(example3, InputFormat.JSON, OutputFormat.BOOLEAN));
    }
    
    /**
     * This test checks that the oneOf example at
     * https://swagger.io/docs/specification/data-models/oneof-anyof-allof-not/
     * behaves according to the specification instead of the example.
     * <p>
     * https://github.com/swagger-api/swagger.io/issues/253
     * https://github.com/OAI/OpenAPI-Specification/issues/3477
     * https://github.com/networknt/json-schema-validator/issues/110
     */
    @Test
    void fixedSwaggerIoExample() {
        String document = "paths:\r\n"
                + "  /pets:\r\n"
                + "    patch:\r\n"
                + "      requestBody:\r\n"
                + "        content:\r\n"
                + "          application/json:\r\n"
                + "            schema:\r\n"
                + "              oneOf:\r\n"
                + "                - $ref: '#/components/schemas/Cat'\r\n"
                + "                - $ref: '#/components/schemas/Dog'\r\n"
                + "      responses:\r\n"
                + "        '200':\r\n"
                + "          description: Updated\r\n"
                + "components:\r\n"
                + "  schemas:\r\n"
                + "    Dog:\r\n"
                + "      type: object\r\n"
                + "      properties:\r\n"
                + "        bark:\r\n"
                + "          type: boolean\r\n"
                + "        breed:\r\n"
                + "          type: string\r\n"
                + "          enum: [Dingo, Husky, Retriever, Shepherd]\r\n"
                + "      required:\r\n"
                + "        - bark\r\n"
                + "        - breed\r\n"
                + "    Cat:\r\n"
                + "      type: object\r\n"
                + "      properties:\r\n"
                + "        hunts:\r\n"
                + "          type: boolean\r\n"
                + "        age:\r\n"
                + "          type: integer\r\n"
                + "      required:\r\n"
                + "        - hunts\r\n"
                + "        - age";
        
        JsonSchema schema = JsonSchemaFactory
                .getInstance(VersionFlag.V202012,
                        builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders
                                .schemas(Collections.singletonMap("http://example.org/example.yaml", document))))
                .getSchema(SchemaLocation.of(
                        "http://example.org/example.yaml#/paths/~1pets/patch/requestBody/content/application~1json/schema"));
        
        String example1 = "{\r\n"
                + "  \"bark\": true,\r\n"
                + "  \"breed\": \"Dingo\" \r\n"
                + "}";
        assertTrue(schema.validate(example1, InputFormat.JSON, OutputFormat.BOOLEAN));
        String example2 = "{\r\n"
                + "  \"bark\": true,\r\n"
                + "  \"hunts\": true\r\n"
                + "}";
        assertFalse(schema.validate(example2, InputFormat.JSON, OutputFormat.BOOLEAN));
        String example3 = "{\r\n"
                + "  \"bark\": true,\r\n"
                + "  \"hunts\": true,\r\n"
                + "  \"breed\": \"Husky\",\r\n"
                + "  \"age\": 3      \r\n"
                + "}";
        assertFalse(schema.validate(example3, InputFormat.JSON, OutputFormat.BOOLEAN));
    }

    /**
     * Test for when the discriminator keyword is enabled but no discriminator is
     * present in the schema. This should process as a normal oneOf and return the
     * error messages.
     */
    @Test
    void oneOfDiscriminatorEnabled() {
        String schemaData = "{\r\n"
                + "  \"oneOf\": [\r\n"
                + "    {\r\n"
                + "      \"type\": \"string\"\r\n"
                + "    },\r\n"
                + "    {\r\n"
                + "      \"type\": \"number\"\r\n"
                + "    }\r\n"
                + "  ]\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData,
                SchemaValidatorsConfig.builder().discriminatorKeywordEnabled(true).build());
        String inputData = "{}";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(3, messages.size());
    }

    /**
     * Standard case where the discriminator is in the same schema as oneOf.
     * <p>
     * Note that discriminators do not affect the validation result and can only
     * affect the messages returned.
     */
    @Test
    void oneOfDiscriminatorEnabledWithDiscriminator() {
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
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData,
                SchemaValidatorsConfig.builder().discriminatorKeywordEnabled(true).build());
        // Valid
        String inputData = "{\r\n"
                + "  \"type\": \"number\",\r\n"
                + "  \"value\": 1\r\n"
                + "}";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(0, messages.size());

        // Invalid only 1 message returned for number
        String inputData2 = "{\r\n"
                + "  \"type\": \"number\",\r\n"
                + "  \"value\": {}\r\n"
                + "}";
        Set<ValidationMessage> messages2 = schema.validate(inputData2, InputFormat.JSON);
        assertEquals(2, messages2.size());

        // Invalid both messages for string and object returned
        JsonSchema schema2 = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData,
                SchemaValidatorsConfig.builder().discriminatorKeywordEnabled(false).build());
        Set<ValidationMessage> messages3 = schema2.validate(inputData2, InputFormat.JSON);
        assertEquals(3, messages3.size());
    }

    /**
     * Subclass case where the discriminator is in an allOf inside one of the oneOf references.
     * <p>
     * Note that discriminators do not affect the validation result and can only
     * affect the messages returned.
     */
    @Test
    void oneOfDiscriminatorEnabledWithDiscriminatorInSubclass() {
        String schemaData = "{\r\n"
                + "  \"oneOf\": [\r\n"
                + "    {\r\n"
                + "      \"$ref\": \"#/$defs/string\"\r\n"
                + "    },\r\n"
                + "    {\r\n"
                + "      \"$ref\": \"#/$defs/number\"\r\n"
                + "    }\r\n"
                + "  ],\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"typed\": {\r\n"
                + "      \"discriminator\": {\r\n"
                + "        \"propertyName\": \"type\",\r\n"
                + "        \"mapping\": {\r\n"
                + "          \"string\": \"#/$defs/string\",\r\n"
                + "          \"number\": \"#/$defs/number\"\r\n"
                + "        }\r\n"
                + "      }\r\n"
                + "    },\r\n"
                + "    \"string\": {\r\n"
                + "      \"allOf\": [\r\n"
                + "        {\r\n"
                + "          \"$ref\": \"#/$defs/typed\"\r\n"
                + "        },\r\n"
                + "        {\r\n"
                + "          \"properties\": {\r\n"
                + "            \"type\": {\r\n"
                + "              \"type\": \"string\"\r\n"
                + "            },\r\n"
                + "            \"value\": {\r\n"
                + "              \"type\": \"string\"\r\n"
                + "            }\r\n"
                + "          }\r\n"
                + "        }\r\n"
                + "      ]\r\n"
                + "    },\r\n"
                + "    \"number\": {\r\n"
                + "      \"allOf\": [\r\n"
                + "        {\r\n"
                + "          \"$ref\": \"#/$defs/typed\"\r\n"
                + "        },\r\n"
                + "        {\r\n"
                + "          \"properties\": {\r\n"
                + "            \"type\": {\r\n"
                + "              \"type\": \"string\"\r\n"
                + "            },\r\n"
                + "            \"value\": {\r\n"
                + "              \"type\": \"number\"\r\n"
                + "            }\r\n"
                + "          }\r\n"
                + "        }\r\n"
                + "      ]\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData,
                SchemaValidatorsConfig.builder().discriminatorKeywordEnabled(true).build());
        // Valid
        String inputData = "{\r\n"
                + "  \"type\": \"number\",\r\n"
                + "  \"value\": 1\r\n"
                + "}";
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(0, messages.size());

        // Invalid only 1 message returned for number
        String inputData2 = "{\r\n"
                + "  \"type\": \"number\",\r\n"
                + "  \"value\": {}\r\n"
                + "}";
        Set<ValidationMessage> messages2 = schema.validate(inputData2, InputFormat.JSON);
        assertEquals(2, messages2.size());

        // Invalid both messages for string and object returned
        JsonSchema schema2 = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData,
                SchemaValidatorsConfig.builder().discriminatorKeywordEnabled(false).build());
        Set<ValidationMessage> messages3 = schema2.validate(inputData2, InputFormat.JSON);
        assertEquals(3, messages3.size());
    }

}
