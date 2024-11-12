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

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

class Issue940Test {
    @Test
    void shouldNotThrowException() {
        String schema = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"$ref\": \"#/$defs/greeting\",\r\n"
                + "  \"$defs\": {\r\n"
                + "    \"greeting\": {}\r\n"
                + "  }\r\n"
                + "}";
        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(VersionFlag.V7).getSchema(schema);
        assertDoesNotThrow(() -> jsonSchema.initializeValidators());
    }
}
