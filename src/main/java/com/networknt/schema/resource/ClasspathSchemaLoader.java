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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.function.Supplier;

import com.networknt.schema.AbsoluteIri;

/**
 * Loads from classpath.
 */
public class ClasspathSchemaLoader implements SchemaLoader {
    private final Supplier<ClassLoader> classLoaderSource;

    /**
     * Constructor.
     */
    public ClasspathSchemaLoader() {
        this(ClasspathSchemaLoader::getClassLoader);
    }

    /**
     * Constructor.
     *
     * @param classLoaderSource the class loader source
     */
    public ClasspathSchemaLoader(Supplier<ClassLoader> classLoaderSource) {
        this.classLoaderSource = classLoaderSource;
    }

    @Override
    public InputStreamSource getSchema(AbsoluteIri absoluteIri) {
        String iri = absoluteIri != null ? absoluteIri.toString() : "";
        String name = null;
        if (iri.startsWith("classpath:")) {
            name = iri.substring(10);
        } else if (iri.startsWith("resource:")) {
            name = iri.substring(9);
        }
        if (name != null) {
            ClassLoader classLoader = this.classLoaderSource.get();
            if (name.startsWith("//")) {
                name = name.substring(2);
            }
            String resource = name;
            return () -> {
                InputStream result = classLoader.getResourceAsStream(resource);
                if (result == null) {
                    result = classLoader.getResourceAsStream(resource.substring(1));
                }
                if (result == null) {
                    throw new FileNotFoundException(iri);
                }
                return result;
            };
        }
        return null;
    }

    protected static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = SchemaLoader.class.getClassLoader();
        }
        return classLoader;
    }
}
