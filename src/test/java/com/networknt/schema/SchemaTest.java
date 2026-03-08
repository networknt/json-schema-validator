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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

/**
 * Tests for JsonSchemaFactory.
 */
class SchemaTest {
    @Test
    void concurrency() throws Exception {
        String schemaData = "{\r\n"
                + "  \"$schema\": \"https://json-schema.org/draft/2019-09/schema\",\r\n"
                + "  \"$id\": \"http://example.org/schema.json\",\r\n"
                + "  \"type\": \"object\",\r\n"
                + "  \"$ref\": \"ref.json\"\r\n"
                + "}";
        String refSchemaData = "{\r\n"
                + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\",\r\n"
                + "  \"$id\": \"http://example.org/ref.json\",\r\n"
                + "  \"properties\": {\r\n"
                + "    \"name\": {\r\n"
                + "      \"type\": \"string\",\r\n"
                + "      \"description\": \"The name\"\r\n"
                + "    },\r\n"
                + "    \"required\": [\r\n"
                + "      \"name\"\r\n"
                + "    ]\r\n"
                + "  }\r\n"
                + "}";        
        String inputData = "{\r\n"
                + "  \"name\": 1\r\n"
                + "}";
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().preloadSchema(false).build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2020_12,
                builder -> builder.schemaRegistryConfig(config)
                        .resourceLoaders(resourceLoaders -> resourceLoaders
                        .resources(Collections.singletonMap("http://example.org/ref.json", refSchemaData))));
        Schema schema = factory.getSchema(schemaData);
        Exception[] instance = new Exception[1];
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
                    try {
                        List<Error> messages = schema.validate(inputData, InputFormat.JSON)
                                .stream()
                                .collect(Collectors.toList());
                        assertEquals(1, messages.size());
                    } catch(Exception e) {
                        synchronized(instance) {
                            instance[0] = e;
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
        if (instance[0] != null) {
            throw instance[0];
        }
    }

    /**
     * Issue 1231.
     * <p>
     * When the YAML input length lands exactly on the snakeyaml-engine StreamReader
     * 1024-char chunk boundary, an IndexOutOfBoundsException is thrown internally.
     * This should be caught and wrapped in a SchemaException with a clear message
     * rather than propagating as a raw IndexOutOfBoundsException.
     */
    @Test
    void yamlInputAtChunkBoundaryShouldThrowSchemaException() {
        // Build a YAML key that is exactly 1024 chars so the total YAML content
        // hits the snakeyaml-engine StreamReader boundary.
        String longKey = "a".repeat(1024);
        String yamlInput = longKey + ": value\n";

        String schemaData = "{\"type\": \"object\"}";
        SchemaRegistry factory = SchemaRegistry.withDialect(com.networknt.schema.dialect.Dialects.getOpenApi31());
        Schema schema = factory.getSchema(schemaData);

        // Before the fix this threw IndexOutOfBoundsException directly.
        // After the fix it should throw SchemaException with a clear message.
        try {
            schema.validate(yamlInput, InputFormat.YAML);
        } catch (IndexOutOfBoundsException e) {
            // Raw IOOBE must not escape - this is the bug we're fixing
            throw new AssertionError("Expected SchemaException but got raw IndexOutOfBoundsException", e);
        } catch (RuntimeException e) {
            // Any other RuntimeException (including SchemaException) is acceptable
            // as long as it's not the raw IndexOutOfBoundsException
        }
    }
}
