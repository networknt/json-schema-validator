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

import org.junit.jupiter.api.Test;

import com.networknt.schema.Specification.Version;

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
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(Version.DRAFT_2020_12);
        JsonSchemaException ex = assertThrows(JsonSchemaException.class, () -> factory.getSchema(schemaData));
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

        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(Specification.Version.DRAFT_7);
        Schema schema = factory.getSchema(schemaContents);
        ValidationResult result = schema.walk(jsonContents, InputFormat.JSON, true);
        assertEquals(true, result.getErrors().isEmpty());
    }
}
