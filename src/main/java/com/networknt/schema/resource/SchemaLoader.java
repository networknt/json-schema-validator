/*
 * Copyright (c) 2025 the original author or authors.
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
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.networknt.schema.AbsoluteIri;
import com.networknt.schema.Error;
import com.networknt.schema.InvalidSchemaException;

/**
 * Schema Loader used to load the schema resource from the schema $id.
 * <p>
 * By default the SchemaLoader does not fetch remote resources. This must be
 * explicitly configured using {@link Builder#fetchRemoteResources}.
 */
public class SchemaLoader {
    private static final MetaSchemaIdResolver META_SCHEMA_ID_RESOLVER = new MetaSchemaIdResolver();
    private static final ClasspathResourceLoader CLASSPATH_RESOURCE_LOADER = new ClasspathResourceLoader();

    private static class DefaultHolder {
        private static final SchemaLoader DEFAULT = new SchemaLoader(Collections.emptyList(), Collections.emptyList());
    }

    private static class RemoteFetcher {
        private static final SchemaLoader REMOTE_FETCHER = SchemaLoader.builder().fetchRemoteResources().build();
    }

    /**
     * Gets the default schema loader.
     * <p>
     * By default this does not fetch remote resources and must be explicitly
     * configured to do so.
     * 
     * @return the default schema loader
     */
    public static SchemaLoader getDefault() {
        return DefaultHolder.DEFAULT;
    }

    /**
     * Gets the schema loader the does remote fetching.
     * 
     * @return the schema loader that does remote fetching
     */
    public static SchemaLoader getRemoteFetcher() {
        return RemoteFetcher.REMOTE_FETCHER;
    }

    protected final List<ResourceLoader> resourceLoaders;
    protected final List<SchemaIdResolver> schemaIdResolvers;
    protected final Predicate<AbsoluteIri> allow;
    protected final Predicate<AbsoluteIri> block;

    public SchemaLoader(ResourceLoader resourceLoader) {
        this(Collections.emptyList(), Collections.singletonList(resourceLoader));
    }

    public SchemaLoader(SchemaIdResolver schemaIdResolver, ResourceLoader resourceLoader) {
        this(Collections.singletonList(schemaIdResolver), Collections.singletonList(resourceLoader));
    }

    public SchemaLoader(List<SchemaIdResolver> schemaIdResolvers, List<ResourceLoader> resourceLoaders) {
        this(schemaIdResolvers, resourceLoaders, null, null);
    }

    public SchemaLoader(List<SchemaIdResolver> schemaIdResolvers, List<ResourceLoader> resourceLoaders,
            Predicate<AbsoluteIri> allow, Predicate<AbsoluteIri> block) {
        this.schemaIdResolvers = schemaIdResolvers;
        this.resourceLoaders = resourceLoaders;
        this.allow = allow;
        this.block = block;
    }

    public SchemaLoader(SchemaLoader copy) {
        this(new ArrayList<>(copy.schemaIdResolvers), new ArrayList<>(copy.resourceLoaders));
    }

    public InputStreamSource getSchemaResource(AbsoluteIri absoluteIri) {
        if (this.allow != null) {
            if (!this.allow.test(absoluteIri)) {
                throw new InvalidSchemaException(Error.builder()
                        .message("Schema from ''{0}'' is not allowed to be loaded.").arguments(absoluteIri).build());
            }
        }
        if (this.block != null) {
            if (this.block.test(absoluteIri)) {
                throw new InvalidSchemaException(Error.builder()
                        .message("Schema from ''{0}'' is not allowed to be loaded.").arguments(absoluteIri).build());
            }
        }
        AbsoluteIri mappedResult = absoluteIri;
        for (SchemaIdResolver mapper : schemaIdResolvers) {
            AbsoluteIri mapped = mapper.resolve(mappedResult);
            if (mapped != null) {
                mappedResult = mapped;
            }
        }
        AbsoluteIri mapped = resolveMetaSchemaId(absoluteIri);
        if (mapped != null) {
            mappedResult = mapped;
        }
        InputStreamSource result = getClasspathResource(mappedResult);
        if (result != null) {
            return result;
        }
        for (ResourceLoader loader : resourceLoaders) {
            result = loader.getResource(mappedResult);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    protected AbsoluteIri resolveMetaSchemaId(AbsoluteIri absoluteIri) {
        return META_SCHEMA_ID_RESOLVER.resolve(absoluteIri);
    }

    protected InputStreamSource getClasspathResource(AbsoluteIri absoluteIri) {
        return CLASSPATH_RESOURCE_LOADER.getResource(absoluteIri);
    }

    public List<ResourceLoader> getResourceLoaders() {
        return this.resourceLoaders;
    }

    public List<SchemaIdResolver> getSchemaIdResolvers() {
        return this.schemaIdResolvers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(SchemaLoader copy) {
        Builder builder = new Builder();
        if (!copy.getResourceLoaders().isEmpty()) {
            builder.resourceLoaders(r -> r.values(c -> c.addAll(copy.resourceLoaders)));
        }
        if (!copy.getSchemaIdResolvers().isEmpty()) {
            builder.schemaIdResolvers(r -> r.values(c -> c.addAll(copy.schemaIdResolvers)));
        }
        builder.allow(copy.allow);
        builder.block(copy.block);
        return builder;
    }

    public static class Builder {
        private SchemaIdResolvers.Builder schemaIdResolversBuilder = new SchemaIdResolvers.Builder();
        private ResourceLoaders.Builder resourceLoadersBuilder = new ResourceLoaders.Builder();
        private Predicate<AbsoluteIri> allow = null;
        private Predicate<AbsoluteIri> block = null;
        private boolean fetchRemoteResources = false;

        public Builder() {
        }

        public Builder schemaIdResolvers(Consumer<SchemaIdResolvers.Builder> customizer) {
            customizer.accept(schemaIdResolversBuilder);
            return this;
        }

        public Builder resourceLoaders(Consumer<ResourceLoaders.Builder> customizer) {
            customizer.accept(resourceLoadersBuilder);
            return this;
        }

        public Builder allow(Predicate<AbsoluteIri> allow) {
            this.allow = allow;
            return this;
        }

        public Builder block(Predicate<AbsoluteIri> block) {
            this.block = block;
            return this;
        }

        public Builder fetchRemoteResources(boolean fetch) {
            this.fetchRemoteResources = fetch;
            return this;
        }

        /**
         * Adds the IriResourceLoader to allow fetching of remote resources.
         *
         * @return the builder
         */
        public Builder fetchRemoteResources() {
            return fetchRemoteResources(true);
        }

        public SchemaIdResolvers.Builder getSchemaIdResolversBuilder() {
            return schemaIdResolversBuilder;
        }

        public ResourceLoaders.Builder getResourceLoadersBuilder() {
            return resourceLoadersBuilder;
        }

        public SchemaLoader build() {
            if (this.fetchRemoteResources) {
                // This ensures the IriResourceLoader is added at the end
                this.resourceLoadersBuilder.add(IriResourceLoader.getInstance());
            }
            return new SchemaLoader(schemaIdResolversBuilder.build(), resourceLoadersBuilder.build(), this.allow,
                    this.block);
        }
    }
}
