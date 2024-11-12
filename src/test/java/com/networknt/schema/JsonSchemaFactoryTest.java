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
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * Tests for JsonSchemaFactory.
 */
class JsonSchemaFactoryTest {
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
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V202012, builder -> builder.schemaLoaders(schemaLoaders -> schemaLoaders.schemas(Collections
                .singletonMap("https://www.example.com/no-validation-no-format/schema",
                        metaSchemaData))));
        AtomicBoolean failed = new AtomicBoolean(false);
        JsonMetaSchema[] instance = new JsonMetaSchema[1];
        CountDownLatch latch = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 50; ++i) {
            Runnable runner = new Runnable() {
                public void run() {
                    SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().build();
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    JsonMetaSchema metaSchema = factory.getMetaSchema("https://www.example.com/no-validation-no-format/schema", config);
                    synchronized(instance) {
                        if (instance[0] == null) {
                            instance[0] = metaSchema;
                        }
                        // Ensure references are the same despite concurrency
                        if (!(instance[0] == metaSchema)) {
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
}
