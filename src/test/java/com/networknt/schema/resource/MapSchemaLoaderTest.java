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
package com.networknt.schema.resource;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.networknt.schema.AbsoluteIri;

class MapSchemaLoaderTest {
    static class Result {
        private final String schema;

        Result(String schema) {
            this.schema = schema;
        }

        String getSchema() {
            return this.schema;
        }
    }

    @Test
    void testMappingsWithTwoFunctions() throws IOException {
        Map<String, Result> mappings = new HashMap<>();
        mappings.put("http://www.example.org/test.json", new Result("test"));
        mappings.put("http://www.example.org/hello.json", new Result("hello"));

        MapSchemaLoader loader = new MapSchemaLoader(mappings::get, Result::getSchema);
        InputStreamSource source = loader.getSchema(AbsoluteIri.of("http://www.example.org/test.json"));
        try (InputStream inputStream = source.getInputStream()) {
            byte[] r = new byte[4];
            inputStream.read(r);
            String value = new String(r, StandardCharsets.UTF_8);
            assertEquals("test", value);
        }

        InputStreamSource result = loader.getSchema(AbsoluteIri.of("http://www.example.org/not-found.json"));
        assertNull(result);
    }
}
