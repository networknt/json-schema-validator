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

import com.networknt.schema.SpecVersion.VersionFlag;

class AllOfValidatorTest {
    @Test
    void invalidTypeShouldThrowJsonSchemaException() {
        String schemaData = "{\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"User\": true\r\n"
                + "  },\r\n"
                + "  \"allOf\": {\r\n"
                + "    \"$ref\": \"#/defs/User\"\r\n"
                + "  }\r\n"
                + "}";
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        JsonSchemaException ex = assertThrows(JsonSchemaException.class, () -> factory.getSchema(schemaData));
        assertEquals("type", ex.getValidationMessage().getMessageKey());
    }

    @Test
    void walkValidationWithNullNodeShouldNotValidate() {
        String schemaContents = "            {\r\n"
                + "                \"type\": \"object\",\r\n"
                + "                \"properties\": {\r\n"
                + "                    \"prop1\": {\r\n"
                + "                        \"allOf\": [\r\n"
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

        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        JsonSchema schema = factory.getSchema(schemaContents);
        ValidationResult result = schema.walk(jsonContents, InputFormat.JSON, true);
        assertEquals(true, result.getValidationMessages().isEmpty());
    }    
}
