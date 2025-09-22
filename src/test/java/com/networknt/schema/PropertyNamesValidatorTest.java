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

import java.util.List;

import org.junit.jupiter.api.Test;

import com.networknt.schema.Specification.Version;

/**
 * PropertyNamesValidatorTest.
 */
class PropertyNamesValidatorTest {
    /**
     * Tests that the message contains the correct values when there are invalid
     * property names.
     */
    @Test
    void messageInvalid() {
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://www.example.org/schema\",\r\n"
                + "  \"propertyNames\": {\"maxLength\": 3}\r\n"
                + "}";
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(Version.DRAFT_2020_12);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
        Schema schema = factory.getSchema(schemaData, config);
        String inputData = "{\r\n"
                + "  \"foo\": {},\r\n"
                + "  \"foobar\": {}\r\n"
                + "}";
        List<Error> messages = schema.validate(inputData, InputFormat.JSON);
        assertFalse(messages.isEmpty());
        Error message = messages.iterator().next();
        assertEquals("/propertyNames", message.getEvaluationPath().toString());
        assertEquals("https://www.example.org/schema#/propertyNames", message.getSchemaLocation().toString());
        assertEquals("", message.getInstanceLocation().toString());
        assertEquals("{\"maxLength\":3}", message.getSchemaNode().toString());
        assertEquals("{\"foo\":{},\"foobar\":{}}", message.getInstanceNode().toString());
        assertEquals(": property 'foobar' name is not valid: must be at most 3 characters long", message.toString());
        assertEquals("foobar", message.getProperty());
    }
}
