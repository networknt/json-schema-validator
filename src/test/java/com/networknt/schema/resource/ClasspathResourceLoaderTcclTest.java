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
package com.networknt.schema.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.Test;

import com.networknt.schema.AbsoluteIri;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.DialectId;
import com.networknt.schema.dialect.Dialects;

/**
 * Tests that the default {@link ClasspathResourceLoader} falls back to this library's own class
 * loader when the thread-context class loader (as in OSGi) cannot see the bundled resources.
 */
class ClasspathResourceLoaderTcclTest {

    private static final String META_SCHEMA_RESOURCE = "draft/2020-12/schema";

    /** A class loader that, like an OSGi TCCL, cannot see this library's resources. */
    private static ClassLoader blindClassLoader() {
        return new ClassLoader(null) {
            @Override
            public InputStream getResourceAsStream(String name) {
                return null;
            }
        };
    }

    private static <T> T withContextClassLoader(ClassLoader cl, Callable<T> body) throws Exception {
        Thread current = Thread.currentThread();
        ClassLoader previous = current.getContextClassLoader();
        current.setContextClassLoader(cl);
        try {
            return body.call();
        } finally {
            current.setContextClassLoader(previous);
        }
    }

    @Test
    void metaSchemaLoadsWhenContextClassLoaderCannotSeeResources() throws Exception {
        Schema metaSchema = withContextClassLoader(blindClassLoader(),
                () -> SchemaRegistry.withDefaultDialect(Dialects.getDraft202012())
                        .getSchema(SchemaLocation.of(DialectId.DRAFT_2020_12)));
        assertNotNull(metaSchema);
    }

    @Test
    void contextClassLoaderStillWinsWhenItCanSeeTheResource() throws Exception {
        byte[] sentinel = "SENTINEL".getBytes(StandardCharsets.UTF_8);
        ClassLoader sentinelLoader = new ClassLoader(null) {
            @Override
            public InputStream getResourceAsStream(String name) {
                return META_SCHEMA_RESOURCE.equals(name) ? new ByteArrayInputStream(sentinel) : null;
            }
        };
        byte[] read = withContextClassLoader(sentinelLoader, () -> {
            InputStreamSource source = ClasspathResourceLoader.getInstance()
                    .getResource(AbsoluteIri.of("classpath:" + META_SCHEMA_RESOURCE));
            try (InputStream in = source.getInputStream()) {
                byte[] buffer = new byte[sentinel.length];
                assertEquals(sentinel.length, in.read(buffer));
                return buffer;
            }
        });
        assertEquals("SENTINEL", new String(read, StandardCharsets.UTF_8));
    }

    @Test
    void explicitCustomClassLoaderSourceIsNotOverridden() {
        ClasspathResourceLoader loader = new ClasspathResourceLoader(
                ClasspathResourceLoaderTcclTest::blindClassLoader);
        InputStreamSource source = loader.getResource(AbsoluteIri.of("classpath:" + META_SCHEMA_RESOURCE));
        assertNotNull(source);
        assertThrows(FileNotFoundException.class, source::getInputStream);
    }
}
