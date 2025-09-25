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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.networknt.schema.AbsoluteIri;

/**
 * Tests for URI schema Loader.
 */
class IriResourceLoaderTest {
    /**
     * This test should only be run manually so as not to always hit the remote
     * server.
     * 
     * @throws IOException the exception
     */
    @Test
    @Disabled("manual")
    void shouldLoadAbsoluteIri() throws IOException {
        IriResourceLoader schemaLoader = new IriResourceLoader();
        InputStreamSource inputStreamSource = schemaLoader.getResource(AbsoluteIri.of("https://私の団体も.jp/"));
        try (InputStream inputStream = inputStreamSource.getInputStream()) {
            String result = new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .collect(Collectors.joining("\n"));
            assertNotNull(result);
        }
    }

    @Test
    void shouldNotThrowAbsoluteIri() throws IOException {
        IriResourceLoader schemaLoader = new IriResourceLoader();
        assertDoesNotThrow(() -> schemaLoader.getResource(AbsoluteIri.of("https://私の団体も.jp/")));
    }

    @Test
    void shouldThrowRelativeIri() throws IOException {
        IriResourceLoader schemaLoader = new IriResourceLoader();
        assertThrows(IllegalArgumentException.class, () -> schemaLoader.getResource(AbsoluteIri.of("私の団体も.jp/")));
    }
}
