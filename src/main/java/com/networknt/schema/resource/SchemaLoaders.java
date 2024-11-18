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

/**
 * Schema Loaders used to load a schema given the retrieval IRI.
 */
public class SchemaLoaders extends ArrayList<SchemaLoader> {
    private static final long serialVersionUID = 1L;

    public SchemaLoaders() {
        super();
    }

    public SchemaLoaders(Collection<? extends SchemaLoader> c) {
        super(c);
    }

    public SchemaLoaders(int initialCapacity) {
        super(initialCapacity);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final SchemaLoaders values = new SchemaLoaders();

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
         * Customize the schema loaders.
         * 
         * @param customizer the customizer
         * @return the builder
         */
        public Builder values(Consumer<List<SchemaLoader>> customizer) {
            customizer.accept(this.values);
            return this;
        }

        /**
         * Adds a schema loader.
         * 
         * @param schemaLoader the schema loader
         * @return the builder
         */
        public Builder add(SchemaLoader schemaLoader) {
            this.values.add(schemaLoader);
            return this;
        }

        /**
         * Sets the schema data by absolute IRI.
         * 
         * @param schemas the map of IRI to schema data
         * @return the builder
         */
        public Builder schemas(Map<String, String> schemas) {
            this.values.add(new MapSchemaLoader(schemas));
            return this;
        }

        /**
         * Sets the schema data by absolute IRI function.
         * 
         * @param schemas the function that returns schema data given IRI
         * @return the builder
         */
        public Builder schemas(Function<String, String> schemas) {
            this.values.add(new MapSchemaLoader(schemas));
            return this;
        }

        /**
         * Sets the schema data by using two mapping functions.
         * <p>
         * Firstly to map the IRI to an object. If the object is null no mapping is
         * performed.
         * <p>
         * Next to map the object to the schema data.
         * 
         * @param <T>             the type of the object
         * @param mapIriToObject  the mapping of IRI to object
         * @param mapObjectToData the mappingof object to schema data
         * @return the builder
         */
        public <T> Builder schemas(Function<String, T> mapIriToObject, Function<T, String> mapObjectToData) {
            this.values.add(new MapSchemaLoader(mapIriToObject, mapObjectToData));
            return this;
        }

        /**
         * Builds a {@link SchemaLoaders}.
         * 
         * @return the schema loaders
         */
        public SchemaLoaders build() {
            return values;
        }
    }
}
