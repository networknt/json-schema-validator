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

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.serialization.JsonMapperFactory;

class Issue877Test {
    @Test
    void test() throws Exception {
        String schemaData = "{\n"
                + "  \"type\": \"object\",\n"
                + "  \"unevaluatedProperties\": false\n"
                + "}";
        
        JsonSchemaFactory jsonSchemaFactory = JsonSchemaFactory.getInstance(VersionFlag.V202012);
        JsonSchema schema = jsonSchemaFactory.getSchema(schemaData);
        String input = "{}";
        ValidationResult result = schema.walk(JsonMapperFactory.getInstance().readTree(input), true);
        assertEquals(0, result.getValidationMessages().size());
        
        input = "";
        result = schema.walk(JsonMapperFactory.getInstance().readTree(input), true);
        assertEquals(1, result.getValidationMessages().size());
    }
}
