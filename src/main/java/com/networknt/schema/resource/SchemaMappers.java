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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Schema Mappers used to map an ID indicated by an absolute IRI to a retrieval
 * IRI.
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
        private final SchemaMappers values = new SchemaMappers();

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

        /**
         * Customize the schema mappers.
         * 
         * @param customizer the customizer
         * @return the builder
         */
        public Builder values(Consumer<List<SchemaMapper>> customizer) {
            customizer.accept(this.values);
            return this;
        }

        /**
         * Adds a schema mapper.
         * 
         * @param schemaMapper the schema mapper
         * @return the builder
         */
        public Builder add(SchemaMapper schemaMapper) {
            this.values.add(schemaMapper);
            return this;
        }

        /**
         * Maps a schema given a source prefix with a replacement.
         * 
         * @param source      the source prefix
         * @param replacement the replacement prefix
         * @return the builder
         */
        public Builder mapPrefix(String source, String replacement) {
            this.values.add(new PrefixSchemaMapper(source, replacement));
            return this;
        }

        /**
         * Sets the mappings.
         * 
         * @param mappings the mappings
         * @return the builder
         */
        public Builder mappings(Map<String, String> mappings) {
            this.values.add(new MapSchemaMapper(mappings));
            return this;
        }

        /**
         * Sets the function that maps the IRI to another IRI.
         * 
         * @param mappings the mappings
         * @return the builder
         */
        public Builder mappings(Function<String, String> mappings) {
            this.values.add(new MapSchemaMapper(mappings));
            return this;
        }

        /**
         * Sets the function that maps the IRI to another IRI if the predicate is true.
         * 
         * @param test     the predicate
         * @param mappings the mappings
         * @return the builder
         */
        public Builder mappings(Predicate<String> test, Function<String, String> mappings) {
            this.values.add(new MapSchemaMapper(test, mappings));
            return this;
        }

        /**
         * Builds a {@link SchemaMappers}
         * 
         * @return the schema mappers
         */
        public SchemaMappers build() {
            return values;
        }
    }

}
