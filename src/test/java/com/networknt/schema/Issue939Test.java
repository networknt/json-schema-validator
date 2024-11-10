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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

class Issue939Test {
    @Test
    void shouldNotThrowException() {
        String schema = "{\r\n"
                + "          \"$schema\": \"http://json-schema.org/draft-07/schema#\",\r\n"
                + "          \"type\": \"object\",\r\n"
                + "          \"additionalProperties\": false,\r\n"
                + "          \"required\": [\r\n"
                + "            \"someUuid\"\r\n"
                + "          ],\r\n"
                + "          \"properties\": {\r\n"
                + "            \"someUuid\": {\r\n"
                + "              \"$ref\": \"#/definitions/uuid\"\r\n"
                + "            }\r\n"
                + "          },\r\n"
                + "          \"definitions\": {\r\n"
                + "            \"uuid\": {\r\n"
                + "              \"type\": \"string\",\r\n"
                + "              \"pattern\": \"^[0-9a-f]{8}(\\\\\\\\-[0-9a-f]{4}){3}\\\\\\\\-[0-9a-f]{12}$\",\r\n"
                + "              \"minLength\": 36,\r\n"
                + "              \"maxLength\": 36\r\n"
                + "            }\r\n"
                + "          }\r\n"
                + "        }";
        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(VersionFlag.V7).getSchema(schema);
        assertDoesNotThrow(() -> jsonSchema.initializeValidators());
        Set<ValidationMessage> assertions = jsonSchema
                .validate("{\"someUuid\":\"invalid\"}", InputFormat.JSON);
        assertEquals(2, assertions.size());
    }
}
