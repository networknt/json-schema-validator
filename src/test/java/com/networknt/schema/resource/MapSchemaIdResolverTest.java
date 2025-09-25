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

import org.junit.jupiter.api.Test;

import com.networknt.schema.AbsoluteIri;

class MapSchemaIdResolverTest {

    @Test
    void predicateMapping() {
        MapSchemaIdResolver mapper = new MapSchemaIdResolver(test -> test.startsWith("http://www.example.org/"),
                original -> original.replaceFirst("http://www.example.org/", "classpath:"));
        AbsoluteIri result = mapper.resolve(AbsoluteIri.of("http://www.example.org/hello"));
        assertEquals("classpath:hello", result.toString());
        result = mapper.resolve(AbsoluteIri.of("notmatchingprefixhttp://www.example.org/hello"));
        assertNull(result);
    }

}
