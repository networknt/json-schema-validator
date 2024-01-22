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
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

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

        public Builder() {
        }

        public Builder(Builder copy) {
            this.values.addAll(copy.values);
        }

        public Builder with(Builder builder) {
            if (!builder.values.isEmpty()) {
                this.values.addAll(builder.values);
            }
            return this;
        }

        public Builder values(Consumer<List<SchemaMapper>> values) {
            values.accept(this.values);
            return this;
        }

        public Builder add(SchemaMapper schemaMapper) {
            this.values.add(schemaMapper);
            return this;
        }

        public Builder mapPrefix(String source, String replacement) {
            this.values.add(new PrefixSchemaMapper(source, replacement));
            return this;
        }

        public Builder values(Map<String, String> mappings) {
            this.values.add(new MapSchemaMapper(mappings));
            return this;
        }
        
        public Builder values(Function<String, String> mappings) {
            this.values.add(new MapSchemaMapper(mappings));
            return this;
        }

        public SchemaMappers build() {
            return values;
        }
    }

}
