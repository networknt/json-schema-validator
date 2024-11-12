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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.SpecVersion.VersionFlag;

class FormatKeywordFactoryTest {
    
    public static class CustomFormatKeyword extends FormatKeyword {
        public CustomFormatKeyword(Map<String, Format> formats) {
            super(formats);
        }

        @Override
        public JsonValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
            throw new IllegalArgumentException();
        }
    }
    
    @Test
    void shouldUseFormatKeyword() {
        JsonMetaSchema metaSchema = JsonMetaSchema.builder(JsonMetaSchema.getV202012())
                .formatKeywordFactory(CustomFormatKeyword::new).build();
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.metaSchema(metaSchema));
        String schemaData = "{\r\n"
                + "  \"format\": \"hello\"\r\n"
                + "}";
        assertThrows(JsonSchemaException.class, () -> factory.getSchema(schemaData));
    }
}
