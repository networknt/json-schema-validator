/*
 * Copyright (c) 2016 Network New Technologies Inc.
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

package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Specification.Version;
import com.networknt.schema.dialect.BasicDialectRegistry;
import com.networknt.schema.dialect.DefaultDialectRegistry;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.DialectId;
import com.networknt.schema.dialect.DialectRegistry;
import com.networknt.schema.resource.DefaultSchemaLoader;
import com.networknt.schema.resource.SchemaLoader;
import com.networknt.schema.resource.SchemaLoaders;
import com.networknt.schema.resource.SchemaMapper;
import com.networknt.schema.resource.SchemaMappers;
import com.networknt.schema.serialization.BasicJsonNodeReader;
import com.networknt.schema.serialization.JsonNodeReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Registry for loading and registering {@link Schema} instances.
 * <p>
 * This can be created with withDefaultDialect(Version) or withDialect(Dialect).
 * <p>
 * The registry should be cached for performance.
 * <p>
 * A different registry should be used when loading unrelated schemas.
 * <p>
 * SchemaRegistry instances are thread-safe provided its configuration is not
 * modified.
 */
public class SchemaRegistry {
    private static final Logger logger = LoggerFactory.getLogger(SchemaRegistry.class);

    public static class Builder {
        private String defaultDialectId;
        private DialectRegistry dialectRegistry = null;
        private JsonNodeReader jsonNodeReader = null;
        private SchemaLoaders.Builder schemaLoadersBuilder = null;
        private SchemaMappers.Builder schemaMappersBuilder = null;
        private boolean enableSchemaCache = true;
        private SchemaRegistryConfig schemaRegistryConfig = null;

        /**
         * Sets the json node reader to read the data.
         * <p>
         * If set this takes precedence over the configured json mapper and yaml mapper.
         * <p>
         * A location aware object reader can be created using JsonNodeReader.builder().locationAware().build().
         *
         * @param jsonNodeReader the object reader
         * @return the builder
         */
        public Builder jsonNodeReader(JsonNodeReader jsonNodeReader) {
            this.jsonNodeReader = jsonNodeReader;
            return this;
        }

        public Builder defaultDialectId(final String defaultDialectId) {
            this.defaultDialectId = defaultDialectId;
            return this;
        }

        public Builder dialectRegistry(final DialectRegistry dialectRegistry) {
            this.dialectRegistry = dialectRegistry;
            return this;
        }

        public Builder enableSchemaCache(boolean enableSchemaCache) {
            this.enableSchemaCache = enableSchemaCache;
            return this;
        }
        
        public Builder schemaLoaders(Consumer<SchemaLoaders.Builder> schemaLoadersBuilderCustomizer) {
            if (this.schemaLoadersBuilder == null) {
                this.schemaLoadersBuilder = SchemaLoaders.builder();
            }
            schemaLoadersBuilderCustomizer.accept(this.schemaLoadersBuilder);
            return this;
        }
        
        public Builder schemaMappers(Consumer<SchemaMappers.Builder> schemaMappersBuilderCustomizer) {
            if (this.schemaMappersBuilder == null) {
                this.schemaMappersBuilder = SchemaMappers.builder();
            }
            schemaMappersBuilderCustomizer.accept(this.schemaMappersBuilder);
            return this;
        }

        public Builder schemaRegistryConfig(SchemaRegistryConfig schemaRegistryConfig) {
            this.schemaRegistryConfig = schemaRegistryConfig;
            return this;
        }

        public SchemaRegistry build() {
            return new SchemaRegistry(
                    jsonNodeReader != null ? jsonNodeReader : BasicJsonNodeReader.getInstance(),
                    defaultDialectId,
                    schemaLoadersBuilder,
                    schemaMappersBuilder,
                    enableSchemaCache,
                    dialectRegistry,
                    schemaRegistryConfig
            );
        }
    }

    private final JsonNodeReader jsonNodeReader;
    private final String defaultDialectId;
    private final SchemaLoaders.Builder schemaLoadersBuilder;
    private final SchemaMappers.Builder schemaMappersBuilder;
    private final SchemaLoader schemaLoader;
    private final ConcurrentMap<SchemaLocation, Schema> schemaCache = new ConcurrentHashMap<>();
    private final boolean enableSchemaCache;
    private final DialectRegistry dialectRegistry;
    private final SchemaRegistryConfig schemaRegistryConfig;
    
