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
import com.networknt.schema.resource.*;
import com.networknt.schema.serialization.JsonMapperFactory;
import com.networknt.schema.serialization.YamlMapperFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class JsonSchemaFactory {
    private static final Logger logger = LoggerFactory
            .getLogger(JsonSchemaFactory.class);


    public static class Builder {
        private ObjectMapper jsonMapper = null;
        private ObjectMapper yamlMapper = null;
        private String defaultMetaSchemaURI;
        private final ConcurrentMap<String, JsonMetaSchema> jsonMetaSchemas = new ConcurrentHashMap<String, JsonMetaSchema>();
        private SchemaLoaders.Builder schemaLoadersBuilder = null;
        private SchemaMappers.Builder schemaMappersBuilder = null;
        private boolean enableUriSchemaCache = true;

        public Builder jsonMapper(final ObjectMapper jsonMapper) {
            this.jsonMapper = jsonMapper;
            return this;
        }

        public Builder yamlMapper(final ObjectMapper yamlMapper) {
            this.yamlMapper = yamlMapper;
            return this;
        }

        public Builder defaultMetaSchemaURI(final String defaultMetaSchemaURI) {
            this.defaultMetaSchemaURI = defaultMetaSchemaURI;
            return this;
        }

        public Builder addMetaSchema(final JsonMetaSchema jsonMetaSchema) {
            this.jsonMetaSchemas.put(normalizeMetaSchemaUri(jsonMetaSchema.getUri()) , jsonMetaSchema);
            return this;
        }

        public Builder addMetaSchemas(final Collection<? extends JsonMetaSchema> jsonMetaSchemas) {
            for (JsonMetaSchema jsonMetaSchema : jsonMetaSchemas) {
                addMetaSchema(jsonMetaSchema);
            }
            return this;
        }

        public Builder enableUriSchemaCache(boolean enableUriSchemaCache) {
            this.enableUriSchemaCache = enableUriSchemaCache;
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

        public JsonSchemaFactory build() {
            // create builtin keywords with (custom) formats.
            return new JsonSchemaFactory(
                    jsonMapper,
                    yamlMapper,
                    defaultMetaSchemaURI,
                    schemaLoadersBuilder,
                    schemaMappersBuilder,
                    jsonMetaSchemas,
                    enableUriSchemaCache
            );
        }
    }

    private final ObjectMapper jsonMapper;
    private final ObjectMapper yamlMapper;
    private final String defaultMetaSchemaURI;
    private final SchemaLoaders.Builder schemaLoadersBuilder;
    private final SchemaMappers.Builder schemaMappersBuilder;
    private final SchemaLoader schemaLoader;
    private final Map<String, JsonMetaSchema> jsonMetaSchemas;
    private final ConcurrentMap<SchemaLocation, JsonSchema> uriSchemaCache = new ConcurrentHashMap<>();
    private final boolean enableUriSchemaCache;
    
    private static final List<SchemaLoader> DEFAULT_SCHEMA_LOADERS = SchemaLoaders.builder().build();
    private static final List<SchemaMapper> DEFAULT_SCHEMA_MAPPERS = SchemaMappers.builder().build();

    private JsonSchemaFactory(
            final ObjectMapper jsonMapper,
            final ObjectMapper yamlMapper,
            final String defaultMetaSchemaURI,
            SchemaLoaders.Builder schemaLoadersBuilder,
            SchemaMappers.Builder schemaMappersBuilder,
            final Map<String, JsonMetaSchema> jsonMetaSchemas,
            final boolean enableUriSchemaCache) {
        if (defaultMetaSchemaURI == null || defaultMetaSchemaURI.trim().isEmpty()) {
            throw new IllegalArgumentException("defaultMetaSchemaURI must not be null or empty");
        } else if (jsonMetaSchemas == null || jsonMetaSchemas.isEmpty()) {
            throw new IllegalArgumentException("Json Meta Schemas must not be null or empty");
        } else if (jsonMetaSchemas.get(normalizeMetaSchemaUri(defaultMetaSchemaURI)) == null) {
            throw new IllegalArgumentException("Meta Schema for default Meta Schema URI must be provided");
        }
        this.jsonMapper = jsonMapper;
        this.yamlMapper = yamlMapper;
        this.defaultMetaSchemaURI = defaultMetaSchemaURI;
        this.schemaLoadersBuilder = schemaLoadersBuilder;
        this.schemaMappersBuilder = schemaMappersBuilder;
        this.schemaLoader = new DefaultSchemaLoader(
                schemaLoadersBuilder != null ? schemaLoadersBuilder.build() : DEFAULT_SCHEMA_LOADERS,
                schemaMappersBuilder != null ? schemaMappersBuilder.build() : DEFAULT_SCHEMA_MAPPERS);
        this.jsonMetaSchemas = jsonMetaSchemas;
        this.enableUriSchemaCache = enableUriSchemaCache;
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
     * @param customizer to customze the factory
     * @return the factory
     */
    public static JsonSchemaFactory getInstance(SpecVersion.VersionFlag versionFlag,
            Consumer<JsonSchemaFactory.Builder> customizer) {
        JsonSchemaVersion jsonSchemaVersion = checkVersion(versionFlag);
        JsonMetaSchema metaSchema = jsonSchemaVersion.getInstance();
        JsonSchemaFactory.Builder builder = builder().defaultMetaSchemaURI(metaSchema.getUri())
                .addMetaSchema(metaSchema);
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
                .addMetaSchemas(blueprint.jsonMetaSchemas.values())
                .defaultMetaSchemaURI(blueprint.defaultMetaSchemaURI)
                .jsonMapper(blueprint.jsonMapper)
                .yamlMapper(blueprint.yamlMapper);
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
        return jsonSchema;
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
        if (metaSchema != null && !metaSchema.getUri().equals(validationContext.getMetaSchema().getUri())) {
            return new ValidationContext(metaSchema, validationContext.getJsonSchemaFactory(),
                    validationContext.getConfig(), validationContext.getSchemaReferences(),
                    validationContext.getSchemaResources(), validationContext.getDynamicAnchors());
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
        return new ValidationContext(jsonMetaSchema, this, config);
    }
    
    private JsonMetaSchema getMetaSchema(final JsonNode schemaNode, SchemaValidatorsConfig config) {
        final JsonNode uriNode = schemaNode.get("$schema");
        if (uriNode != null && uriNode.isTextual()) {
            return jsonMetaSchemas.computeIfAbsent(normalizeMetaSchemaUri(uriNode.textValue()), id -> getMetaSchema(id, config));
        }
        return null;
    }

    private JsonMetaSchema getMetaSchemaOrDefault(final JsonNode schemaNode, SchemaValidatorsConfig config) {
        final JsonNode uriNode = schemaNode.get("$schema");
        if (uriNode != null && !uriNode.isNull() && !uriNode.isTextual()) {
            throw new JsonSchemaException("Unknown MetaSchema: " + uriNode.toString());
        }
        final String uri = uriNode == null || uriNode.isNull() ? defaultMetaSchemaURI : normalizeMetaSchemaUri(uriNode.textValue());
        return jsonMetaSchemas.computeIfAbsent(uri, id -> getMetaSchema(id, config));
    }

    public JsonMetaSchema getMetaSchema(String id, SchemaValidatorsConfig config) {
        // Is it a well-known dialect?
        return SpecVersionDetector.detectOptionalVersion(id)
            .map(JsonSchemaFactory::checkVersion)
            .map(JsonSchemaVersion::getInstance)
            .orElseGet(() -> {
                // Custom meta schema
                return loadMetaSchema(id, config);
            });
    }

    protected JsonMetaSchema loadMetaSchema(String id, SchemaValidatorsConfig config) {
        JsonSchema schema = getSchema(SchemaLocation.of(id), config);
        JsonMetaSchema.Builder builder = JsonMetaSchema.builder(id, schema.getValidationContext().getMetaSchema());
        VersionFlag specification = schema.getValidationContext().getMetaSchema().getSpecification();
        if (specification != null) {
            if (specification.getVersionFlagValue() >= VersionFlag.V201909.getVersionFlagValue()) {
                // Process vocabularies
                JsonNode vocabulary = schema.getSchemaNode().get("$vocabulary");
                if (vocabulary != null) {
                    builder.vocabularies(new HashMap<>());
                    for(Entry<String, JsonNode> vocabs : vocabulary.properties()) {
                        builder.vocabulary(vocabs.getKey(), vocabs.getValue().booleanValue());
                    }
                }
                
            }
        }
        return builder.build();
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
        try {
            final JsonNode schemaNode = getJsonMapper().readTree(schema);
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
     * @param schemaStream the input stream with the schema data
     * @param config the config
     * @return the schema
     */
    public JsonSchema getSchema(final InputStream schemaStream, final SchemaValidatorsConfig config) {
        try {
            final JsonNode schemaNode = getJsonMapper().readTree(schemaStream);
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
        if (enableUriSchemaCache) {
            // ConcurrentHashMap computeIfAbsent does not allow calls that result in a
            // recursive update to the map.
            // The getMapperSchema potentially recurses to call back to getSchema again
            JsonSchema cachedUriSchema = uriSchemaCache.get(schemaUri);
            if (cachedUriSchema == null) {
                synchronized (this) { // acquire lock on shared factory object to prevent deadlock
                    cachedUriSchema = uriSchemaCache.get(schemaUri);
                    if (cachedUriSchema == null) {
                        cachedUriSchema = getMappedSchema(schemaUri, config);
                        if (cachedUriSchema != null) {
                            uriSchemaCache.put(schemaUri, cachedUriSchema);
                        }
                    }
                }
            }
            return cachedUriSchema.withConfig(config);
        }
        return getMappedSchema(schemaUri, config);
    }
    
    protected ObjectMapper getYamlMapper() {
        return this.yamlMapper != null ? this.yamlMapper : YamlMapperFactory.getInstance();
    }
    
    protected ObjectMapper getJsonMapper() {
        return this.jsonMapper != null ? this.jsonMapper : JsonMapperFactory.getInstance();
    }

    /**
     * Creates a schema validators config.
     * 
     * @return the schema validators config
     */
    protected SchemaValidatorsConfig createSchemaValidatorsConfig() {
        return new SchemaValidatorsConfig();
    }

    protected JsonSchema getMappedSchema(final SchemaLocation schemaUri, SchemaValidatorsConfig config) {
        try (InputStream inputStream = this.schemaLoader.getSchema(schemaUri.getAbsoluteIri()).getInputStream()) {
            if (inputStream == null) {
                throw new IOException("Cannot load schema at " + schemaUri.toString());
            }
            final JsonNode schemaNode;
            if (isYaml(schemaUri)) {
                schemaNode = getYamlMapper().readTree(inputStream);
            } else {
                schemaNode = getJsonMapper().readTree(inputStream);
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
        return newJsonSchema(schemaUri, jsonNode, null);
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
        return newJsonSchema(null, jsonNode, null);
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
