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
import com.networknt.schema.uri.URITranslator.CompositeURITranslator;
import com.networknt.schema.urn.URNFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class JsonSchemaFactory {
    private static final Logger logger = LoggerFactory
            .getLogger(JsonSchemaFactory.class);


    public static class Builder {
        private ObjectMapper objectMapper = null;
        private YAMLMapper yamlMapper = null;
        private String defaultMetaSchemaURI;
        private final Map<String, URIFactory> uriFactoryMap = new HashMap<String, URIFactory>();
        private final Map<String, URIFetcher> uriFetcherMap = new HashMap<String, URIFetcher>();
        private URNFactory urnFactory;
        private final ConcurrentMap<String, JsonMetaSchema> jsonMetaSchemas = new ConcurrentHashMap<String, JsonMetaSchema>();
        private final Map<String, String> uriMap = new HashMap<String, String>();
        private boolean enableUriSchemaCache = true;
        private final CompositeURITranslator uriTranslators = new CompositeURITranslator();

        public Builder() {
            // Adds support for creating {@link URL}s.
            final URIFactory urlFactory = new URLFactory();
            for (final String scheme : URLFactory.SUPPORTED_SCHEMES) {
                this.uriFactoryMap.put(scheme, urlFactory);
            }
            // Adds support for creating URNs.
            this.uriFactoryMap.put(URNURIFactory.SCHEME, new URNURIFactory());

            // Adds support for fetching with {@link URL}s.
            final URIFetcher urlFetcher = new URLFetcher();
            for (final String scheme : URLFetcher.SUPPORTED_SCHEMES) {
                this.uriFetcherMap.put(scheme, urlFetcher);
            }

            // Adds support for creating and fetching with classpath {@link URL}s.
            final URIFactory classpathURLFactory = new ClasspathURLFactory();
            final URIFetcher classpathURLFetcher = new ClasspathURLFetcher();
            for (final String scheme : ClasspathURLFactory.SUPPORTED_SCHEMES) {
                this.uriFactoryMap.put(scheme, classpathURLFactory);
                this.uriFetcherMap.put(scheme, classpathURLFetcher);
            }
        }

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

        /**
         * Maps a number of schemes to a {@link URIFactory}.
         *
         * @param uriFactory the uri factory that will be used for the given schemes.
         * @param schemes    the scheme that the uri factory will be assocaited with.
         * @return this builder.
         */
        public Builder uriFactory(final URIFactory uriFactory, final String... schemes) {
            return uriFactory(uriFactory, Arrays.asList(schemes));
        }

        public Builder uriFactory(final URIFactory uriFactory, final Iterable<String> schemes) {
            for (final String scheme : schemes) {
                this.uriFactoryMap.put(scheme, uriFactory);
            }
            return this;
        }

        /**
         * Maps a number of schemes to a {@link URIFetcher}.
         *
         * @param uriFetcher the uri fetcher that will be used for the given schemes.
         * @param schemes    the scheme that the uri fetcher will be assocaited with.
         * @return this builder.
         */
        public Builder uriFetcher(final URIFetcher uriFetcher, final String... schemes) {
            return uriFetcher(uriFetcher, Arrays.asList(schemes));
        }

        public Builder uriFetcher(final URIFetcher uriFetcher, final Iterable<String> schemes) {
            for (final String scheme : schemes) {
                this.uriFetcherMap.put(scheme, uriFetcher);
            }
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

        /**
         * @deprecated Use {@code addUriTranslator} instead.
         * @param map the map of uri mappings.
         * @return this builder.
         */
        @Deprecated
        public Builder addUriMappings(final Map<String, String> map) {
            this.uriMap.putAll(map);
            return this;
        }

        public Builder addUriTranslator(URITranslator translator) {
            if (null != translator) {
                this.uriTranslators.add(translator);
            }
            return this;
        }

        public Builder addUrnFactory(URNFactory urnFactory) {
            this.urnFactory = urnFactory;
            return this;
        }

        /**
         * @deprecated No longer necessary.
         * @param forceHttps ignored.
         * @return this builder.
         */
        public Builder forceHttps(boolean forceHttps) {
            return this;
        }

        /**
         * @deprecated No longer necessary.
         * @param removeEmptyFragmentSuffix ignored.
         * @return this builder.
         */
        public Builder removeEmptyFragmentSuffix(boolean removeEmptyFragmentSuffix) {
            return this;
        }

        public Builder enableUriSchemaCache(boolean enableUriSchemaCache) {
            this.enableUriSchemaCache = enableUriSchemaCache;
            return this;
        }

        public JsonSchemaFactory build() {
            // create builtin keywords with (custom) formats.
            return new JsonSchemaFactory(
                    objectMapper == null ? new ObjectMapper() : objectMapper,
                    yamlMapper == null ? new YAMLMapper(): yamlMapper,
                    defaultMetaSchemaURI,
                    new URISchemeFactory(uriFactoryMap),
                    new URISchemeFetcher(uriFetcherMap),
                    urnFactory,
                    jsonMetaSchemas,
                    uriMap,
                    enableUriSchemaCache,
                    uriTranslators
            );
        }
    }

    private final ObjectMapper jsonMapper;
    private final YAMLMapper yamlMapper;
    private final String defaultMetaSchemaURI;
    private final URISchemeFactory uriFactory;
    private final URISchemeFetcher uriFetcher;
    private final CompositeURITranslator uriTranslators;
    private final URNFactory urnFactory;
    private final Map<String, JsonMetaSchema> jsonMetaSchemas;
    private final Map<String, String> uriMap;
    private final ConcurrentMap<URI, JsonSchema> uriSchemaCache = new ConcurrentHashMap<>();
    private final boolean enableUriSchemaCache;


    private JsonSchemaFactory(
            final ObjectMapper jsonMapper,
            final YAMLMapper yamlMapper,
            final String defaultMetaSchemaURI,
            final URISchemeFactory uriFactory,
            final URISchemeFetcher uriFetcher,
            final URNFactory urnFactory,
            final Map<String, JsonMetaSchema> jsonMetaSchemas,
            final Map<String, String> uriMap,
            final boolean enableUriSchemaCache,
            final CompositeURITranslator uriTranslators) {
        if (jsonMapper == null) {
            throw new IllegalArgumentException("ObjectMapper must not be null");
        } else if (yamlMapper == null) {
            throw new IllegalArgumentException("YAMLMapper must not be null");
        } else if (defaultMetaSchemaURI == null || defaultMetaSchemaURI.trim().isEmpty()) {
            throw new IllegalArgumentException("defaultMetaSchemaURI must not be null or empty");
        } else if (uriFactory == null) {
            throw new IllegalArgumentException("URIFactory must not be null");
        } else if (uriFetcher == null) {
            throw new IllegalArgumentException("URIFetcher must not be null");
        } else if (jsonMetaSchemas == null || jsonMetaSchemas.isEmpty()) {
            throw new IllegalArgumentException("Json Meta Schemas must not be null or empty");
        } else if (jsonMetaSchemas.get(normalizeMetaSchemaUri(defaultMetaSchemaURI)) == null) {
            throw new IllegalArgumentException("Meta Schema for default Meta Schema URI must be provided");
        } else if (uriMap == null) {
            throw new IllegalArgumentException("URL Mappings must not be null");
        } else if (uriTranslators == null) {
            throw new IllegalArgumentException("URI Translators must not be null");
        }
        this.jsonMapper = jsonMapper;
        this.yamlMapper = yamlMapper;
        this.defaultMetaSchemaURI = defaultMetaSchemaURI;
        this.uriFactory = uriFactory;
        this.uriFetcher = uriFetcher;
        this.urnFactory = urnFactory;
        this.jsonMetaSchemas = jsonMetaSchemas;
        this.uriMap = uriMap;
        this.enableUriSchemaCache = enableUriSchemaCache;
        this.uriTranslators = uriTranslators;
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
                .yamlMapper(blueprint.yamlMapper)
                .addUriMappings(blueprint.uriMap);

        for (URITranslator translator: blueprint.uriTranslators) {
            builder = builder.addUriTranslator(translator);
        }
        for (Map.Entry<String, URIFactory> entry : blueprint.uriFactory.getURIFactories().entrySet()) {
            builder = builder.uriFactory(entry.getValue(), entry.getKey());
        }
        for (Map.Entry<String, URIFetcher> entry : blueprint.uriFetcher.getURIFetchers().entrySet()) {
            builder = builder.uriFetcher(entry.getValue(), entry.getKey());
        }
        return builder;
    }

    protected JsonSchema newJsonSchema(final URI schemaUri, final JsonNode schemaNode, final SchemaValidatorsConfig config) {
        final ValidationContext validationContext = createValidationContext(schemaNode);
        validationContext.setConfig(config);
        return doCreate(validationContext, getSchemaLocation(schemaUri, schemaNode, validationContext),
                new JsonNodePath(validationContext.getConfig().getPathType()), schemaUri, schemaNode, null, false);
    }

    public JsonSchema create(ValidationContext validationContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath, URI currentUri, JsonNode schemaNode, JsonSchema parentSchema) {
        return doCreate(validationContext,
                null == schemaLocation ? getSchemaLocation(currentUri, schemaNode, validationContext) : schemaLocation,
                evaluationPath, currentUri, schemaNode, parentSchema, false);
    }

    public JsonSchema create(ValidationContext validationContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema) {
        return create(validationContext, schemaLocation, evaluationPath, parentSchema.getCurrentUri(), schemaNode, parentSchema);
    }

    private JsonSchema doCreate(ValidationContext validationContext, SchemaLocation schemaLocation, JsonNodePath evaluationPath, URI currentUri, JsonNode schemaNode, JsonSchema parentSchema, boolean suppressSubSchemaRetrieval) {
        return JsonSchema.from(validationContext, schemaLocation, evaluationPath, currentUri, schemaNode, parentSchema, suppressSubSchemaRetrieval);
    }

    /**
     * Gets the schema location from the $id or retrieval uri.
     *
     * @param schemaRetrievalUri the schema retrieval uri
     * @param schemaNode the schema json
     * @param validationContext the validationContext
     * @return the schema location
     */
    protected SchemaLocation getSchemaLocation(URI schemaRetrievalUri, JsonNode schemaNode,
            ValidationContext validationContext) {
        String schemaLocation = validationContext.resolveSchemaId(schemaNode);
        if (schemaLocation == null && schemaRetrievalUri != null) {
            schemaLocation = schemaRetrievalUri.toString();
        }
        return schemaLocation != null ? SchemaLocation.of(schemaLocation) : SchemaLocation.DOCUMENT;
    }

    protected ValidationContext createValidationContext(final JsonNode schemaNode) {
        final JsonMetaSchema jsonMetaSchema = findMetaSchemaForSchema(schemaNode);
        return new ValidationContext(this.uriFactory, this.urnFactory, jsonMetaSchema, this, null);
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

    /**
     * @return A shared {@link URI} factory that is used for creating the URI references in schemas.
     */
    public URIFactory getUriFactory() {
        return this.uriFactory;
    }

    public URITranslator getUriTranslator() {
        return this.uriTranslators.with(URITranslator.map(uriMap));
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

    public JsonSchema getSchema(final URI schemaUri, final SchemaValidatorsConfig config) {
        final URITranslator uriTranslator = null == config ? getUriTranslator()
                : config.getUriTranslator().with(getUriTranslator());

        final URI mappedUri;
        try {
            mappedUri = this.uriFactory.create(uriTranslator.translate(schemaUri).toString());
        } catch (IllegalArgumentException e) {
            logger.error("Failed to create URI.", e);
            throw new JsonSchemaException(e);
        }

        if (enableUriSchemaCache) {
            JsonSchema cachedUriSchema = uriSchemaCache.computeIfAbsent(mappedUri, key -> {
                return getMappedSchema(schemaUri, config, mappedUri);
            });
            // This is important because if we use same JsonSchemaFactory for creating
            // multiple JSONSchema instances,
            // these schemas will be cached along with config. We have to replace the config
            // for cached $ref references
            // with the latest config.
            cachedUriSchema.getValidationContext().setConfig(config);
            return cachedUriSchema;
        }
        return getMappedSchema(schemaUri, config, mappedUri);
    }
    
    protected JsonSchema getMappedSchema(final URI schemaUri, SchemaValidatorsConfig config, final URI mappedUri) {
        try (InputStream inputStream = this.uriFetcher.fetch(mappedUri)) {
            final JsonNode schemaNode;
            if (isYaml(mappedUri)) {
                schemaNode = yamlMapper.readTree(inputStream);
            } else {
                schemaNode = jsonMapper.readTree(inputStream);
            }

            final JsonMetaSchema jsonMetaSchema = findMetaSchemaForSchema(schemaNode);
            JsonNodePath evaluationPath = new JsonNodePath(config.getPathType());
            JsonSchema jsonSchema;
            SchemaLocation schemaLocation = SchemaLocation.of(schemaUri.toString());
            if (idMatchesSourceUri(jsonMetaSchema, schemaNode, schemaUri) || schemaUri.getFragment() == null
                    || "".equals(schemaUri.getFragment())) {
                ValidationContext validationContext = new ValidationContext(this.uriFactory, this.urnFactory, jsonMetaSchema, this, config);
                jsonSchema = doCreate(validationContext, schemaLocation, evaluationPath, mappedUri, schemaNode, null, true /* retrieved via id, resolving will not change anything */);
            } else {
                // Subschema
                final ValidationContext validationContext = createValidationContext(schemaNode);
                validationContext.setConfig(config);
                URI documentUri = "".equals(schemaUri.getSchemeSpecificPart()) ? new URI(schemaUri.getScheme(), schemaUri.getUserInfo(), schemaUri.getHost(), schemaUri.getPort(), schemaUri.getPath(), schemaUri.getQuery(), null) : new URI(schemaUri.getScheme(), schemaUri.getSchemeSpecificPart(), null);
                SchemaLocation documentLocation = new SchemaLocation(schemaLocation.getAbsoluteIri());
                JsonSchema document = doCreate(validationContext, documentLocation, evaluationPath, documentUri, schemaNode, null, false);
                JsonNode subSchemaNode = document.getRefSchemaNode(schemaLocation.getFragment().toString());
                if (subSchemaNode != null) {
                    jsonSchema = doCreate(validationContext, schemaLocation, evaluationPath, mappedUri, subSchemaNode, document, false);
                } else {
                    throw new JsonSchemaException("Unable to find subschema");
                }
            }
            return jsonSchema;
        } catch (IOException | URISyntaxException e) {
            logger.error("Failed to load json schema!", e);
            throw new JsonSchemaException(e);
        }
    }

    public JsonSchema getSchema(final URI schemaUri) {
        return getSchema(schemaUri, new SchemaValidatorsConfig());
    }

    public JsonSchema getSchema(final URI schemaUri, final JsonNode jsonNode, final SchemaValidatorsConfig config) {
        return newJsonSchema(schemaUri, jsonNode, config);
    }


    public JsonSchema getSchema(final JsonNode jsonNode, final SchemaValidatorsConfig config) {
        return newJsonSchema(null, jsonNode, config);
    }

    public JsonSchema getSchema(final URI schemaUri, final JsonNode jsonNode) {
        return newJsonSchema(schemaUri, jsonNode, null);
    }

    public JsonSchema getSchema(final JsonNode jsonNode) {
        return newJsonSchema(null, jsonNode, null);
    }

    private boolean idMatchesSourceUri(final JsonMetaSchema metaSchema, final JsonNode schema, final URI schemaUri) {
        String id = metaSchema.readId(schema);
        if (id == null || id.isEmpty()) {
            return false;
        }
        boolean result = id.equals(schemaUri.toString());
        logger.debug("Matching {} to {}: {}", id, schemaUri, result);
        return result;
    }

    private boolean isYaml(final URI schemaUri) {
        final String schemeSpecificPart = schemaUri.getSchemeSpecificPart();
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