    private static final List<SchemaLoader> DEFAULT_SCHEMA_LOADERS = SchemaLoaders.builder().build();
    private static final List<SchemaMapper> DEFAULT_SCHEMA_MAPPERS = SchemaMappers.builder().build();

    private SchemaRegistry(
            JsonNodeReader jsonNodeReader,
            String defaultDialectId,
            SchemaLoaders.Builder schemaLoadersBuilder,
            SchemaMappers.Builder schemaMappersBuilder,
            boolean enableSchemaCache,
            DialectRegistry dialectRegistry,
            SchemaRegistryConfig schemaRegistryConfig) {
        if (defaultDialectId == null || defaultDialectId.trim().isEmpty()) {
            throw new IllegalArgumentException("defaultDialectId must not be null or empty");
        }
        this.jsonNodeReader = jsonNodeReader;
        this.defaultDialectId = defaultDialectId;
        this.schemaLoadersBuilder = schemaLoadersBuilder;
        this.schemaMappersBuilder = schemaMappersBuilder;
        this.schemaLoader = new DefaultSchemaLoader(
                schemaLoadersBuilder != null ? schemaLoadersBuilder.build() : DEFAULT_SCHEMA_LOADERS,
                schemaMappersBuilder != null ? schemaMappersBuilder.build() : DEFAULT_SCHEMA_MAPPERS);
        this.enableSchemaCache = enableSchemaCache;
        this.dialectRegistry = dialectRegistry != null ? dialectRegistry : new DefaultDialectRegistry();
        this.schemaRegistryConfig = schemaRegistryConfig != null ? schemaRegistryConfig : SchemaRegistryConfig.getInstance();
    }

    public SchemaLoader getSchemaLoader() {
        return this.schemaLoader;
    }

    /**
     * Builder without keywords or formats.
     * <p>
     * Typically {@link #builder(SchemaRegistry)} or
     * {@link #withDefaultDialect(Version)} or {@link #withDialect(Dialect)} would be used instead.
     *
     * @return a builder instance without any keywords or formats - usually not what
     *         one needs.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new schema registry with a default schema dialect. The schema dialect
     * will only be used if the input does not specify a $schema.
     * 
     * @param specificationVersion the default dialect id corresponding to the
     *                             specification version used when the schema does
     *                             not specify the $schema keyword
     * @return the factory
     */
    public static SchemaRegistry withDefaultDialect(Specification.Version specificationVersion) {
        return withDefaultDialect(specificationVersion, null);
    }

    /**
     * Creates a new schema registry with a default schema dialect. The schema dialect
     * will only be used if the input does not specify a $schema.
     * 
     * @param specificationVersion the default dialect id corresponding to the
     *                             specification version used when the schema does
     *                             not specify the $schema keyword
     * @param customizer           to customize the registry
     * @return the factory
     */
    public static SchemaRegistry withDefaultDialect(Specification.Version specificationVersion,
            Consumer<SchemaRegistry.Builder> customizer) {
        Dialect dialect = Specification.getDialect(specificationVersion);
        return withDefaultDialectId(dialect.getId(), customizer);
    }

    /**
     * Creates a new schema registry with a default schema dialect. The schema dialect
     * will only be used if the input does not specify a $schema.
     * 
     * @param dialectId  the default dialect id used when the schema does not
     *                   specify the $schema keyword
     * @param customizer to customize the registry
     * @return the factory
     */
    public static SchemaRegistry withDefaultDialectId(String dialectId, Consumer<SchemaRegistry.Builder> customizer) {
        SchemaRegistry.Builder builder = builder().defaultDialectId(dialectId);
        if (customizer != null) {
            customizer.accept(builder);
        }
        return builder.build();
    }

    /**
     * Gets a new schema registry that supports a specific dialect only.
     * <p>
     * Schemas that do not specify dialect using $schema will use the dialect.
     * 
     * @param dialect the dialect
     * @return the schema registry
     */
    public static SchemaRegistry withDialect(Dialect dialect) {
        return withDialect(dialect, null);
    }

    /**
     * Gets a new schema registry that supports a specific dialect only.
     * <p>
     * Schemas that do not specify dialect using $schema will use the dialect.
     * 
     * @param dialect the dialect
     * @param customizer to customize the registry
     * @return the schema registry
     */
    public static SchemaRegistry withDialect(Dialect dialect, Consumer<SchemaRegistry.Builder> customizer) {
        SchemaRegistry.Builder builder = builder().defaultDialectId(dialect.getId())
                .dialectRegistry(new BasicDialectRegistry(dialect));
        if (customizer != null) {
            customizer.accept(builder);
        }
        return builder.build();
    }

