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
import com.networknt.schema.dialect.BasicDialectRegistry;
import com.networknt.schema.dialect.DefaultDialectRegistry;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.dialect.DialectId;
import com.networknt.schema.dialect.DialectRegistry;
import com.networknt.schema.resource.InputStreamSource;
import com.networknt.schema.resource.ResourceLoaders;
import com.networknt.schema.resource.SchemaIdResolvers;
import com.networknt.schema.resource.SchemaLoader;
import com.networknt.schema.serialization.BasicNodeReader;
import com.networknt.schema.serialization.DefaultNodeReader;
import com.networknt.schema.serialization.NodeReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;

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
        private NodeReader nodeReader = null;
        private SchemaLoader schemaLoader = null;
        private boolean schemaCacheEnabled = true;
        private SchemaRegistryConfig schemaRegistryConfig = null;

        /**
         * Sets the json node reader to read the data.
         * <p>
         * A location aware object reader can be created using
         * NodeReader.builder().locationAware().build().
         *
         * @param nodeReader the object reader
         * @return the builder
         */
        public Builder nodeReader(NodeReader nodeReader) {
            this.nodeReader = nodeReader;
            return this;
        }

        /**
         * Sets the json node reader to read the data.
         * 
         * <pre>
         * A location aware object reader can be created using
         * schemaRegistryBuilder.nodeReader(nodeReader -&gt; nodeReader.locationAware()).
         * </pre>
         * 
         * A json ObjectMapper can be set using
         * 
         * <pre>
         * schemaRegistryBuilder.nodeReader(nodeReader -&gt; nodeReader.jsonMapper(objectMapper))
         * </pre>
         * 
         * @param customizer
         * @return the builder
         */
        public Builder nodeReader(Consumer<DefaultNodeReader.Builder> customizer) {
            DefaultNodeReader.Builder builder = NodeReader.builder();
            customizer.accept(builder);
            this.nodeReader = builder.build();
            return this;
        }

        public Builder defaultDialectId(String defaultDialectId) {
            this.defaultDialectId = defaultDialectId;
            return this;
        }

        public Builder dialectRegistry(DialectRegistry dialectRegistry) {
            this.dialectRegistry = dialectRegistry;
            return this;
        }

        public Builder schemaCacheEnabled(boolean schemaCacheEnabled) {
            this.schemaCacheEnabled = schemaCacheEnabled;
            return this;
        }

        public Builder schemaLoader(SchemaLoader schemaLoader) {
            this.schemaLoader = schemaLoader;
            return this;
        }

        public Builder schemaLoader(Consumer<SchemaLoader.Builder> customizer) {
            SchemaLoader.Builder builder = null;
            if (this.schemaLoader != null) {
                builder = SchemaLoader.builder(this.schemaLoader);
            } else {
                builder = SchemaLoader.builder();
            }
            customizer.accept(builder);
            this.schemaLoader = builder.build();
            return this;
        }

        public Builder resourceLoaders(Consumer<ResourceLoaders.Builder> customizer) {
            SchemaLoader.Builder builder = null;
            if (this.schemaLoader != null) {
                builder = SchemaLoader.builder(this.schemaLoader);
            } else {
                builder = SchemaLoader.builder();
            }
            customizer.accept(builder.getResourceLoadersBuilder());
            this.schemaLoader = builder.build();
            return this;
        }

        public Builder schemaIdResolvers(Consumer<SchemaIdResolvers.Builder> customizer) {
            SchemaLoader.Builder builder = null;
            if (this.schemaLoader != null) {
                builder = SchemaLoader.builder(this.schemaLoader);
            } else {
                builder = SchemaLoader.builder();
            }
            customizer.accept(builder.getSchemaIdResolversBuilder());
            this.schemaLoader = builder.build();
            return this;
        }

        public Builder schemaRegistryConfig(SchemaRegistryConfig schemaRegistryConfig) {
            this.schemaRegistryConfig = schemaRegistryConfig;
            return this;
        }

        /**
         * Sets the schema data by absolute IRI.
         * 
         * @param schemas the map of IRI to schema data
         * @return the builder
         */
        public Builder schemas(Map<String, String> schemas) {
            return this.resourceLoaders(resourceLoaders -> resourceLoaders.resources(schemas));
        }

        /**
         * Sets the schema data by absolute IRI function.
         * 
         * @param schemas the function that returns schema data given IRI
         * @return the builder
         */
        public Builder schemas(Function<String, String> schemas) {
            return this.resourceLoaders(resourceLoaders -> resourceLoaders.resources(schemas));
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
            return this.resourceLoaders(resourceLoaders -> resourceLoaders.resources(mapIriToObject, mapObjectToData));
        }

        public SchemaRegistry build() {
            return new SchemaRegistry(nodeReader, defaultDialectId, schemaLoader, schemaCacheEnabled,
                    dialectRegistry, schemaRegistryConfig);
        }
    }

    private final NodeReader nodeReader;
    private final String defaultDialectId;
    private final SchemaLoader schemaLoader;
    private final ConcurrentMap<SchemaLocation, Schema> schemaCache = new ConcurrentHashMap<>();
    private final boolean schemaCacheEnabled;
    private final DialectRegistry dialectRegistry;
    private final SchemaRegistryConfig schemaRegistryConfig;

    private SchemaRegistry(NodeReader nodeReader, String defaultDialectId, SchemaLoader schemaLoader,
            boolean schemaCacheEnabled, DialectRegistry dialectRegistry, SchemaRegistryConfig schemaRegistryConfig) {
        if (defaultDialectId == null || defaultDialectId.trim().isEmpty()) {
            throw new IllegalArgumentException("defaultDialectId must not be null or empty");
        }
        this.nodeReader = nodeReader != null ? nodeReader : BasicNodeReader.getInstance();
        this.defaultDialectId = defaultDialectId;
        this.schemaLoader = schemaLoader != null ? schemaLoader : SchemaLoader.getDefault();
        this.schemaCacheEnabled = schemaCacheEnabled;
        this.dialectRegistry = dialectRegistry != null ? dialectRegistry : new DefaultDialectRegistry();
        this.schemaRegistryConfig = schemaRegistryConfig != null ? schemaRegistryConfig
                : SchemaRegistryConfig.getInstance();
    }

    /**
     * Builder without keywords or formats.
     * <p>
     * Typically {@link #builder(SchemaRegistry)} or
     * {@link #withDefaultDialect(SpecificationVersion)} or
     * {@link #withDialect(Dialect)} would be used instead.
     *
     * @return a builder instance without any keywords or formats - usually not what
     *         one needs.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new schema registry with a default schema dialect. The schema
     * dialect will only be used if the input does not specify a $schema.
     * <p>
     * This uses a dialect registry that contains all the supported standard
     * specification dialects, Draft 4, Draft 6, Draft 7, Draft 2019-09 and Draft
     * 2020-12.
     * 
     * @param specificationVersion the default dialect id corresponding to the
     *                             specification version used when the schema does
     *                             not specify the $schema keyword
     * @return the factory
     */
    public static SchemaRegistry withDefaultDialect(SpecificationVersion specificationVersion) {
        return withDefaultDialect(specificationVersion, null);
    }

    /**
     * Creates a new schema registry with a default schema dialect. The schema
     * dialect will only be used if the input does not specify a $schema.
     * <p>
     * This uses a dialect registry that contains all the supported standard
     * specification dialects, Draft 4, Draft 6, Draft 7, Draft 2019-09 and Draft
     * 2020-12.
     * 
     * @param specificationVersion the default dialect id corresponding to the
     *                             specification version used when the schema does
     *                             not specify the $schema keyword
     * @param customizer           to customize the registry
     * @return the factory
     */
    public static SchemaRegistry withDefaultDialect(SpecificationVersion specificationVersion,
            Consumer<SchemaRegistry.Builder> customizer) {
        Dialect dialect = Specification.getDialect(specificationVersion);
        return withDefaultDialectId(dialect.getId(), customizer);
    }

    /**
     * Creates a new schema registry with a default schema dialect. The schema
     * dialect will only be used if the input does not specify a $schema.
     * <p>
     * This uses a dialect registry that contains all the supported standard
     * specification dialects, Draft 4, Draft 6, Draft 7, Draft 2019-09 and Draft
     * 2020-12.
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
     * Creates a new schema registry with a default schema dialect. The schema
     * dialect will only be used if the input does not specify a $schema.
     * <p>
     * This uses a dialect registry that contains all the supported standard
     * specification dialects, Draft 4, Draft 6, Draft 7, Draft 2019-09 and Draft
     * 2020-12.
     * 
     * @param dialect the default dialect used when the schema does not specify the
     *                $schema keyword
     * @return the factory
     */
    public static SchemaRegistry withDefaultDialect(Dialect dialect) {
        return withDefaultDialect(dialect, null);
    }

    /**
     * Creates a new schema registry with a default schema dialect. The schema
     * dialect will only be used if the input does not specify a $schema.
     * <p>
     * This uses a dialect registry that contains all the supported standard
     * specification dialects, Draft 4, Draft 6, Draft 7, Draft 2019-09 and Draft
     * 2020-12.
     * 
     * @param dialect    the default dialect used when the schema does not specify
     *                   the $schema keyword
     * @param customizer to customize the registry
     * @return the factory
     */
    public static SchemaRegistry withDefaultDialect(Dialect dialect, Consumer<SchemaRegistry.Builder> customizer) {
        SchemaRegistry.Builder builder = builder().defaultDialectId(dialect.getId())
                .dialectRegistry(new DefaultDialectRegistry(dialect));
        if (customizer != null) {
            customizer.accept(builder);
        }
        return builder.build();
    }

    /**
     * Gets a new schema registry that supports a specific dialect only.
     * <p>
     * Schemas that do not specify dialect using $schema will use the dialect.
     * <p>
     * This uses a dialect registry that only contains this dialect and will throw
     * an exception for unknown dialects.
     * 
     * @param dialect the dialect
     * @return the schema registry
     */
    public static SchemaRegistry withDialect(Dialect dialect) {
        return withDialect(dialect, null);
    }

    /**
     * Gets a new schema registry that supports a list of specific dialects only.
     * <p>
     * Schemas that do not specify dialect using $schema will use the first dialect
     * on the list.
     * <p>
     * This uses a dialect registry that only contains the list of dialects and will
     * throw an exception for unknown dialects.
     * 
     * @param dialect    the dialect
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
     * Gets a new schema registry that supports a list of specific dialects only.
     * <p>
     * Schemas that do not specify dialect using $schema will use the first dialect
     * on the list.
     * <p>
     * This uses a dialect registry that only contains the list of dialects and will
     * throw an exception for unknown dialects.
     * 
     * @param dialects the dialects with the first being the default dialect
     * @return the schema registry
     */
    public static SchemaRegistry withDialects(List<Dialect> dialects) {
        return withDialects(dialects, null);
    }

    /**
     * Gets a new schema registry that supports a specific dialect only.
     * <p>
     * Schemas that do not specify dialect using $schema will use the dialect.
     * <p>
     * This uses a dialect registry that only contains this dialect and will throw
     * an exception for unknown dialects.
     * 
     * @param dialects   the dialects with the first being the default dialect
     * @param customizer to customize the registry
     * @return the schema registry
     */
    public static SchemaRegistry withDialects(List<Dialect> dialects, Consumer<SchemaRegistry.Builder> customizer) {
        SchemaRegistry.Builder builder = builder().defaultDialectId(dialects.get(0).getId())
                .dialectRegistry(new BasicDialectRegistry(dialects));
        if (customizer != null) {
            customizer.accept(builder);
        }
        return builder.build();
    }


    /**
     * Builder from an existing {@link SchemaRegistry}.
     * <p>
     * <code>
     * SchemaRegistry.builder(SchemaRegistry.withDefaultDialect(SpecificationVersion.DRAFT_2019_09));
     * </code>
     * 
     * @param blueprint the existing factory
     * @return the builder
     */
    public static Builder builder(SchemaRegistry blueprint) {
        Builder builder = builder().schemaLoader(blueprint.schemaLoader)
                .defaultDialectId(blueprint.defaultDialectId)
                .nodeReader(blueprint.nodeReader)
                .dialectRegistry(blueprint.dialectRegistry)
                .schemaRegistryConfig(blueprint.schemaRegistryConfig);
        return builder;
    }

    /**
     * Gets the schema loader.
     * 
     * @return the schema loader
     */
    public SchemaLoader getSchemaLoader() {
        return this.schemaLoader;
    }

    /**
     * Creates a schema from initial input.
     * 
     * @param schemaUri  the schema location
     * @param schemaNode the schema data node
     * @return the schema
     */
    protected Schema newSchema(SchemaLocation schemaUri, JsonNode schemaNode) {
        final SchemaContext schemaContext = createSchemaContext(schemaNode);
        Schema jsonSchema = doCreate(schemaContext, getSchemaLocation(schemaUri),
                schemaNode, null, false);
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

    public Schema create(SchemaContext schemaContext, SchemaLocation schemaLocation,
            JsonNode schemaNode, Schema parentSchema) {
        return doCreate(schemaContext, schemaLocation, schemaNode, parentSchema, false);
    }

    private Schema doCreate(SchemaContext schemaContext, SchemaLocation schemaLocation,
            JsonNode schemaNode, Schema parentSchema, boolean suppressSubSchemaRetrieval) {
        return Schema.from(withDialect(schemaContext, schemaNode), schemaLocation, schemaNode,
                parentSchema, suppressSubSchemaRetrieval);
    }

    /**
     * Determines the schema context to use for the schema given the parent schema
     * context.
     * <p>
     * This is typically the same schema context unless the schema has a different
     * $schema from the parent.
     * <p>
     * If the schema does not define a $schema, the parent should be used.
     * 
     * @param schemaContext the parent schema context
     * @param schemaNode    the schema node
     * @return the schema context to use
     */
    private SchemaContext withDialect(SchemaContext schemaContext, JsonNode schemaNode) {
        Dialect dialect = getDialect(schemaNode, schemaContext.getSchemaRegistryConfig());
        if (dialect != null && !dialect.getId().equals(schemaContext.getDialect().getId())) {
            return new SchemaContext(dialect, schemaContext.getSchemaRegistry(), schemaContext.getSchemaReferences(),
                    schemaContext.getSchemaResources(), schemaContext.getDynamicAnchors());
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
        return this.nodeReader.readTree(content, inputFormat);
    }

    JsonNode readTree(InputStream content, InputFormat inputFormat) throws IOException {
        return this.nodeReader.readTree(content, inputFormat);
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
     * @param schema      the schema data as a string
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
     * @param inputFormat  input format
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
     * @param schemaUri the absolute IRI of the schema which can map to the
     *                  retrieval IRI.
     * @return the schema
     */
    public Schema getSchema(final SchemaLocation schemaUri) {
        Schema schema = loadSchema(schemaUri);
        preload(schema);
        return schema;
    }

    /**
     * Gets the schema.
     * 
     * @param schemaUri the base absolute IRI
     * @param jsonNode  the node
     * @return the schema
     */
    public Schema getSchema(final SchemaLocation schemaUri, final JsonNode jsonNode) {
        return newSchema(schemaUri, jsonNode);
    }

    /**
     * Gets the schema.
     * 
     * @param schemaUri   the base absolute IRI
     * @param schema      the input schema data
     * @param inputFormat input format
     * @return the schema
     */
    public Schema getSchema(final SchemaLocation schemaUri, final String schema, InputFormat inputFormat) {
        try {
            final JsonNode schemaNode = readTree(schema, inputFormat);
            return newSchema(schemaUri, schemaNode);
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new SchemaException(ioe);
        }
    }

    /**
     * Gets the schema.
     * 
     * @param schemaUri   the base absolute IRI
     * @param schemaStream      the input schema data
     * @param inputFormat input format
     * @return the schema
     */
    public Schema getSchema(final SchemaLocation schemaUri, final InputStream schemaStream, InputFormat inputFormat) {
        try {
            final JsonNode schemaNode = readTree(schemaStream, inputFormat);
            return newSchema(schemaUri, schemaNode);
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
     * <p>
     * Prefer {@link #getSchema(SchemaLocation, JsonNode)} instead to ensure the
     * base IRI if no id is present.
     * 
     * @param jsonNode the node
     * @return the schema
     */
    public Schema getSchema(final JsonNode jsonNode) {
        return newSchema(null, jsonNode);
    }

    /**
     * Loads the schema.
     * 
     * @param schemaUri the absolute IRI of the schema which can map to the
     *                  retrieval IRI.
     * @return the schema
     */
    public Schema loadSchema(final SchemaLocation schemaUri) {
        if (schemaCacheEnabled) {
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
        InputStreamSource inputStreamSource = this.schemaLoader.getSchemaResource(schemaUri.getAbsoluteIri());
        if (inputStreamSource != null) {
            try (InputStream inputStream = inputStreamSource.getInputStream()) {
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
                if (schemaUri.getFragment() == null || schemaUri.getFragment().getNameCount() == 0) {
                    // Schema without fragment
                    SchemaContext schemaContext = new SchemaContext(dialect, this);
                    return doCreate(schemaContext, schemaUri, schemaNode, null,
                            true /* retrieved via id, resolving will not change anything */);
                } else {
                    // Schema with fragment pointing to sub schema
                    final SchemaContext schemaContext = createSchemaContext(schemaNode);
                    SchemaLocation documentLocation = new SchemaLocation(schemaUri.getAbsoluteIri());
                    Schema document = doCreate(schemaContext, documentLocation, schemaNode, null,
                            false);
                    return document.getRefSchema(schemaUri.getFragment());
                }
            } catch (IOException e) {
                logger.error("Failed to load json schema from {}", schemaUri.getAbsoluteIri(), e);
                SchemaException exception = new SchemaException(
                        "Failed to load json schema from " + schemaUri.getAbsoluteIri());
                exception.initCause(e);
                throw exception;
            }
        } else {
            throw new SchemaException(new FileNotFoundException(schemaUri.getAbsoluteIri().toString()));
        }
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
        for (SpecificationVersion flag : SpecificationVersion.values()) {
            if (flag.getDialectId().equals(id)) {
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
