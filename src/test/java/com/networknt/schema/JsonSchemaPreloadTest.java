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

import org.junit.jupiter.api.Test;

import com.networknt.schema.Specification.Version;

/**
 * Test to control preloading of schemas.
 */
class JsonSchemaPreloadTest {
    @Test
    void cacheRefsFalse() {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder().cacheRefs(false).build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(Version.DRAFT_7, builder -> builder.schemaRegistryConfig(config));
        factory.getSchema(SchemaLocation.of("classpath:/issues/1016/schema.json"));
    }

    @Test
    void preloadSchemaRefMaxNestingDepth() {
        SchemaRegistryConfig config = SchemaRegistryConfig.builder()
                .preloadSchemaRefMaxNestingDepth(20)
                .build();
        SchemaRegistry factory = SchemaRegistry.withDefaultDialect(Version.DRAFT_7, builder -> builder.schemaRegistryConfig(config));
        factory.getSchema(SchemaLocation.of("classpath:/issues/1016/schema.json"));
    }
}
