/*
 * Copyright (c) 2023 the original author or authors.
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
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Builder for {@link SchemaLoader}.
 */
public class SchemaLoaderBuilder {
    private BiFunction<List<SchemaLoader>, List<AbsoluteIriMapper>, SchemaLoader> schemaLoaderFactory = DefaultSchemaLoader::new;
    private List<SchemaLoader> schemaLoaders = new ArrayList<>();
    private List<AbsoluteIriMapper> absoluteIriMappers = new ArrayList<>();

    public SchemaLoaderBuilder() {
        this.schemaLoaders.add(new ClasspathSchemaLoader());
        this.schemaLoaders.add(new UriSchemaLoader());
    }

    public SchemaLoaderBuilder schemaLoaderFactory(
            BiFunction<List<SchemaLoader>, List<AbsoluteIriMapper>, SchemaLoader> schemaLoaderFactory) {
        this.schemaLoaderFactory = schemaLoaderFactory;
        return this;
    }

    public SchemaLoaderBuilder schemaLoaders(List<SchemaLoader> schemaLoaders) {
        this.schemaLoaders = schemaLoaders;
        return this;
    }

    public SchemaLoaderBuilder absoluteIriMappers(List<AbsoluteIriMapper> absoluteIriMappers) {
        this.absoluteIriMappers = absoluteIriMappers;
        return this;
    }

    public SchemaLoaderBuilder schemaLoaders(Consumer<List<SchemaLoader>> schemaLoaderCustomizer) {
        schemaLoaderCustomizer.accept(this.schemaLoaders);
        return this;
    }

    public SchemaLoaderBuilder absoluteIriMappers(Consumer<List<AbsoluteIriMapper>> absoluteIriCustomizer) {
        absoluteIriCustomizer.accept(this.absoluteIriMappers);
        return this;
    }

    public SchemaLoaderBuilder absoluteIriMapper(AbsoluteIriMapper absoluteIriMapper) {
        this.absoluteIriMappers.add(absoluteIriMapper);
        return this;
    }

    public SchemaLoaderBuilder schemaLoader(SchemaLoader schemaLoader) {
        this.schemaLoaders.add(0, schemaLoader);
        return this;
    }
    
    public SchemaLoaderBuilder mapPrefix(String source, String replacement) {
        this.absoluteIriMappers.add(new PrefixAbsoluteIriMapper(source, replacement));
        return this;
    }

    public SchemaLoaderBuilder map(Map<String, String> mappings) {
        this.absoluteIriMappers.add(new MapAbsoluteIriMapper(mappings));
        return this;
    }

    public SchemaLoader build() {
        return schemaLoaderFactory.apply(this.schemaLoaders, this.absoluteIriMappers);
    }

}
