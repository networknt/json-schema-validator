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
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.networknt.schema.uri.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class JsonSchemaFactory {
    private static final Logger logger = LoggerFactory
            .getLogger(JsonSchemaFactory.class);


    public static class Builder {
        private ObjectMapper objectMapper = null;
        private YAMLMapper yamlMapper = null;
        private String defaultMetaSchemaURI;
        private final ConcurrentMap<String, JsonMetaSchema> jsonMetaSchemas = new ConcurrentHashMap<String, JsonMetaSchema>();
        private SchemaLoaderBuilder schemaLoaderBuilder = new SchemaLoaderBuilder();
        private boolean enableUriSchemaCache = true;

        public Builder objectMapper(final ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        public Builder yamlMapper(final YAMLMapper yamlMapper) {
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
        
        public Builder schemaLoaderBuilder(Consumer<SchemaLoaderBuilder> schemaLoaderBuilderCustomizer) {
            schemaLoaderBuilderCustomizer.accept(this.schemaLoaderBuilder);
            return this;
        }

        public JsonSchemaFactory build() {
            // create builtin keywords with (custom) formats.
            return new JsonSchemaFactory(
                    objectMapper == null ? new ObjectMapper() : objectMapper,
                    yamlMapper == null ? new YAMLMapper(): yamlMapper,
                    defaultMetaSchemaURI,
                    schemaLoaderBuilder,
                    jsonMetaSchemas,
                    enableUriSchemaCache
            );
        }
    }

    private final ObjectMapper jsonMapper;
    private final YAMLMapper yamlMapper;
    private final String defaultMetaSchemaURI;
    private final SchemaLoaderBuilder schemaLoaderBuilder;
    private final SchemaLoader schemaLoader;
    private final Map<String, JsonMetaSchema> jsonMetaSchemas;
    private final ConcurrentMap<SchemaLocation, JsonSchema> uriSchemaCache = new ConcurrentHashMap<>();
    private final boolean enableUriSchemaCache;


    private JsonSchemaFactory(
            final ObjectMapper jsonMapper,
            final YAMLMapper yamlMapper,
            final String defaultMetaSchemaURI,
            SchemaLoaderBuilder schemaLoaderBuilder,
            final Map<String, JsonMetaSchema> jsonMetaSchemas,
            final boolean enableUriSchemaCache) {
        if (jsonMapper == null) {
            throw new IllegalArgumentException("ObjectMapper must not be null");
        } else if (yamlMapper == null) {
            throw new IllegalArgumentException("YAMLMapper must not be null");
        } else if (defaultMetaSchemaURI == null || defaultMetaSchemaURI.trim().isEmpty()) {
            throw new IllegalArgumentException("defaultMetaSchemaURI must not be null or empty");
        } else if (schemaLoaderBuilder == null) {
            throw new IllegalArgumentException("SchemaLoaders must not be null");
        } else if (jsonMetaSchemas == null || jsonMetaSchemas.isEmpty()) {
            throw new IllegalArgumentException("Json Meta Schemas must not be null or empty");
        } else if (jsonMetaSchemas.get(normalizeMetaSchemaUri(defaultMetaSchemaURI)) == null) {
            throw new IllegalArgumentException("Meta Schema for default Meta Schema URI must be provided");
        }
        this.jsonMapper = jsonMapper;
        this.yamlMapper = yamlMapper;
        this.defaultMetaSchemaURI = defaultMetaSchemaURI;
        this.schemaLoaderBuilder = schemaLoaderBuilder;
        this.schemaLoader = schemaLoaderBuilder.build();
        this.jsonMetaSchemas = jsonMetaSchemas;
        this.enableUriSchemaCache = enableUriSchemaCache;
    }
    
    public SchemaLoader getSchemaLoader() {
        return this.schemaLoader;
    }

    /**
     * Builder without keywords or formats.
     *
     * <code>
     * JsonSchemaFactory.builder(JsonSchemaFactory.getDraftV4()).build();
     * </code>
     *
     * @return a builder instance without any keywords or formats - usually not what one needs.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @deprecated
     * This is a method that is kept to ensure backward compatible. You shouldn't use it anymore.
     * Please specify the draft version when get an instance.
     *
     * @return JsonSchemaFactory
     */
    @Deprecated
    public static JsonSchemaFactory getInstance() {
        return getInstance(SpecVersion.VersionFlag.V4);
    }

    public static JsonSchemaFactory getInstance(SpecVersion.VersionFlag versionFlag) {
        JsonSchemaVersion jsonSchemaVersion = checkVersion(versionFlag);
        JsonMetaSchema metaSchema = jsonSchemaVersion.getInstance();
        return builder()
                .defaultMetaSchemaURI(metaSchema.getUri())
                .addMetaSchema(metaSchema)
                .build();
    }

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

    public static Builder builder(final JsonSchemaFactory blueprint) {
        Builder builder = builder()
                .addMetaSchemas(blueprint.jsonMetaSchemas.values())
                .defaultMetaSchemaURI(blueprint.defaultMetaSchemaURI)
                .objectMapper(blueprint.jsonMapper)
                .yamlMapper(blueprint.yamlMapper);
        builder.schemaLoaderBuilder = blueprint.schemaLoaderBuilder;
        return builder;
    }

    protected JsonSchema newJsonSchema(final SchemaLocation schemaUri, final JsonNode schemaNode, final SchemaValidatorsConfig config) {
        final ValidationContext validationContext = createValidationContext(schemaNode, config);
        return doCreate(validationContext, getSchemaLocation(schemaUri, schemaNode, validationContext),
                new JsonNodePath(validationContext.getConfig().getPathType()), schemaNode, null, false);
    }

    public JsonSchema create(ValidationContext validationContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema) {
        return doCreate(validationContext, schemaLocation, evaluationPath, schemaNode, parentSchema, false);
    }

    private JsonSchema doCreate(ValidationContext validationContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, boolean suppressSubSchemaRetrieval) {
        return JsonSchema.from(validationContext, schemaLocation, evaluationPath, schemaNode, parentSchema, suppressSubSchemaRetrieval);
    }

    /**
     * Gets the schema location from the $id or retrieval uri.
     *
     * @param schemaRetrievalUri the schema retrieval uri
     * @param schemaNode the schema json
     * @param validationContext the validationContext
     * @return the schema location
     */
    protected SchemaLocation getSchemaLocation(SchemaLocation schemaRetrievalUri, JsonNode schemaNode,
            ValidationContext validationContext) {
        String schemaLocation = validationContext.resolveSchemaId(schemaNode);
        if (schemaLocation == null && schemaRetrievalUri != null) {
            schemaLocation = schemaRetrievalUri.toString();
        }
        return schemaLocation != null ? SchemaLocation.of(schemaLocation) : SchemaLocation.DOCUMENT;
    }

    protected ValidationContext createValidationContext(final JsonNode schemaNode, SchemaValidatorsConfig config) {
        final JsonMetaSchema jsonMetaSchema = findMetaSchemaForSchema(schemaNode);
        return new ValidationContext(jsonMetaSchema, this, config);
    }

    private JsonMetaSchema findMetaSchemaForSchema(final JsonNode schemaNode) {
        final JsonNode uriNode = schemaNode.get("$schema");
        if (uriNode != null && !uriNode.isNull() && !uriNode.isTextual()) {
            throw new JsonSchemaException("Unknown MetaSchema: " + uriNode.toString());
        }
        final String uri = uriNode == null || uriNode.isNull() ? defaultMetaSchemaURI : normalizeMetaSchemaUri(uriNode.textValue());
        final JsonMetaSchema jsonMetaSchema = jsonMetaSchemas.computeIfAbsent(uri, this::fromId);
        return jsonMetaSchema;
    }

    private JsonMetaSchema fromId(String id) {
        // Is it a well-known dialect?
        return SpecVersionDetector.detectOptionalVersion(id)
            .map(JsonSchemaFactory::checkVersion)
            .map(JsonSchemaVersion::getInstance)
            .orElseThrow(() -> new JsonSchemaException("Unknown MetaSchema: " + id));
    }

    public JsonSchema getSchema(final String schema, final SchemaValidatorsConfig config) {
        try {
            final JsonNode schemaNode = jsonMapper.readTree(schema);
            return newJsonSchema(null, schemaNode, config);
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new JsonSchemaException(ioe);
        }
    }

    public JsonSchema getSchema(final String schema) {
        return getSchema(schema, null);
    }

    public JsonSchema getSchema(final InputStream schemaStream, final SchemaValidatorsConfig config) {
        try {
            final JsonNode schemaNode = jsonMapper.readTree(schemaStream);
            return newJsonSchema(null, schemaNode, config);
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new JsonSchemaException(ioe);
        }
    }

    public JsonSchema getSchema(final InputStream schemaStream) {
        return getSchema(schemaStream, null);
    }

    public JsonSchema getSchema(final SchemaLocation schemaUri, final SchemaValidatorsConfig config) {
        if (enableUriSchemaCache) {
            JsonSchema cachedUriSchema = uriSchemaCache.computeIfAbsent(schemaUri, key -> {
                return getMappedSchema(schemaUri, config);
            });
            return cachedUriSchema.withConfig(config);
        }
        return getMappedSchema(schemaUri, config);
    }
    
    protected JsonSchema getMappedSchema(final SchemaLocation schemaUri, SchemaValidatorsConfig config) {
        try (InputStream inputStream = this.schemaLoader.getSchema(schemaUri).getInputStream()) {
            if (inputStream == null) {
                throw new IOException("Cannot load schema at " + schemaUri.toString());
            }
            final JsonNode schemaNode;
            if (isYaml(schemaUri)) {
                schemaNode = yamlMapper.readTree(inputStream);
            } else {
                schemaNode = jsonMapper.readTree(inputStream);
            }

            final JsonMetaSchema jsonMetaSchema = findMetaSchemaForSchema(schemaNode);
            JsonNodePath evaluationPath = new JsonNodePath(config.getPathType());
            JsonSchema jsonSchema;
            SchemaLocation schemaLocation = SchemaLocation.of(schemaUri.toString());
            if (idMatchesSourceUri(jsonMetaSchema, schemaNode, schemaUri) || schemaUri.getFragment() == null
                    || schemaUri.getFragment().getNameCount() == 0) {
                ValidationContext validationContext = new ValidationContext(jsonMetaSchema, this, config);
                jsonSchema = doCreate(validationContext, schemaLocation, evaluationPath, schemaNode, null, true /* retrieved via id, resolving will not change anything */);
            } else {
                // Subschema
                final ValidationContext validationContext = createValidationContext(schemaNode, config);
                SchemaLocation documentLocation = new SchemaLocation(schemaLocation.getAbsoluteIri());
                JsonSchema document = doCreate(validationContext, documentLocation, evaluationPath, schemaNode, null, false);
                return document.getSubSchema(schemaLocation.getFragment());
            }
            return jsonSchema;
        } catch (IOException e) {
            logger.error("Failed to load json schema from {}", schemaUri, e);
            throw new JsonSchemaException(e);
        }
    }
    
    public JsonSchema getSchema(final SchemaLocation schemaUri) {
        return getSchema(schemaUri, new SchemaValidatorsConfig());
    }

    public JsonSchema getSchema(final SchemaLocation schemaUri, final JsonNode jsonNode, final SchemaValidatorsConfig config) {
        return newJsonSchema(schemaUri, jsonNode, config);
    }


    public JsonSchema getSchema(final JsonNode jsonNode, final SchemaValidatorsConfig config) {
        return newJsonSchema(null, jsonNode, config);
    }

    public JsonSchema getSchema(final SchemaLocation schemaUri, final JsonNode jsonNode) {
        return newJsonSchema(schemaUri, jsonNode, null);
    }

    public JsonSchema getSchema(final JsonNode jsonNode) {
        return newJsonSchema(null, jsonNode, null);
    }

    private boolean idMatchesSourceUri(final JsonMetaSchema metaSchema, final JsonNode schema, final SchemaLocation schemaUri) {
        String id = metaSchema.readId(schema);
        if (id == null || id.isEmpty()) {
            return false;
        }
        boolean result = id.equals(schemaUri.toString());
        logger.debug("Matching {} to {}: {}", id, schemaUri, result);
        return result;
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

    static protected String normalizeMetaSchemaUri(String u) {
        try {
            URI uri = new URI(u);
            URI newUri = new URI("https", uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), null, null);

            return newUri.toString();
        } catch (URISyntaxException e) {
            throw new JsonSchemaException("Wrong MetaSchema URI: " + u);
        }
    }
}
