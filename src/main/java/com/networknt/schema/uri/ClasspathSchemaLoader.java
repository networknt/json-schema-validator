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
package com.networknt.schema.uri;

import java.io.InputStream;

import com.networknt.schema.SchemaLocation;

/**
 * Loads from classpath.
 */
public class ClasspathSchemaLoader implements SchemaLoader {

    @Override
    public InputStreamSource getSchema(SchemaLocation schemaLocation) {
        String scheme = schemaLocation.getAbsoluteIri().getScheme();
        if (scheme.startsWith("classpath") || scheme.startsWith("resource")) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = SchemaLoader.class.getClassLoader();
            }
            ClassLoader loader = classLoader;
            String name = schemaLocation.getAbsoluteIri().toString().substring(scheme.length() + 1);
            if (name.startsWith("//")) {
                name = name.substring(2);
            }
            String resource = name;
            return () -> {
                InputStream result = loader.getResourceAsStream(resource);
                if (result == null) {
                    result = loader.getResourceAsStream(resource.substring(1));
                }
                return result;
            };
        }
        return null;
    }

}
