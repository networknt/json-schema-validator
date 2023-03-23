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
        private ObjectMapper objectMapper = new ObjectMapper();
        private YAMLMapper yamlMapper = new YAMLMapper();
        private String defaultMetaSchemaURI;
        private final Map<String, URIFactory> uriFactoryMap = new HashMap<String, URIFactory>();
        private final Map<String, URIFetcher> uriFetcherMap = new HashMap<String, URIFetcher>();
        private URNFactory urnFactory;
        private final Map<String, JsonMetaSchema> jsonMetaSchemas = new HashMap<String, JsonMetaSchema>();
        private final Map<String, String> uriMap = new HashMap<String, String>();
        private boolean forceHttps = true;
        private boolean removeEmptyFragmentSuffix = true;
        private boolean cacheSchema = true;

        public Builder() {
            // Adds support for creating {@link URL}s.
            final URIFactory urlFactory = new URLFactory();
            for (final String scheme : URLFactory.SUPPORTED_SCHEMES) {
                this.uriFactoryMap.put(scheme, urlFactory);
            }

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
            this.jsonMetaSchemas.put(jsonMetaSchema.getUri(), jsonMetaSchema);
            return this;
        }

        public Builder addMetaSchemas(final Collection<? extends JsonMetaSchema> jsonMetaSchemas) {
            for (JsonMetaSchema jsonMetaSchema : jsonMetaSchemas) {
                this.jsonMetaSchemas.put(jsonMetaSchema.getUri(), jsonMetaSchema);
            }
            return this;
        }

        public Builder addUriMappings(final Map<String, String> map) {
            this.uriMap.putAll(map);
            return this;
        }

        public Builder addUrnFactory(URNFactory urnFactory) {
            this.urnFactory = urnFactory;
            return this;
        }

        public Builder forceHttps(boolean forceHttps) {
            this.forceHttps = forceHttps;
            return this;
        }

        public Builder removeEmptyFragmentSuffix(boolean removeEmptyFragmentSuffix) {
            this.removeEmptyFragmentSuffix = removeEmptyFragmentSuffix;
            return this;
        }

        public Builder cacheSchema(boolean cacheSchema) {
            this.cacheSchema = cacheSchema;
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
                    forceHttps,
                    removeEmptyFragmentSuffix,
                    cacheSchema
            );
        }
    }

    private final ObjectMapper jsonMapper;
    private final YAMLMapper yamlMapper;
    private final String defaultMetaSchemaURI;
    private final URISchemeFactory uriFactory;
    private final URISchemeFetcher uriFetcher;
    private final URNFactory urnFactory;
    private final Map<String, JsonMetaSchema> jsonMetaSchemas;
    private final Map<String, String> uriMap;
    private final ConcurrentMap<URI, JsonSchema> uriSchemaCache = new ConcurrentHashMap<URI, JsonSchema>();
    private final boolean forceHttps;
    private final boolean removeEmptyFragmentSuffix;
    private final boolean cacheSchema;


    private JsonSchemaFactory(
            final ObjectMapper jsonMapper,
            final YAMLMapper yamlMapper,
            final String defaultMetaSchemaURI,
            final URISchemeFactory uriFactory,
            final URISchemeFetcher uriFetcher,
            final URNFactory urnFactory,
            final Map<String, JsonMetaSchema> jsonMetaSchemas,
            final Map<String, String> uriMap,
            final boolean forceHttps,
            final boolean removeEmptyFragmentSuffix,
            final boolean cacheSchema) {
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
        } else if (jsonMetaSchemas.get(defaultMetaSchemaURI) == null) {
            throw new IllegalArgumentException("Meta Schema for default Meta Schema URI must be provided");
        } else if (uriMap == null) {
            throw new IllegalArgumentException("URL Mappings must not be null");
        }
        this.jsonMapper = jsonMapper;
        this.yamlMapper = yamlMapper;
        this.defaultMetaSchemaURI = defaultMetaSchemaURI;
        this.uriFactory = uriFactory;
        this.uriFetcher = uriFetcher;
        this.urnFactory = urnFactory;
        this.jsonMetaSchemas = jsonMetaSchemas;
        this.uriMap = uriMap;
        this.forceHttps = forceHttps;
        this.removeEmptyFragmentSuffix = removeEmptyFragmentSuffix;
        this.cacheSchema = cacheSchema;
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
        JsonSchemaVersion jsonSchemaVersion = null;
        switch (versionFlag) {
            case V202012:
                jsonSchemaVersion = new Version202012();
                break;
            case V201909:
                jsonSchemaVersion = new Version201909();
                break;
            case V7:
                jsonSchemaVersion = new Version7();
                break;
            case V6:
                jsonSchemaVersion = new Version6();
                break;
            case V4:
                jsonSchemaVersion = new Version4();
                break;
        }
        return jsonSchemaVersion;
    }

    public static Builder builder(final JsonSchemaFactory blueprint) {
        Builder builder = builder()
                .addMetaSchemas(blueprint.jsonMetaSchemas.values())
                .defaultMetaSchemaURI(blueprint.defaultMetaSchemaURI)
                .objectMapper(blueprint.jsonMapper)
                .yamlMapper(blueprint.yamlMapper)
                .addUriMappings(blueprint.uriMap);

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
        return new JsonSchema(validationContext, schemaUri, schemaNode);
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
        final String uri = uriNode == null || uriNode.isNull() ? defaultMetaSchemaURI : normalizeMetaSchemaUri(uriNode.textValue(), forceHttps, removeEmptyFragmentSuffix);
        final JsonMetaSchema jsonMetaSchema = jsonMetaSchemas.get(uri);
        if (jsonMetaSchema == null) {
            throw new JsonSchemaException("Unknown MetaSchema: " + uri);
        }
        return jsonMetaSchema;
    }

    /**
     * @return A shared {@link URI} factory that is used for creating the URI references in schemas.
     */
    public URIFactory getUriFactory() {
        return this.uriFactory;
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
        try {
            InputStream inputStream = null;
            final Map<String, String> map = (config != null) ? config.getUriMappings() : new HashMap<String, String>();
            map.putAll(uriMap);

            final URI mappedUri;
            try {
                mappedUri = this.uriFactory.create(map.get(schemaUri.toString()) != null ? map.get(schemaUri.toString()) : schemaUri.toString());
            } catch (IllegalArgumentException e) {
                logger.error("Failed to create URI.", e);
                throw new JsonSchemaException(e);
            }

            if (cacheSchema && uriSchemaCache.containsKey(mappedUri)) {
                JsonSchema cachedUriSchema =  uriSchemaCache.get(mappedUri);
                // This is important because if we use same JsonSchemaFactory for creating multiple JSONSchema instances,
                // these schemas will be cached along with config. We have to replace the config for cached $ref references
                // with the latest config.
                cachedUriSchema.getValidationContext().setConfig(config);
                return cachedUriSchema;
            }

            try {
                inputStream = this.uriFetcher.fetch(mappedUri);

                final JsonNode schemaNode;
                if (isYaml(mappedUri)) {
                    schemaNode = yamlMapper.readTree(inputStream);
                } else {
                    schemaNode = jsonMapper.readTree(inputStream);
                }

                final JsonMetaSchema jsonMetaSchema = findMetaSchemaForSchema(schemaNode);

                JsonSchema jsonSchema;
                if (idMatchesSourceUri(jsonMetaSchema, schemaNode, schemaUri)) {
                    jsonSchema = new JsonSchema(
                        new ValidationContext(this.uriFactory, this.urnFactory, jsonMetaSchema, this, config),
                        mappedUri, schemaNode, true /* retrieved via id, resolving will not change anything */);
                } else {
                    final ValidationContext validationContext = createValidationContext(schemaNode);
                    validationContext.setConfig(config);
                    jsonSchema = new JsonSchema(validationContext, mappedUri, schemaNode);
                }

                if (cacheSchema) {
                    uriSchemaCache.put(mappedUri, jsonSchema);
                }

                return jsonSchema;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new JsonSchemaException(ioe);
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

    static protected String normalizeMetaSchemaUri(String u, boolean forceHttps, boolean removeEmptyFragmentSuffix) {
        try {
            URI uri = new URI(u);
            String scheme = forceHttps ? "https" : uri.getScheme();
            URI newUri = new URI(scheme, uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), null, null);

            if (!removeEmptyFragmentSuffix && u.endsWith("#")) {
                return newUri + "#";
            } else {
                return newUri.toString();
            }
        } catch (URISyntaxException e) {
            throw new JsonSchemaException("Wrong MetaSchema URI: " + u);
        }
    }
}
