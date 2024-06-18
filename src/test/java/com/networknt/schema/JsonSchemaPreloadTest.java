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

import com.networknt.schema.SpecVersion.VersionFlag;

/**
 * Test to control preloading of schemas.
 */
public class JsonSchemaPreloadTest {
    @Test
    void cacheRefsFalse() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V7);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder().cacheRefs(false).build();
        factory.getSchema(SchemaLocation.of("classpath:/issues/1016/schema.json"), config);
    }

    @Test
    void preloadSchemaRefMaxNestingDepth() {
        JsonSchemaFactory factory = JsonSchemaFactory.getInstance(VersionFlag.V7);
        SchemaValidatorsConfig config = SchemaValidatorsConfig.builder()
                .preloadJsonSchemaRefMaxNestingDepth(20)
                .build();
        factory.getSchema(SchemaLocation.of("classpath:/issues/1016/schema.json"), config);
    }
}
