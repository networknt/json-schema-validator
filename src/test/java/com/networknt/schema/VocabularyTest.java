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

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * Tests for vocabulary support in meta schemas.
 */
public class VocabularyTest {
    @Test
    void noValidation() {
        String metaSchemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"$id\": \"https://www.example.com/no-validation-no-format/schema\",\r\n"
                + "  \"$vocabulary\": {\r\n"
                + "    \"https://www.example.com/vocab/validation\": true,\r\n"
                + "    \"https://json-schema.org/draft/2020-12/vocab/applicator\": true,\r\n"
                + "    \"https://json-schema.org/draft/2020-12/vocab/core\": true\r\n"
                + "  },\r\n"
                + "  \"allOf\": [\r\n"
                + "    { \"$ref\": \"https://json-schema.org/draft/2020-12/meta/applicator\" },\r\n"
                + "    { \"$ref\": \"https://json-schema.org/draft/2020-12/meta/core\" }\r\n"
                + "  ]\r\n"
                + "}";
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://schema/using/no/validation\",\r\n"
                + "  \"$schema\": \"https://www.example.com/no-validation-no-format/schema\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"badProperty\": false,\r\n"
                + "    \"numberProperty\": {\r\n"
                + "      \"minimum\": 10\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory
                .getInstance(VersionFlag.V202012,
                        builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(Collections
                                .singletonMap("https://www.example.com/no-validation-no-format/schema",
                                        metaSchemaData))))
                .getSchema(schemaData);

        String inputDataNoValidation = "{\r\n"
                + "  \"numberProperty\": 1\r\n"
                + "}";

        Set<ValidationMessage> messages = schema.validate(inputDataNoValidation, InputFormat.JSON);
        assertEquals(0, messages.size());

        // Set validation vocab
        schema = JsonSchemaFactory
                .getInstance(VersionFlag.V202012,
                        builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(Collections
                                .singletonMap("https://www.example.com/no-validation-no-format/schema",
                                        metaSchemaData.replace("https://www.example.com/vocab/validation",
                                                Vocabulary.V202012_VALIDATION.getId())))))
                .getSchema(schemaData);
        messages = schema.validate(inputDataNoValidation, InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("minimum", messages.iterator().next().getType());
    }

    @Test
    void noFormatValidation() {
        String metaSchemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"$id\": \"https://www.example.com/no-validation-no-format/schema\",\r\n"
                + "  \"$vocabulary\": {\r\n"
                + "    \"https://www.example.com/vocab/format\": true,\r\n"
                + "    \"https://json-schema.org/draft/2020-12/vocab/applicator\": true,\r\n"
                + "    \"https://json-schema.org/draft/2020-12/vocab/core\": true\r\n"
                + "  },\r\n"
                + "  \"allOf\": [\r\n"
                + "    { \"$ref\": \"https://json-schema.org/draft/2020-12/meta/applicator\" },\r\n"
                + "    { \"$ref\": \"https://json-schema.org/draft/2020-12/meta/core\" }\r\n"
                + "  ]\r\n"
                + "}";
        String schemaData = "{\r\n"
                + "  \"$id\": \"https://schema/using/no/format\",\r\n"
                + "  \"$schema\": \"https://www.example.com/no-validation-no-format/schema\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"dateProperty\": {\r\n"
                + "      \"format\": \"date\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        JsonSchema schema = JsonSchemaFactory
                .getInstance(VersionFlag.V202012,
                        builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(Collections
                                .singletonMap("https://www.example.com/no-validation-no-format/schema",
                                        metaSchemaData))))
                .getSchema(schemaData);

        String inputDataNoValidation = "{\r\n"
                + "  \"dateProperty\": \"hello\"\r\n"
                + "}";

        Set<ValidationMessage> messages = schema.validate(inputDataNoValidation, InputFormat.JSON,
                executionContext -> executionContext.getExecutionConfig().setFormatAssertionsEnabled(true));
        assertEquals(0, messages.size());

        // Set format assertion vocab
        schema = JsonSchemaFactory
                .getInstance(VersionFlag.V202012,
                        builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(Collections
                                .singletonMap("https://www.example.com/no-validation-no-format/schema",
                                        metaSchemaData.replace("https://www.example.com/vocab/format",
                                                Vocabulary.V202012_FORMAT_ASSERTION.getId())))))
                .getSchema(schemaData);
        messages = schema.validate(inputDataNoValidation, InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("format", messages.iterator().next().getType());
    }
}