    /**
     * Builder from an existing {@link SchemaRegistry}.
     * <p>
     * <code>
     * SchemaRegistry.builder(SchemaRegistry.withDefaultDialect(Specification.Version.DRAFT_2019_09));
     * </code>
     * 
     * @param blueprint the existing factory
     * @return the builder
     */
    public static Builder builder(final SchemaRegistry blueprint) {
        Builder builder = builder()
                .defaultDialectId(blueprint.defaultDialectId)
                .jsonNodeReader(blueprint.jsonNodeReader);
        if (blueprint.schemaLoadersBuilder != null) {
            builder.schemaLoadersBuilder = SchemaLoaders.builder().with(blueprint.schemaLoadersBuilder);
        }
        if (blueprint.schemaMappersBuilder != null) {
            builder.schemaMappersBuilder = SchemaMappers.builder().with(blueprint.schemaMappersBuilder);
        }
        return builder;
    }

    /**
     * Creates a schema from initial input.
     * 
     * @param schemaUri the schema location
     * @param schemaNode the schema data node
     * @return the schema
     */
    protected Schema newSchema(final SchemaLocation schemaUri, final JsonNode schemaNode) {
        final SchemaContext schemaContext = createSchemaContext(schemaNode);
        Schema jsonSchema = doCreate(schemaContext, getSchemaLocation(schemaUri),
                new NodePath(schemaContext.getSchemaRegistryConfig().getPathType()), schemaNode, null, false);
        preload(jsonSchema);
        return jsonSchema;
    }

    /**
     * Preloads the json schema if the configuration option is set.
     * 
     * @param schema the schema to preload
     * @param config containing the configuration option
     */
    private void preload(Schema schema) {
        if (this.getSchemaRegistryConfig().isPreloadSchema()) {
            try {
                /*
                 * Attempt to preload and resolve $refs for performance.
                 */
                schema.initializeValidators();
            } catch (Exception e) {
                /*
                 * Do nothing here to allow the schema to be cached even if the remote $ref
                 * cannot be resolved at this time. If the developer wants to ensure that all
                 * remote $refs are currently resolvable they need to call initializeValidators
                 * themselves.
                 */
            }
        }
    }

    public Schema create(SchemaContext schemaContext, SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema) {
        return doCreate(schemaContext, schemaLocation, evaluationPath, schemaNode, parentSchema, false);
    }

    private Schema doCreate(SchemaContext schemaContext, SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, boolean suppressSubSchemaRetrieval) {
        return Schema.from(withDialect(schemaContext, schemaNode), schemaLocation, evaluationPath,
                schemaNode, parentSchema, suppressSubSchemaRetrieval);
    }
    
    /**
     * Determines the schema context to use for the schema given the parent
     * schema context.
     * <p>
     * This is typically the same schema context unless the schema has a
     * different $schema from the parent.
     * <p>
     * If the schema does not define a $schema, the parent should be used.
     * 
     * @param schemaContext the parent schema context
     * @param schemaNode        the schema node
     * @return the schema context to use
     */
    private SchemaContext withDialect(SchemaContext schemaContext, JsonNode schemaNode) {
        Dialect dialect = getDialect(schemaNode, schemaContext.getSchemaRegistryConfig());
        if (dialect != null && !dialect.getId().equals(schemaContext.getDialect().getId())) {
            return new SchemaContext(dialect, schemaContext.getSchemaRegistry(),
                    schemaContext.getSchemaReferences(), schemaContext.getSchemaResources(),
                    schemaContext.getDynamicAnchors());
        }
        return schemaContext;
    }

    /**
     * Gets the base IRI from the schema retrieval IRI if present otherwise return
     * one with a null base IRI.
     * <p>
     * Note that the resolving of the $id or id in the schema node will take place
     * in the Schema constructor.
     *
     * @param schemaLocation the schema retrieval uri
     * @return the schema location
     */
    protected SchemaLocation getSchemaLocation(SchemaLocation schemaLocation) {
        return schemaLocation != null ? schemaLocation : SchemaLocation.DOCUMENT;
    }

    protected SchemaContext createSchemaContext(final JsonNode schemaNode) {
        final Dialect dialect = getDialectOrDefault(schemaNode);
        return new SchemaContext(dialect, this);
    }

    private Dialect getDialect(final JsonNode schemaNode, SchemaRegistryConfig config) {
        final JsonNode iriNode = schemaNode.get("$schema");
        if (iriNode != null && iriNode.isTextual()) {
            return getDialect(iriNode.textValue());
        }
        return null;
    }

