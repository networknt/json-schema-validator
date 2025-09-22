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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.networknt.schema.Specification.Version;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;
import com.networknt.schema.keyword.AnnotationKeyword;
import com.networknt.schema.output.OutputUnit;

/**
 * Tests for vocabulary support in meta schemas.
 */
class VocabularyTest {
    @Test
    void noValidation() {
        String metaSchemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"$id\": \"https://www.example.com/no-validation-no-format/schema\",\r\n"
                + "  \"$vocabulary\": {\r\n"
                + "    \"https://www.example.com/vocab/validation\": false,\r\n"
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
        Schema schema = SchemaRegistry
                .getInstance(Version.DRAFT_2020_12,
                        builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(Collections
                                .singletonMap("https://www.example.com/no-validation-no-format/schema",
                                        metaSchemaData))))
                .getSchema(schemaData);

        String inputDataNoValidation = "{\r\n"
                + "  \"numberProperty\": 1\r\n"
                + "}";

        List<Error> messages = schema.validate(inputDataNoValidation, InputFormat.JSON);
        assertEquals(0, messages.size());

        // Set validation vocab
        schema = SchemaRegistry
                .getInstance(Version.DRAFT_2020_12,
                        builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(Collections
                                .singletonMap("https://www.example.com/no-validation-no-format/schema",
                                        metaSchemaData.replace("https://www.example.com/vocab/validation",
                                                Vocabulary.V202012_VALIDATION.getIri())))))
                .getSchema(schemaData);
        messages = schema.validate(inputDataNoValidation, InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("minimum", messages.iterator().next().getKeyword());
        assertEquals(Version.DRAFT_2020_12, schema.getValidationContext().activeDialect().get());
    }

    @Test
    void noFormatValidation() {
        String metaSchemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2020-12/schema\",\r\n"
                + "  \"$id\": \"https://www.example.com/no-validation-no-format/schema\",\r\n"
                + "  \"$vocabulary\": {\r\n"
                + "    \"https://www.example.com/vocab/format\": false,\r\n"
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
        Schema schema = SchemaRegistry
                .getInstance(Version.DRAFT_2020_12,
                        builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(Collections
                                .singletonMap("https://www.example.com/no-validation-no-format/schema",
                                        metaSchemaData))))
                .getSchema(schemaData);

        String inputDataNoValidation = "{\r\n"
                + "  \"dateProperty\": \"hello\"\r\n"
                + "}";

        List<Error> messages = schema.validate(inputDataNoValidation, InputFormat.JSON,
                executionContext -> executionContext.getExecutionConfig().setFormatAssertionsEnabled(true));
        assertEquals(0, messages.size());

        // Set format assertion vocab
        schema = SchemaRegistry
                .getInstance(Version.DRAFT_2020_12,
                        builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(Collections
                                .singletonMap("https://www.example.com/no-validation-no-format/schema",
                                        metaSchemaData.replace("https://www.example.com/vocab/format",
                                                Vocabulary.V202012_FORMAT_ASSERTION.getIri())))))
                .getSchema(schemaData);
        messages = schema.validate(inputDataNoValidation, InputFormat.JSON);
        assertEquals(1, messages.size());
        assertEquals("format", messages.iterator().next().getKeyword());
    }
    
    @Test
    void requiredUnknownVocabulary() {
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
        SchemaRegistry factory = SchemaRegistry
                .getInstance(Version.DRAFT_2020_12,
                        builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(Collections
                                .singletonMap("https://www.example.com/no-validation-no-format/schema",
                                        metaSchemaData))));
        assertThrows(InvalidSchemaException.class, () -> factory.getSchema(schemaData));
    }
    
    @Test
    void customVocabulary() {
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
                + "  \"hello\": {\r\n"
                + "    \"dateProperty\": {\r\n"
                + "      \"format\": \"date\"\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        VocabularyFactory vocabularyFactory = uri -> {
            if ("https://www.example.com/vocab/format".equals(uri)) {
                return new Vocabulary("https://www.example.com/vocab/format", new AnnotationKeyword("hello"));
            }
            return null;
        };
        
        Dialect dialect = Dialect
                .builder(Dialects.getDraft202012().getIri(), Dialects.getDraft202012())
                .vocabularyFactory(vocabularyFactory)
                .build();
        SchemaRegistry factory = SchemaRegistry
                .getInstance(Version.DRAFT_2020_12,
                        builder -> builder.metaSchema(dialect).schemaLoaders(schemaLoaders -> schemaLoaders.schemas(Collections
                                .singletonMap("https://www.example.com/no-validation-no-format/schema",
                                        metaSchemaData))));
        Schema schema = factory.getSchema(schemaData);
        OutputUnit outputUnit = schema.validate("{}", InputFormat.JSON, OutputFormat.HIERARCHICAL, executionContext -> {
            executionContext.getExecutionConfig().setAnnotationCollectionEnabled(true);
            executionContext.getExecutionConfig().setAnnotationCollectionFilter(keyword -> true);
        });
        assertNotNull(outputUnit.getAnnotations().get("hello"));
    }
}
