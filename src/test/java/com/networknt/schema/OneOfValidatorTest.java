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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * OneOfValidatorTest.
 */
public class OneOfValidatorTest {
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
}