    private Dialect getDialectOrDefault(final JsonNode schemaNode) {
        final JsonNode iriNode = schemaNode.get("$schema");
        if (iriNode != null && !iriNode.isNull() && !iriNode.isTextual()) {
            throw new SchemaException("Unknown dialect: " + iriNode);
        }
        final String iri = iriNode == null || iriNode.isNull() ? defaultDialectId : iriNode.textValue();
        return getDialect(iri);
    }

    /**
     * Gets the dialect that is available to the registry.
     * 
     * @param dialectId the IRI of the meta-schema
     * @return the meta-schema
     */
    public Dialect getDialect(String dialectId) {
        String key = normalizeDialectId(dialectId);
        return dialectRegistry.getDialect(key, this);
    }

    JsonNode readTree(String content, InputFormat inputFormat) throws IOException {
        return this.jsonNodeReader.readTree(content, inputFormat);
    }

    JsonNode readTree(InputStream content, InputFormat inputFormat) throws IOException {
        return this.jsonNodeReader.readTree(content, inputFormat);
    }

    /**
     * Gets the schema.
     * <p>
     * Using this is not recommended as there is potentially no base IRI for
     * resolving references to the absolute IRI.
     * 
     * @param schema the schema data as a string
     * @return the schema
     */
    public Schema getSchema(final String schema) {
        return getSchema(schema, InputFormat.JSON);
    }

    /**
     * Gets the schema.
     * <p>
     * Using this is not recommended as there is potentially no base IRI for
     * resolving references to the absolute IRI.
     * 
     * @param schema the schema data as a string
     * @param inputFormat input format
     * @return the schema
     */
    public Schema getSchema(final String schema, InputFormat inputFormat) {
        try {
            final JsonNode schemaNode = readTree(schema, inputFormat);
            return newSchema(null, schemaNode);
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new SchemaException(ioe);
        }
    }

    /**
     * Gets the schema.
     * <p>
     * Using this is not recommended as there is potentially no base IRI for
     * resolving references to the absolute IRI.
     * 
     * @param schemaStream the input stream with the schema data
     * @return the schema
     */
    public Schema getSchema(final InputStream schemaStream) {
        return getSchema(schemaStream, InputFormat.JSON);
    }

    /**
     * Gets the schema.
     * <p>
     * Using this is not recommended as there is potentially no base IRI for
     * resolving references to the absolute IRI.
     * 
     * @param schemaStream the input stream with the schema data
     * @param inputFormat input format
     * @return the schema
     */
    public Schema getSchema(final InputStream schemaStream, InputFormat inputFormat) {
        try {
            final JsonNode schemaNode = readTree(schemaStream, inputFormat);
            return newSchema(null, schemaNode);
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new SchemaException(ioe);
        }
    }

    /**
     * Gets the schema.
     * 
     * @param schemaUri the absolute IRI of the schema which can map to the retrieval IRI.
     * @return the schema
     */
    public Schema getSchema(final SchemaLocation schemaUri) {
        Schema schema = loadSchema(schemaUri);
        preload(schema);
        return schema;
    }

    /**
     * Loads the schema.
     * 
     * @param schemaUri the absolute IRI of the schema which can map to the retrieval IRI.
     * @return the schema
     */
    public Schema loadSchema(final SchemaLocation schemaUri) {
        if (enableSchemaCache) {
            // ConcurrentHashMap computeIfAbsent does not allow calls that result in a
            // recursive update to the map.
            // The getMapperSchema potentially recurses to call back to getSchema again
            Schema cachedUriSchema = schemaCache.get(schemaUri);
            if (cachedUriSchema == null) {
                synchronized (this) { // acquire lock on shared registry object to prevent deadlock
                    cachedUriSchema = schemaCache.get(schemaUri);
                    if (cachedUriSchema == null) {
                        cachedUriSchema = getMappedSchema(schemaUri);
                        if (cachedUriSchema != null) {
                            schemaCache.put(schemaUri, cachedUriSchema);
                        }
                    }
                }
            }
            return cachedUriSchema;
        }
        return getMappedSchema(schemaUri);
    }

