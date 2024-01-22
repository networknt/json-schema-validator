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

import java.util.ArrayList;
import java.util.Collection;

/**
 * Schema Mappers.
 */
public class SchemaMappers extends ArrayList<SchemaMapper> {
    private static final long serialVersionUID = 1L;

    public SchemaMappers() {
        super();
    }

    public SchemaMappers(Collection<? extends SchemaMapper> c) {
        super(c);
    }

    public SchemaMappers(int initialCapacity) {
        super(initialCapacity);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private SchemaMappers values = new SchemaMappers();
        
        public SchemaMappers build() {
            return values;
        }
    }

}
