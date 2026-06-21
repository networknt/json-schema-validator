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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.networknt.schema.dialect.BasicDialectRegistry;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.Dialects;

/**
 * Tests for JsonSchemaFactory.
 */
class SchemaRegistryTest {
    @Test
    void concurrency() {
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
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12, builder -> builder.resourceLoaders(resourceLoaders -> resourceLoaders.resources(Collections
                .singletonMap("https://www.example.com/no-validation-no-format/schema",
                        metaSchemaData))));
        AtomicBoolean failed = new AtomicBoolean(false);
        Dialect[] instance = new Dialect[1];
        CountDownLatch latch = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 50; ++i) {
            Runnable runner = new Runnable() {
                public void run() {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    Dialect dialect = factory.getDialect("https://www.example.com/no-validation-no-format/schema");
                    synchronized(instance) {
                        if (instance[0] == null) {
                            instance[0] = dialect;
                        }
                        // Ensure references are the same despite concurrency
                        if (!(instance[0] == dialect)) {
                            failed.set(true);
                        }
                    }
                }
            };
            Thread thread = new Thread(runner, "Thread" + i);
            thread.start();
            threads.add(thread);
        }
        latch.countDown(); // Release the latch for threads to run concurrently
        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        assertFalse(failed.get());
    }

    @Test
    void noDefaultDialect() {
        SchemaRegistry registry = SchemaRegistry.builder()
                .dialectRegistry(new BasicDialectRegistry(Dialects.getDraft202012())).build();
        assertThrows(MissingSchemaKeywordException.class, () -> {
            registry.getSchema("{\"type\":\"object\"}");
        });
    }

    @Test
    void noDefaultDialectButSchemaSpecified() {
        SchemaRegistry registry = SchemaRegistry.builder()
                .dialectRegistry(new BasicDialectRegistry(Dialects.getDraft202012())).build();
        assertDoesNotThrow(() -> {
            registry.getSchema("{\"$schema\":\"https://json-schema.org/draft/2020-12/schema\",\"type\":\"object\"}");
        });
    }

    @Test
    void noDefaultDialectWithDialectId() {
        SchemaRegistry registry = SchemaRegistry.withDefaultDialectId(null);
        assertThrows(MissingSchemaKeywordException.class, () -> {
            registry.getSchema("{\"type\":\"object\"}");
        });
    }

    @Test
    void noDefaultDialectButSchemaSpecifiedButNotInRegistry() {
        SchemaRegistry registry = SchemaRegistry.builder()
                .dialectRegistry(new BasicDialectRegistry(Dialects.getDraft201909())).build();
        assertThrows(InvalidSchemaException.class, () -> {
            registry.getSchema("{\"$schema\":\"https://json-schema.org/draft/2020-12/schema\",\"type\":\"object\"}");
        });
    }

    @Test
    void noDialectReferredByParentShouldDefaultToDefaultDialect() {
        String schema = "{\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"key\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"description\": \"The unique identifier or name (key) for the pair.\"\r\n"
                + "    },\r\n"
                + "    \"value\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"description\": \"The associated data (value) for the key.\"\r\n"
                + "    }\r\n"
                + "  },\r\n"
                + "  \"required\": [\r\n"
                + "    \"key\",\r\n"
                + "    \"value\"\r\n"
                + "  ],\r\n"
                + "  \"additionalProperties\": false\r\n"
                + "}";
        Map<String, String> schemas = new HashMap<>();
        schemas.put("https://example.org/schema", schema);
        SchemaRegistry registry = SchemaRegistry.withDefaultDialect(Dialects.getDraft4(), builder -> builder.schemas(schemas));
        Schema result = registry.getSchema("{\"$schema\":\"https://json-schema.org/draft/2020-12/schema\",\"type\":\"object\",\"$ref\":\"https://example.org/schema\"}");
        String input = "{\r\n"
                + "  \"key\": \"user_id\",\r\n"
                + "  \"value\": \"123456\"\r\n"
                + "}";
        result.validate(input, InputFormat.JSON);
        Schema nested = registry.getSchema(SchemaLocation.of("https://example.org/schema"));
        assertEquals(Dialects.getDraft4(), nested.getSchemaContext().getDialect());
    }
}