    protected Schema getMappedSchema(final SchemaLocation schemaUri) {
        try (InputStream inputStream = this.schemaLoader.getSchema(schemaUri.getAbsoluteIri()).getInputStream()) {
            if (inputStream == null) {
                throw new IOException("Cannot load schema at " + schemaUri);
            }
            final JsonNode schemaNode;
            if (isYaml(schemaUri)) {
                schemaNode = readTree(inputStream, InputFormat.YAML);
            } else {
                schemaNode = readTree(inputStream, InputFormat.JSON);
            }

            final Dialect dialect = getDialectOrDefault(schemaNode);
            NodePath evaluationPath = new NodePath(getSchemaRegistryConfig().getPathType());
            if (schemaUri.getFragment() == null
                    || schemaUri.getFragment().getNameCount() == 0) {
                // Schema without fragment
                SchemaContext schemaContext = new SchemaContext(dialect, this);
                return doCreate(schemaContext, schemaUri, evaluationPath, schemaNode, null, true /* retrieved via id, resolving will not change anything */);
            } else {
                // Schema with fragment pointing to sub schema
                final SchemaContext schemaContext = createSchemaContext(schemaNode);
                SchemaLocation documentLocation = new SchemaLocation(schemaUri.getAbsoluteIri());
                Schema document = doCreate(schemaContext, documentLocation, evaluationPath, schemaNode, null, false);
                return document.getRefSchema(schemaUri.getFragment());
            }
        } catch (IOException e) {
            logger.error("Failed to load json schema from {}", schemaUri.getAbsoluteIri(), e);
            SchemaException exception = new SchemaException("Failed to load json schema from "+schemaUri.getAbsoluteIri());
            exception.initCause(e);
            throw exception;
        }
    }

    /**
     * Gets the schema.
     * 
     * @param schemaUri the absolute IRI of the schema which can map to the retrieval IRI.
     * @return the schema
     */
    public Schema getSchema(final URI schemaUri) {
        return getSchema(SchemaLocation.of(schemaUri.toString()));
    }

    /**
     * Gets the schema.
     * 
     * @param schemaUri the absolute IRI of the schema which can map to the retrieval IRI.
     * @param jsonNode the node
     * @return the schema
     */
    public Schema getSchema(final URI schemaUri, final JsonNode jsonNode) {
        return newSchema(SchemaLocation.of(schemaUri.toString()), jsonNode);
    }

    /**
     * Gets the schema.
     * 
     * @param schemaUri the base absolute IRI
     * @param jsonNode the node
     * @return the schema
     */
    public Schema getSchema(final SchemaLocation schemaUri, final JsonNode jsonNode) {
        return newSchema(schemaUri, jsonNode);
    }
    
    /**
     * Gets the schema.
     * <p>
     * Using this is not recommended as there is potentially no base IRI for
     * resolving references to the absolute IRI.
     * <p>
     * Prefer {@link #getSchema(SchemaLocation, JsonNode)}
     * instead to ensure the base IRI if no id is present.
     * 
     * @param jsonNode the node
     * @return the schema
     */
    public Schema getSchema(final JsonNode jsonNode) {
        return newSchema(null, jsonNode);
    }

    /**
     * Gets the schema registry config.
     *
     * @return the schema registry config
     */
    public SchemaRegistryConfig getSchemaRegistryConfig() {
        return this.schemaRegistryConfig;
    }

    private boolean isYaml(final SchemaLocation schemaUri) {
        final String schemeSpecificPart = schemaUri.getAbsoluteIri().toString();
        final int idx = schemeSpecificPart.lastIndexOf('.');

        if (idx == -1) {
            // no extension; assume json
            return false;
        }

        final String extension = schemeSpecificPart.substring(idx);
        return (".yml".equals(extension) || ".yaml".equals(extension));
    }

    /**
     * Normalizes the standard JSON schema dialects.
     * <p>
     * This should not normalize any other unrecognized dialects.
     * 
     * @param id the $schema identifier
     * @return the normalized uri
     */
    static protected String normalizeDialectId(String id) {
        boolean found = false;
        for (Version flag : Specification.Version.values()) {
            if(flag.getDialectId().equals(id)) {
                found = true;
                break;
            }
        }
        if (!found) {
            if (id.contains("://json-schema.org/draft")) {
                // unnormalized $schema
                if (id.contains("/draft-07/")) {
                    id = DialectId.DRAFT_7;
                } else if (id.contains("/draft/2019-09/")) {
                    id = DialectId.DRAFT_2019_09;
                } else if (id.contains("/draft/2020-12/")) {
                    id = DialectId.DRAFT_2020_12;
                } else if (id.contains("/draft-04/")) {
                    id = DialectId.DRAFT_4;
                } else if (id.contains("/draft-06/")) {
                    id = DialectId.DRAFT_6;
                } 
            }
        }
        return id;
    }
}
