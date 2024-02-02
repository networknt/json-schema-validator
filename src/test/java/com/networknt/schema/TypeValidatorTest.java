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

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * Test TypeValidator validator.
 */
public class TypeValidatorTest {
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
        SchemaValidatorsConfig config = new SchemaValidatorsConfig();
        config.setTypeLoose(true);
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
}
