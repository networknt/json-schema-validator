/*
 * Copyright (c) 2026 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.node.JsonNodeFactory;

class Issue1174Test {
    @Test
    void textNodeShouldNotBeAcceptedAsRootSchema() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);

        SchemaException exception = assertThrows(SchemaException.class,
                () -> registry.getSchema(JsonNodeFactory.instance.textNode("false")));

        assertTrue(exception.getMessage().contains("must be object or boolean"));
        assertTrue(exception.getMessage().contains("STRING"));
    }

    @Test
    void textNodeContainingJsonShouldNotBeAcceptedAsRootSchema() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);

        SchemaException exception = assertThrows(SchemaException.class,
                () -> registry.getSchema(JsonNodeFactory.instance.textNode("{\"type\":\"string\"}")));

        assertTrue(exception.getMessage().contains("must be object or boolean"));
        assertTrue(exception.getMessage().contains("STRING"));
    }

    @Test
    void textNodeShouldNotBeAcceptedAsLoadedRootSchema() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7,
                builder -> builder.schemas(Collections.singletonMap("https://www.example.org/text-schema", "\"false\"")));

        SchemaException exception = assertThrows(SchemaException.class,
                () -> registry.getSchema(SchemaLocation.of("https://www.example.org/text-schema")));

        assertTrue(exception.getMessage().contains("must be object or boolean"));
        assertTrue(exception.getMessage().contains("STRING"));
    }

    @Test
    void textNodeShouldNotBeAcceptedAsReferencedRootSchema() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7,
                builder -> builder.schemas(Collections.singletonMap("https://www.example.org/text-schema", "\"false\"")));
        Schema schema = registry.getSchema("{\"$ref\":\"https://www.example.org/text-schema\"}");

        SchemaException exception = assertThrows(SchemaException.class, () -> schema.validate("42", InputFormat.JSON));

        assertTrue(exception.getMessage().contains("must be object or boolean"));
        assertTrue(exception.getMessage().contains("STRING"));
    }

    @Test
    void textNodeShouldNotBeAcceptedAsReferencedDocumentFragmentSchema() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7,
                builder -> builder.schemas(Collections.singletonMap("https://www.example.org/text-schema", "\"false\"")));
        Schema schema = registry.getSchema("{\"$ref\":\"https://www.example.org/text-schema#\"}");

        SchemaException exception = assertThrows(SchemaException.class, () -> schema.validate("42", InputFormat.JSON));

        assertTrue(exception.getMessage().contains("must be object or boolean"));
        assertTrue(exception.getMessage().contains("STRING"));
    }

    @Test
    void textNodeShouldNotBeAcceptedAsSubSchema() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);

        SchemaException exception = assertThrows(SchemaException.class,
                () -> registry.getSchema("{\"type\":\"object\",\"properties\":{\"a\":\"false\"}}"));

        assertTrue(exception.getMessage().contains("must be object or boolean"));
        assertTrue(exception.getMessage().contains("STRING"));
    }

    @Test
    void booleanSchemaShouldBeAcceptedForDraft7() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7);
        Schema schema = registry.getSchema("false");

        List<Error> errors = schema.validate("42", InputFormat.JSON);

        assertEquals(1, errors.size());
    }

    @Test
    void loadedBooleanSchemaShouldBeAcceptedForDraft7() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_7,
                builder -> builder.schemas(Collections.singletonMap("https://www.example.org/false-schema", "false")));
        Schema schema = registry.getSchema(SchemaLocation.of("https://www.example.org/false-schema"));

        List<Error> errors = schema.validate("42", InputFormat.JSON);

        assertEquals(1, errors.size());
    }

    @Test
    void booleanSchemaShouldNotBeAcceptedForDraft4() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_4);

        SchemaException exception = assertThrows(SchemaException.class, () -> registry.getSchema("false"));

        assertTrue(exception.getMessage().contains("must be object"));
        assertTrue(exception.getMessage().contains("BOOLEAN"));
    }
}
