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

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * Tests for RefValidator.
 */
class RefValidatorTest {
    @Test
    void resolveSamePathDotSlash() {
        String mainSchema = "{\r\n"
                + "  \"$id\": \"https://www.example.com/schema/test.json\",\r\n"
                + "  \"$ref\": \"./integer.json\"\r\n"
                + "}";

        String otherSchema = "{\r\n"
                + "  \"type\": \"integer\"\r\n"
                + "}";

        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(
                        Collections.singletonMap("https://www.example.com/schema/integer.json", otherSchema))));
        JsonSchema jsonSchema = factory.getSchema(mainSchema);
        Set<ValidationMessage> messages = jsonSchema.validate("\"string\"", InputFormat.JSON);
        assertEquals(1, messages.size());
    }

    @Test
    void resolveSamePath() {
        String mainSchema = "{\r\n"
                + "  \"$id\": \"https://www.example.com/schema/test.json\",\r\n"
                + "  \"$ref\": \"integer.json\"\r\n"
                + "}";

        String otherSchema = "{\r\n"
                + "  \"type\": \"integer\"\r\n"
                + "}";

        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(
                        Collections.singletonMap("https://www.example.com/schema/integer.json", otherSchema))));
        JsonSchema jsonSchema = factory.getSchema(mainSchema);
        Set<ValidationMessage> messages = jsonSchema.validate("\"string\"", InputFormat.JSON);
        assertEquals(1, messages.size());
    }

    @Test
    void resolveParent() {
        String mainSchema = "{\r\n"
                + "  \"$id\": \"https://www.example.com/schema/test.json\",\r\n"
                + "  \"$ref\": \"../integer.json\"\r\n"
                + "}";
        
        String otherSchema = "{\r\n"
                + "  \"type\": \"integer\"\r\n"
                + "}";

        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(
                        Collections.singletonMap("https://www.example.com/integer.json", otherSchema))));
        JsonSchema jsonSchema = factory.getSchema(mainSchema);
        Set<ValidationMessage> messages = jsonSchema.validate("\"string\"", InputFormat.JSON);
        assertEquals(1, messages.size());
    }

    @Test
    void resolveComplex() {
        String mainSchema = "{\r\n"
                + "  \"$id\": \"https://www.example.com/schema/test.json\",\r\n"
                + "  \"$ref\": \"./hello/././world/../integer.json\"\r\n"
                + "}";
        
        String otherSchema = "{\r\n"
                + "  \"type\": \"integer\"\r\n"
                + "}";

        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012,
                builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(
                        Collections.singletonMap("https://www.example.com/schema/hello/integer.json", otherSchema))));
        JsonSchema jsonSchema = factory.getSchema(mainSchema);
        Set<ValidationMessage> messages = jsonSchema.validate("\"string\"", InputFormat.JSON);
        assertEquals(1, messages.size());
    }

    @Test
    void classPathSlash() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V201909);
        JsonSchema schema = factory.getSchema(SchemaLocation.of("classpath:/schema/main/main.json"));
        String inputData = "{\r\n"
                + "  \"fields\": {\r\n"
                + "    \"ids\": {\r\n"
                + "      \"value\": {\r\n"
                + "        \"value\": 1\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        assertFalse(schema.validate(inputData, InputFormat.JSON, OutputFormat.BOOLEAN));
    }

    @Test
    void classPathNoSlash() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V201909);
        JsonSchema schema = factory.getSchema(SchemaLocation.of("classpath:schema/main/main.json"));
        String inputData = "{\r\n"
                + "  \"fields\": {\r\n"
                + "    \"ids\": {\r\n"
                + "      \"value\": {\r\n"
                + "        \"value\": 1\r\n"
                + "      }\r\n"
                + "    }\r\n"
                + "  }\r\n"
                + "}";
        assertFalse(schema.validate(inputData, InputFormat.JSON, OutputFormat.BOOLEAN));
    }
}
