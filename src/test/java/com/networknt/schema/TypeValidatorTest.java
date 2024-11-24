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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * Test TypeValidator validator.
 */
class TypeValidatorTest {
    String schemaData = "{\r\n" // Issue 415
            + "  \"$schema\": \"http://json-schema.org/draft-07/schema\",\r\n"
            + "  \"$id\": \"http://example.com/example.json\",\r\n"
            + "  \"type\": \"object\",\r\n"
            + "  \"properties\": {\r\n"
            + "    \"array_of_integers\": {\r\n"
            + "      \"$id\": \"#/properties/array_of_integers\",\r\n"
            + "      \"type\": \"array\",\r\n"
            + "      \"items\": {\r\n"
            + "        \"type\": \"integer\"\r\n"
            + "      }\r\n"
            + "    },\r\n"
            + "    \"array_of_objects\": {\r\n"
            + "      \"$id\": \"#/properties/array_of_objects\",\r\n"
            + "      \"type\": \"array\",\r\n"
            + "      \"items\": {\r\n"
            + "        \"type\": \"object\"\r\n"
            + "      }\r\n"
            + "    }\r\n"
            + "  }\r\n"
            + "}";

    @Test
    void testTypeLoose() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        JsonSchema schema = factory.getSchema(schemaData);

        String inputData = "{\r\n"
                + "  \"array_of_integers\": 1,\r\n"
                + "  \"array_of_objects\": {}\r\n"
                + "}";
        String validTypeLooseInputData = "{\r\n"
                + "  \"array_of_integers\": [\"1\"],\r\n"
                + "  \"array_of_objects\": [{}]\r\n"
                + "}";
        String invalidTypeLooseData = "{\r\n"
                + "  \"array_of_integers\": \"a\",\r\n"
                + "  \"array_of_objects\": {}\r\n"
                + "}";        
        // Without type loose this has 2 type errors
        Set<ValidationMessage> messages = schema.validate(inputData, InputFormat.JSON);
        assertEquals(2, messages.size());
        assertEquals(2, messages.stream().filter(m -> "type".equals(m.getType())).count());

        // 1 type error in array_of_integers
        messages = schema.validate(validTypeLooseInputData, InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals(1, messages.stream().filter(m -> "type".equals(m.getType())).count());

        // With type loose this has 0 type errors as any item can also be interpreted as an array of 1 item
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().typeLoose(true).build();
        JsonSchema typeLoose = factory.getSchema(schemaData, config);
        messages = typeLoose.validate(inputData, InputFormat.JSON);
        assertEquals(0, messages.size());

        // No errors
        messages = typeLoose.validate(validTypeLooseInputData, InputFormat.JSON);
        assertEquals(0, messages.size());

        // Error because a string cannot be interpreted as an array of integer
        messages = typeLoose.validate(invalidTypeLooseData, InputFormat.JSON);
        assertEquals(1, messages.size());

    }

    /**
     * Issue 864.
     */
    @Test
    void integer() {
        String schemaData = "{\r\n"
                + "  \"type\": \"integer\"\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V202012).getSchema(schemaData);
        Set<ValidationMessage> messages = schema.validate("1", InputFormat.JSON);
        assertEquals(0, messages.size());
        messages = schema.validate("2.0", InputFormat.JSON);
        assertEquals(0, messages.size());
        messages = schema.validate("2.000001", InputFormat.JSON);
        assertEquals(1, messages.size());
    }

    /**
     * Issue 864.
     * <p>
     * In draft-04, "integer" is listed as a primitive type and defined as "a JSON
     * number without a fraction or exponent part"; in draft-06, "integer" is not
     * considered a primitive type and is only defined in the section for keyword
     * "type" as "any number with a zero fractional part"; 1.0 is thus not a valid
     * "integer" type in draft-04 and earlier, but is a valid "integer" type in
     * draft-06 and later; note that both drafts say that integers SHOULD be encoded
     * in JSON without fractional parts
     * 
     * @see <a href=
     *      "https://json-schema.org/draft-06/json-schema-release-notes">Draft-06
     *      Release Notes</a>
     */
    @Test
    void integerDraft4() {
        String schemaData = "{\r\n"
                + "  \"type\": \"integer\"\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V4).getSchema(schemaData);
        Set<ValidationMessage> messages = schema.validate("1", InputFormat.JSON);
        assertEquals(0, messages.size());
        // The logic in JsonNodeUtil specifically excludes V4 from this handling
        messages = schema.validate("2.0", InputFormat.JSON);
        assertEquals(1, messages.size());
        messages = schema.validate("2.000001", InputFormat.JSON);
        assertEquals(1, messages.size());
    }

    @Test
    void walkNull() {
        String schemaData = "{\r\n"
                + "  \"type\": \"integer\"\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory.getInstance(VersionFlag.V4).getSchema(schemaData);
        ValidationResult result = schema.walk(null, true);
        assertTrue(result.getValidationMessages().isEmpty());
    }

    @Test
    void nullable() {
        String schemaData = "{\r\n"
                + "   \"$schema\":\"http://json-schema.org/draft-07/schema#\",\r\n"
                + "   \"type\":\"object\",\r\n"
                + "   \"properties\":{\r\n"
                + "      \"test\":{\r\n"
                + "         \"type\":\"object\",\r\n"
                + "         \"properties\":{\r\n"
                + "            \"nested\":{\r\n"
                + "               \"type\":\"string\",\r\n"
                + "               \"nullable\":true,\r\n"
                + "               \"format\":\"date\"\r\n"
                + "            }\r\n"
                + "         }\r\n"
                + "      }\r\n"
                + "   }\r\n"
                + "}";
        String inputData = "{\r\n"
                + "  \"test\":{\r\n"
                + "      \"nested\":null\r\n"
                + "  }\r\n"
                + "}";
        final JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V7);
        final JsonSchema validator = factory.getSchema(schemaData, SchemaValidatorsConfig.builder()
            .nullableKeywordEnabled(false)
            .build());

        final Set<ValidationMessage> errors = validator.validate(inputData, InputFormat.JSON);
        assertEquals(1, errors.size());
    }
}
