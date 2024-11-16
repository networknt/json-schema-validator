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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.resource.DefaultSchemaLoader;
import com.networknt.schema.resource.SchemaLoader;
import com.networknt.schema.resource.SchemaLoaders;
import com.networknt.schema.resource.SchemaMapper;
import com.networknt.schema.resource.SchemaMappers;
import com.networknt.schema.serialization.JsonMapperFactory;
import com.networknt.schema.serialization.JsonNodeReader;
import com.networknt.schema.serialization.YamlMapperFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Factory for building {@link JsonSchema} instances. The factory should be
 * typically be created using {@link #getInstance(VersionFlag, Consumer)} and
 * should be cached for performance.
 * <p>
 * JsonSchemaFactory instances are thread-safe provided its configuration is not
 * modified.
 */
public class JsonSchemaFactory {
    private static final Logger logger = LoggerFactory.getLogger(JsonSchemaFactory.class);

    public static class Builder {
        private ObjectMapper jsonMapper = null;
        private ObjectMapper yamlMapper = null;
        private JsonNodeReader jsonNodeReader = null;
        private String defaultMetaSchemaIri;
        private final ConcurrentMap<String, JsonMetaSchema> metaSchemas = new ConcurrentHashMap<>();
        private SchemaLoaders.Builder schemaLoadersBuilder = null;
        private SchemaMappers.Builder schemaMappersBuilder = null;
        private boolean enableSchemaCache = true;
        private JsonMetaSchemaFactory metaSchemaFactory = null;

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

        /**
         * Sets the json mapper to read the data.
         * <p>
         * If the object reader is set this will not be used.
         * <p>
         * This is deprecated use an object reader instead.
         * 
         * @param jsonMapper the json mapper
         * @return the builder
         */
        @Deprecated
        public Builder jsonMapper(final ObjectMapper jsonMapper) {
            this.jsonMapper = jsonMapper;
            return this;
        }

        /**
         * Sets the yaml mapper to read the data.
         * <p>
         * If the object reader is set this will not be used.
         * <p>
         * This is deprecated use an object reader instead.
         * 
         * @param yamlMapper the yaml mapper
         * @return the builder
         */
        @Deprecated
        public Builder yamlMapper(final ObjectMapper yamlMapper) {
            this.yamlMapper = yamlMapper;
            return this;
        }

        public Builder defaultMetaSchemaIri(final String defaultMetaSchemaIri) {
            this.defaultMetaSchemaIri = defaultMetaSchemaIri;
            return this;
        }

        public Builder metaSchemaFactory(final JsonMetaSchemaFactory jsonMetaSchemaFactory) {
            this.metaSchemaFactory = jsonMetaSchemaFactory;
            return this;
        }

        public Builder metaSchema(final JsonMetaSchema jsonMetaSchema) {
            this.metaSchemas.put(normalizeMetaSchemaUri(jsonMetaSchema.getIri()), jsonMetaSchema);
            return this;
        }

        public Builder metaSchemas(final Collection<? extends JsonMetaSchema> jsonMetaSchemas) {
            for (JsonMetaSchema jsonMetaSchema : jsonMetaSchemas) {
                metaSchema(jsonMetaSchema);
            }
            return this;
        }

        public Builder metaSchemas(Consumer<Map<String, JsonMetaSchema>> customizer) {
            customizer.accept(this.metaSchemas);
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

        @Deprecated
        public Builder addMetaSchema(final JsonMetaSchema jsonMetaSchema) {
            return metaSchema(jsonMetaSchema);
        }

        @Deprecated
        public Builder addMetaSchemas(final Collection<? extends JsonMetaSchema> jsonMetaSchemas) {
            return metaSchemas(jsonMetaSchemas);
        }

        public JsonSchemaFactory build() {
            return new JsonSchemaFactory(
                    jsonMapper,
                    yamlMapper,
                    jsonNodeReader,
                    defaultMetaSchemaIri,
                    schemaLoadersBuilder,
                    schemaMappersBuilder,
                    metaSchemas,
                    enableSchemaCache,
                    metaSchemaFactory
            );
        }
    }

    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;
    private final JsonNodeReader jsonNodeReader;
    private final String defaultMetaSchemaIri;
    private final SchemaLoaders.Builder schemaLoadersBuilder;
    private final SchemaMappers.Builder schemaMappersBuilder;
    private final SchemaLoader schemaLoader;
    private final ConcurrentMap<String, JsonMetaSchema> metaSchemas;
    private final ConcurrentMap<SchemaLocation, JsonSchema> schemaCache = new ConcurrentHashMap<>();
    private final boolean enableSchemaCache;
    private final JsonMetaSchemaFactory metaSchemaFactory;
    
    private static final List<SchemaLoader> DEFAULT_SCHEMA_LOADERS = SchemaLoaders.builder().build();
    private static final List<SchemaMapper> DEFAULT_SCHEMA_MAPPERS = SchemaMappers.builder().build();

    private JsonSchemaFactory(
            ObjectMapper jsonMapper,
            ObjectMapper yamlMapper,
            JsonNodeReader jsonNodeReader,
            String defaultMetaSchemaIri,
            SchemaLoaders.Builder schemaLoadersBuilder,
            SchemaMappers.Builder schemaMappersBuilder,
            ConcurrentMap<String, JsonMetaSchema> metaSchemas,
            boolean enableSchemaCache,
            JsonMetaSchemaFactory metaSchemaFactory) {
        this.metaSchemas = metaSchemas;
        if (defaultMetaSchemaIri == null || defaultMetaSchemaIri.trim().isEmpty()) {
            throw new IllegalArgumentException("defaultMetaSchemaIri must not be null or empty");
        } else if (metaSchemas == null || metaSchemas.isEmpty()) {
            throw new IllegalArgumentException("Json Meta Schemas must not be null or empty");
        } else if (this.metaSchemas.get(normalizeMetaSchemaUri(defaultMetaSchemaIri)) == null) {
            throw new IllegalArgumentException("Meta Schema for default Meta Schema URI must be provided");
        }
        this.jsonMapper = jsonMapper;
        this.yamlMapper = yamlMapper;
        this.jsonNodeReader = jsonNodeReader;
        this.defaultMetaSchemaIri = defaultMetaSchemaIri;
        this.schemaLoadersBuilder = schemaLoadersBuilder;
        this.schemaMappersBuilder = schemaMappersBuilder;
        this.schemaLoader = new DefaultSchemaLoader(
                schemaLoadersBuilder != null ? schemaLoadersBuilder.build() : DEFAULT_SCHEMA_LOADERS,
                schemaMappersBuilder != null ? schemaMappersBuilder.build() : DEFAULT_SCHEMA_MAPPERS);
        this.enableSchemaCache = enableSchemaCache;
        this.metaSchemaFactory = metaSchemaFactory;
    }

    public SchemaLoader getSchemaLoader() {
        return this.schemaLoader;
    }

    /**
     * Builder without keywords or formats.
     *
     * Typically {@link #builder(JsonSchemaFactory)} is what is required.
     *
     * @return a builder instance without any keywords or formats - usually not what one needs.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a factory with a default schema dialect. The schema dialect will only
     * be used if the input does not specify a $schema.
     * 
     * @param versionFlag the default dialect
     * @return the factory
     */
    public static JsonSchemaFactory getInstance(SpecVersion.VersionFlag versionFlag) {
        return getInstance(versionFlag, null);
    }

    /**
     * Creates a factory with a default schema dialect. The schema dialect will only
     * be used if the input does not specify a $schema.
     * 
     * @param versionFlag the default dialect
     * @param customizer to customize the factory
     * @return the factory
     */
    public static JsonSchemaFactory getInstance(SpecVersion.VersionFlag versionFlag,
            Consumer<JsonSchemaFactory.Builder> customizer) {
        JsonSchemaVersion jsonSchemaVersion = checkVersion(versionFlag);
        JsonMetaSchema metaSchema = jsonSchemaVersion.getInstance();
        JsonSchemaFactory.Builder builder = builder().defaultMetaSchemaIri(metaSchema.getIri())
                .metaSchema(metaSchema);
        if (customizer != null) {
            customizer.accept(builder);
        }
        return builder.build();
    }

    /**
     * Gets the json schema version to get the meta schema.
     * <p>
     * This throws an {@link IllegalArgumentException} for an unsupported value.
     * 
     * @param versionFlag the schema dialect
     * @return the version
     */
    public static JsonSchemaVersion checkVersion(SpecVersion.VersionFlag versionFlag){
        if (null == versionFlag) return null;
        switch (versionFlag) {
            case V202012: return new Version202012();
            case V201909: return new Version201909();
            case V7: return new Version7();
            case V6: return new Version6();
            case V4: return new Version4();
            default: throw new IllegalArgumentException("Unsupported value" + versionFlag);
        }
    }

    /**
     * Builder from an existing {@link JsonSchemaFactory}.
     * <p>
     * <code>
     * JsonSchemaFactory.builder(JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V201909));
     * </code>
     * 
     * @param blueprint the existing factory
     * @return the builder
     */
    public static Builder builder(final JsonSchemaFactory blueprint) {
        Builder builder = builder()
                .metaSchemas(blueprint.metaSchemas.values())
                .defaultMetaSchemaIri(blueprint.defaultMetaSchemaIri)
                .jsonMapper(blueprint.jsonMapper)
                .yamlMapper(blueprint.yamlMapper)
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
     * Creates a json schema from initial input.
     * 
     * @param schemaUri the schema location
     * @param schemaNode the schema data node
     * @param config the config to use
     * @return the schema
     */
    protected JsonSchema newJsonSchema(final SchemaLocation schemaUri, final JsonNode schemaNode, final SchemaValidatorsConfig config) {
        final ValidationContext validationContext = createValidationContext(schemaNode, config);
        JsonSchema jsonSchema = doCreate(validationContext, getSchemaLocation(schemaUri),
                new JsonNodePath(validationContext.getConfig().getPathType()), schemaNode, null, false);
        preload(jsonSchema, config);
        return jsonSchema;
    }

    /**
     * Preloads the json schema if the configuration option is set.
     * 
     * @param jsonSchema the schema to preload
     * @param config containing the configuration option
     */
    private void preload(JsonSchema jsonSchema, SchemaValidatorsConfig config) {
        if (config.isPreloadJsonSchema()) {
            try {
                /*
                 * Attempt to preload and resolve $refs for performance.
                 */
                jsonSchema.initializeValidators();
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

    public JsonSchema create(ValidationContext validationContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema) {
        return doCreate(validationContext, schemaLocation, evaluationPath, schemaNode, parentSchema, false);
    }

    private JsonSchema doCreate(ValidationContext validationContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, boolean suppressSubSchemaRetrieval) {
        return JsonSchema.from(withMetaSchema(validationContext, schemaNode), schemaLocation, evaluationPath,
                schemaNode, parentSchema, suppressSubSchemaRetrieval);
    }
    
    /**
     * Determines the validation context to use for the schema given the parent
     * validation context.
     * <p>
     * This is typically the same validation context unless the schema has a
     * different $schema from the parent.
     * <p>
     * If the schema does not define a $schema, the parent should be used.
     * 
     * @param validationContext the parent validation context
     * @param schemaNode        the schema node
     * @return the validation context to use
     */
    private ValidationContext withMetaSchema(ValidationContext validationContext, JsonNode schemaNode) {
        JsonMetaSchema metaSchema = getMetaSchema(schemaNode, validationContext.getConfig());
        if (metaSchema != null && !metaSchema.getIri().equals(validationContext.getMetaSchema().getIri())) {
            SchemaValidatorsConfig config = validationContext.getConfig();
            if (metaSchema.getKeywords().containsKey("discriminator") && !config.isDiscriminatorKeywordEnabled()) {
                config = SchemaValidatorsConfig.builder(config)
                        .discriminatorKeywordEnabled(true)
                        .nullableKeywordEnabled(true)
                        .build();
            }
            return new ValidationContext(metaSchema, validationContext.getJsonSchemaFactory(), config,
                    validationContext.getSchemaReferences(), validationContext.getSchemaResources(),
                    validationContext.getDynamicAnchors());
        }
        return validationContext;
    }

    /**
     * Gets the base IRI from the schema retrieval IRI if present otherwise return
     * one with a null base IRI.
     * <p>
     * Note that the resolving of the $id or id in the schema node will take place
     * in the JsonSchema constructor.
     *
     * @param schemaLocation the schema retrieval uri
     * @return the schema location
     */
    protected SchemaLocation getSchemaLocation(SchemaLocation schemaLocation) {
        return schemaLocation != null ? schemaLocation : SchemaLocation.DOCUMENT;
    }

    protected ValidationContext createValidationContext(final JsonNode schemaNode, SchemaValidatorsConfig config) {
        final JsonMetaSchema jsonMetaSchema = getMetaSchemaOrDefault(schemaNode, config);
        SchemaValidatorsConfig configResult = config;
        if (jsonMetaSchema.getKeywords().containsKey("discriminator") && !config.isDiscriminatorKeywordEnabled()) {
            configResult = SchemaValidatorsConfig.builder(config)
                    .discriminatorKeywordEnabled(true)
                    .nullableKeywordEnabled(true)
                    .build();
        }
        return new ValidationContext(jsonMetaSchema, this, configResult);
    }

    private JsonMetaSchema getMetaSchema(final JsonNode schemaNode, SchemaValidatorsConfig config) {
        final JsonNode iriNode = schemaNode.get("$schema");
        if (iriNode != null && iriNode.isTextual()) {
            JsonMetaSchema result = metaSchemas.computeIfAbsent(normalizeMetaSchemaUri(iriNode.textValue()),
                    id -> loadMetaSchema(id, config));
            return result;
        }
        return null;
    }

    private JsonMetaSchema getMetaSchemaOrDefault(final JsonNode schemaNode, SchemaValidatorsConfig config) {
        final JsonNode iriNode = schemaNode.get("$schema");
        if (iriNode != null && !iriNode.isNull() && !iriNode.isTextual()) {
            throw new JsonSchemaException("Unknown MetaSchema: " + iriNode);
        }
        final String iri = iriNode == null || iriNode.isNull() ? defaultMetaSchemaIri : iriNode.textValue();
        return getMetaSchema(iri, config);
    }

    /**
     * Gets the meta-schema that is available to the factory.
     * 
     * @param iri    the IRI of the meta-schema
     * @param config the schema validators config
     * @return the meta-schema
     */
    public JsonMetaSchema getMetaSchema(String iri, SchemaValidatorsConfig config) {
        String key = normalizeMetaSchemaUri(iri);
        JsonMetaSchema result =  metaSchemas.computeIfAbsent(key, id -> loadMetaSchema(id, config));
        return result;
    }

    /**
     * Loads the meta-schema from the configured meta-schema factory.
     * 
     * @param iri    the IRI of the meta-schema
     * @param config the schema validators config
     * @return the meta-schema
     */
    protected JsonMetaSchema loadMetaSchema(String iri, SchemaValidatorsConfig config) {
        return this.metaSchemaFactory != null ? this.metaSchemaFactory.getMetaSchema(iri, this, config)
                : DefaultJsonMetaSchemaFactory.getInstance().getMetaSchema(iri, this, config);
    }

    JsonNode readTree(String content, InputFormat inputFormat) throws IOException {
        if (this.jsonNodeReader == null) {
            return getObjectMapper(inputFormat).readTree(content);
        } else {
            return this.jsonNodeReader.readTree(content, inputFormat);
        }
    }

    JsonNode readTree(InputStream content, InputFormat inputFormat) throws IOException {
        if (this.jsonNodeReader == null) {
            return getObjectMapper(inputFormat).readTree(content);
        } else {
            return this.jsonNodeReader.readTree(content, inputFormat);
        }
    }

    ObjectMapper getObjectMapper(InputFormat inputFormat) {
        if (InputFormat.JSON.equals(inputFormat)) {
            return getJsonMapper();
        } else if (InputFormat.YAML.equals(inputFormat)) {
            return getYamlMapper();
        }
        throw new IllegalArgumentException("Unsupported input format "+inputFormat); 
    }

    /**
     * Gets the schema.
     * <p>
     * Using this is not recommended as there is potentially no base IRI for
     * resolving references to the absolute IRI.
     * 
     * @param schema the schema data as a string
     * @param config the config
     * @return the schema
     */
    public JsonSchema getSchema(final String schema, final SchemaValidatorsConfig config) {
        return getSchema(schema, InputFormat.JSON, config);
    }

    /**
     * Gets the schema.
     * <p>
     * Using this is not recommended as there is potentially no base IRI for
     * resolving references to the absolute IRI.
     * 
     * @param schema the schema data as a string
     * @param inputFormat input format
     * @param config the config
     * @return the schema
     */
    public JsonSchema getSchema(final String schema, InputFormat inputFormat, final SchemaValidatorsConfig config) {
        try {
            final JsonNode schemaNode = readTree(schema, inputFormat);
            return newJsonSchema(null, schemaNode, config);
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new JsonSchemaException(ioe);
        }
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
    public JsonSchema getSchema(final String schema) {
        return getSchema(schema, createSchemaValidatorsConfig());
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
    public JsonSchema getSchema(final String schema, InputFormat inputFormat) {
        return getSchema(schema, inputFormat, createSchemaValidatorsConfig());
    }

    /**
     * Gets the schema.
     * <p>
     * Using this is not recommended as there is potentially no base IRI for
     * resolving references to the absolute IRI.
     * 
     * @param schemaStream the input stream with the schema data
     * @param config the config
     * @return the schema
     */
    public JsonSchema getSchema(final InputStream schemaStream, final SchemaValidatorsConfig config) {
        return getSchema(schemaStream, InputFormat.JSON, config);
    }

    /**
     * Gets the schema.
     * <p>
     * Using this is not recommended as there is potentially no base IRI for
     * resolving references to the absolute IRI.
     * 
     * @param schemaStream the input stream with the schema data
     * @param inputFormat input format
     * @param config the config
     * @return the schema
     */
    public JsonSchema getSchema(final InputStream schemaStream, InputFormat inputFormat, final SchemaValidatorsConfig config) {
        try {
            final JsonNode schemaNode = readTree(schemaStream, inputFormat);
            return newJsonSchema(null, schemaNode, config);
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new JsonSchemaException(ioe);
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
    public JsonSchema getSchema(final InputStream schemaStream) {
        return getSchema(schemaStream, createSchemaValidatorsConfig());
    }
    
    /**
     * Gets the schema.
     * 
     * @param schemaUri the absolute IRI of the schema which can map to the retrieval IRI.
     * @param config the config
     * @return the schema
     */
    public JsonSchema getSchema(final SchemaLocation schemaUri, final SchemaValidatorsConfig config) {
        JsonSchema schema = loadSchema(schemaUri, config);
        preload(schema, config);
        return schema;
    }

    /**
     * Loads the schema.
     * 
     * @param schemaUri the absolute IRI of the schema which can map to the retrieval IRI.
     * @param config the config
     * @return the schema
     */
    protected JsonSchema loadSchema(final SchemaLocation schemaUri, final SchemaValidatorsConfig config) {
        if (enableSchemaCache) {
            // ConcurrentHashMap computeIfAbsent does not allow calls that result in a
            // recursive update to the map.
            // The getMapperSchema potentially recurses to call back to getSchema again
            JsonSchema cachedUriSchema = schemaCache.get(schemaUri);
            if (cachedUriSchema == null) {
                synchronized (this) { // acquire lock on shared factory object to prevent deadlock
                    cachedUriSchema = schemaCache.get(schemaUri);
                    if (cachedUriSchema == null) {
                        cachedUriSchema = getMappedSchema(schemaUri, config);
                        if (cachedUriSchema != null) {
                            schemaCache.put(schemaUri, cachedUriSchema);
                        }
                    }
                }
            }
            return cachedUriSchema.withConfig(config);
        }
        return getMappedSchema(schemaUri, config);
    }

    ObjectMapper getYamlMapper() {
        return this.yamlMapper != null ? this.yamlMapper : YamlMapperFactory.getInstance();
    }

    ObjectMapper getJsonMapper() {
        return this.jsonMapper != null ? this.jsonMapper : JsonMapperFactory.getInstance();
    }

    /**
     * Creates a schema validators config.
     * 
     * @return the schema validators config
     */
    protected SchemaValidatorsConfig createSchemaValidatorsConfig() {
        // Remain as constructor until constructor is removed
        return new SchemaValidatorsConfig();
    }

    protected JsonSchema getMappedSchema(final SchemaLocation schemaUri, SchemaValidatorsConfig config) {
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

            final JsonMetaSchema jsonMetaSchema = getMetaSchemaOrDefault(schemaNode, config);
            JsonNodePath evaluationPath = new JsonNodePath(config.getPathType());
            if (schemaUri.getFragment() == null
                    || schemaUri.getFragment().getNameCount() == 0) {
                // Schema without fragment
                ValidationContext validationContext = new ValidationContext(jsonMetaSchema, this, config);
                return doCreate(validationContext, schemaUri, evaluationPath, schemaNode, null, true /* retrieved via id, resolving will not change anything */);
            } else {
                // Schema with fragment pointing to sub schema
                final ValidationContext validationContext = createValidationContext(schemaNode, config);
                SchemaLocation documentLocation = new SchemaLocation(schemaUri.getAbsoluteIri());
                JsonSchema document = doCreate(validationContext, documentLocation, evaluationPath, schemaNode, null, false);
                return document.getRefSchema(schemaUri.getFragment());
            }
        } catch (IOException e) {
            logger.error("Failed to load json schema from {}", schemaUri.getAbsoluteIri(), e);
            JsonSchemaException exception = new JsonSchemaException("Failed to load json schema from "+schemaUri.getAbsoluteIri());
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
    public JsonSchema getSchema(final URI schemaUri) {
        return getSchema(SchemaLocation.of(schemaUri.toString()), createSchemaValidatorsConfig());
    }

    /**
     * Gets the schema.
     * 
     * @param schemaUri the absolute IRI of the schema which can map to the retrieval IRI.
     * @param jsonNode the node
     * @param config the config
     * @return the schema
     */
    public JsonSchema getSchema(final URI schemaUri, final JsonNode jsonNode, final SchemaValidatorsConfig config) {
        return newJsonSchema(SchemaLocation.of(schemaUri.toString()), jsonNode, config);
    }

    /**
     * Gets the schema.
     * 
     * @param schemaUri the absolute IRI of the schema which can map to the retrieval IRI.
     * @param jsonNode the node
     * @return the schema
     */
    public JsonSchema getSchema(final URI schemaUri, final JsonNode jsonNode) {
        return newJsonSchema(SchemaLocation.of(schemaUri.toString()), jsonNode, createSchemaValidatorsConfig());
    }

    /**
     * Gets the schema.
     * 
     * @param schemaUri the absolute IRI of the schema which can map to the retrieval IRI.
     * @return the schema
     */
    public JsonSchema getSchema(final SchemaLocation schemaUri) {
        return getSchema(schemaUri, createSchemaValidatorsConfig());
    }

    /**
     * Gets the schema.
     * 
     * @param schemaUri the base absolute IRI
     * @param jsonNode the node
     * @param config the config
     * @return the schema
     */
    public JsonSchema getSchema(final SchemaLocation schemaUri, final JsonNode jsonNode, final SchemaValidatorsConfig config) {
        return newJsonSchema(schemaUri, jsonNode, config);
    }
    
    /**
     * Gets the schema.
     * 
     * @param schemaUri the base absolute IRI
     * @param jsonNode  the node
     * @return the schema
     */
    public JsonSchema getSchema(final SchemaLocation schemaUri, final JsonNode jsonNode) {
        return newJsonSchema(schemaUri, jsonNode, createSchemaValidatorsConfig());
    }

    /**
     * Gets the schema.
     * <p>
     * Using this is not recommended as there is potentially no base IRI for
     * resolving references to the absolute IRI.
     * <p>
     * Prefer {@link #getSchema(SchemaLocation, JsonNode, SchemaValidatorsConfig)}
     * instead to ensure the base IRI if no id is present.
     * 
     * @param jsonNode the node
     * @param config   the config
     * @return the schema
     */
    public JsonSchema getSchema(final JsonNode jsonNode, final SchemaValidatorsConfig config) {
        return newJsonSchema(null, jsonNode, config);
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
    public JsonSchema getSchema(final JsonNode jsonNode) {
        return newJsonSchema(null, jsonNode, createSchemaValidatorsConfig());
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
    static protected String normalizeMetaSchemaUri(String id) {
        boolean found = false;
        for (VersionFlag flag : SpecVersion.VersionFlag.values()) {
            if(flag.getId().equals(id)) {
                found = true;
                break;
            }
        }
        if (!found) {
            if (id.contains("://json-schema.org/draft")) {
                // unnormalized $schema
                if (id.contains("/draft-07/")) {
                    id = SchemaId.V7;
                } else if (id.contains("/draft/2019-09/")) {
                    id = SchemaId.V201909;
                } else if (id.contains("/draft/2020-12/")) {
                    id = SchemaId.V202012;
                } else if (id.contains("/draft-04/")) {
                    id = SchemaId.V4;
                } else if (id.contains("/draft-06/")) {
                    id = SchemaId.V6;
                } 
            }
        }
        return id;
    }
}
