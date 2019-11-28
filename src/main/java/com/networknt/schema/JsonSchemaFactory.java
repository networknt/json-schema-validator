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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.uri.ClasspathURLFactory;
import com.networknt.schema.uri.ClasspathURLFetcher;
import com.networknt.schema.uri.URIFactory;
import com.networknt.schema.uri.URIFetcher;
import com.networknt.schema.uri.URISchemeFactory;
import com.networknt.schema.uri.URISchemeFetcher;
import com.networknt.schema.uri.URLFactory;
import com.networknt.schema.uri.URLFetcher;

public class JsonSchemaFactory {
    private static final Logger logger = LoggerFactory
            .getLogger(JsonSchemaFactory.class);


    public static class Builder {
        private ObjectMapper objectMapper = new ObjectMapper();
        private String defaultMetaSchemaURI;
        private final Map<String, URIFactory> uriFactoryMap = new HashMap<String, URIFactory>();
        private final Map<String, URIFetcher> uriFetcherMap = new HashMap<String, URIFetcher>();
        private final Map<String, JsonMetaSchema> jsonMetaSchemas = new HashMap<String, JsonMetaSchema>();
        private final Map<String, String> uriMap = new HashMap<String, String>();

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
        
        public Builder defaultMetaSchemaURI(final String defaultMetaSchemaURI) {
            this.defaultMetaSchemaURI = defaultMetaSchemaURI;
            return this;
        }
        
        /**
         * Maps a number of schemes to a {@link URIFactory}.
         * @param uriFactory the uri factory that will be used for the given schemes.
         * @param schemes the scheme that the uri factory will be assocaited with.
         * @return this builder.
         */
        public Builder uriFactory(final URIFactory uriFactory, final String... schemes) {
            for (final String scheme : schemes)
            {
                this.uriFactoryMap.put(scheme, uriFactory);
            }
            return this;
        }
		
        /**
         * Maps a number of schemes to a {@link URIFetcher}.
         * @param uriFetcher the uri fetcher that will be used for the given schemes.
         * @param schemes the scheme that the uri fetcher will be assocaited with.
         * @return this builder.
         */
        public Builder uriFetcher(final URIFetcher uriFetcher, final String... schemes) {
            for (final String scheme : schemes)
            {
                this.uriFetcherMap.put(scheme, uriFetcher);
            }
            return this;
        }
        
        public Builder addMetaSchema(final JsonMetaSchema jsonMetaSchema) {
            this.jsonMetaSchemas.put(jsonMetaSchema.getUri(), jsonMetaSchema);
            return this;
        }
        
        public Builder addMetaSchemas(final Collection<? extends JsonMetaSchema> jsonMetaSchemas) {
            for (JsonMetaSchema jsonMetaSchema: jsonMetaSchemas) {
                this.jsonMetaSchemas.put(jsonMetaSchema.getUri(), jsonMetaSchema);
            }
            return this;
        }
        
        public Builder addUriMappings(final Map<String, String> map) {
            this.uriMap.putAll(map);
            return this;
        }
        
        public JsonSchemaFactory build() {
            // create builtin keywords with (custom) formats.
            return new JsonSchemaFactory(
                    objectMapper == null ? new ObjectMapper() : objectMapper,
                    defaultMetaSchemaURI, 
                    new URISchemeFactory(uriFactoryMap),
                    new URISchemeFetcher(uriFetcherMap), 
                    jsonMetaSchemas,
                    uriMap
            );
        }
    }
    
    private final ObjectMapper mapper;
    private final String defaultMetaSchemaURI;
    private final URISchemeFactory uriFactory;
    private final URISchemeFetcher uriFetcher;
    private final Map<String, JsonMetaSchema> jsonMetaSchemas;
    private final Map<String, String> uriMap;

    private JsonSchemaFactory(
            final ObjectMapper mapper,
            final String defaultMetaSchemaURI,
            final URISchemeFactory uriFactory,
            final URISchemeFetcher uriFetcher,
            final Map<String, JsonMetaSchema> jsonMetaSchemas,
            final Map<String, String> uriMap) {
        if (mapper == null) {
            throw new IllegalArgumentException("ObjectMapper must not be null");
        }
        if (defaultMetaSchemaURI == null || defaultMetaSchemaURI.trim().isEmpty()) {
            throw new IllegalArgumentException("defaultMetaSchemaURI must not be null or empty");
        }
        if (uriFactory == null) {
            throw new IllegalArgumentException("URIFactory must not be null");
        }
        if (uriFetcher == null) {
            throw new IllegalArgumentException("URIFetcher must not be null");
        }
        if (jsonMetaSchemas == null || jsonMetaSchemas.isEmpty()) {
            throw new IllegalArgumentException("Json Meta Schemas must not be null or empty");
        }
        if (jsonMetaSchemas.get(defaultMetaSchemaURI) == null) {
            throw new IllegalArgumentException("Meta Schema for default Meta Schema URI must be provided");
        }
        if (uriMap == null) {
            throw new IllegalArgumentException("URL Mappings must not be null");
        }
        this.mapper = mapper;
        this.defaultMetaSchemaURI = defaultMetaSchemaURI;
        this.uriFactory = uriFactory;
        this.uriFetcher = uriFetcher;
        this.jsonMetaSchemas = jsonMetaSchemas;
        this.uriMap = uriMap;
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
    static Builder builder() {
        return new Builder();
    }
    
    public static JsonSchemaFactory getInstance() {
        return getInstance(SpecVersion.VersionFlag.V4);
    }

    public static JsonSchemaFactory getInstance(SpecVersion.VersionFlag versionFlag) {
        if(versionFlag == SpecVersion.VersionFlag.V201909) {
            JsonMetaSchema v201909 = JsonMetaSchema.getV201909();
            return builder()
                    .defaultMetaSchemaURI(v201909.getUri())
                    .addMetaSchema(v201909)
                    .build();
        } else if(versionFlag == SpecVersion.VersionFlag.V7) {
            JsonMetaSchema v7 = JsonMetaSchema.getV7();
            return builder()
                    .defaultMetaSchemaURI(v7.getUri())
                    .addMetaSchema(v7)
                    .build();
        } else if(versionFlag == SpecVersion.VersionFlag.V6) {
            JsonMetaSchema v6 = JsonMetaSchema.getV6();
            return builder()
                    .defaultMetaSchemaURI(v6.getUri())
                    .addMetaSchema(v6)
                    .build();
        } else if(versionFlag == SpecVersion.VersionFlag.V4) {
            JsonMetaSchema v4 = JsonMetaSchema.getV4();
            return builder()
                    .defaultMetaSchemaURI(v4.getUri())
                    .addMetaSchema(v4)
                    .build();
        }
        return null;
    }
    
    public static Builder builder(final JsonSchemaFactory blueprint) {
        Builder builder = builder()
                .addMetaSchemas(blueprint.jsonMetaSchemas.values())
                .defaultMetaSchemaURI(blueprint.defaultMetaSchemaURI)
                .objectMapper(blueprint.mapper)
                .addUriMappings(blueprint.uriMap);
        
        for (Map.Entry<String, URIFactory> entry : blueprint.uriFactory.getURIFactories().entrySet())
        {
            builder = builder.uriFactory(entry.getValue(), entry.getKey());
        }
        for (Map.Entry<String, URIFetcher> entry : blueprint.uriFetcher.getURIFetchers().entrySet())
        {
            builder = builder.uriFetcher(entry.getValue(), entry.getKey());
        }
        return builder;
    }
    
    private JsonSchema newJsonSchema(final URI schemaUri, final JsonNode schemaNode, final SchemaValidatorsConfig config) {
        final ValidationContext validationContext = createValidationContext(schemaNode);
        validationContext.setConfig(config);
        JsonSchema jsonSchema = new JsonSchema(validationContext, schemaUri, schemaNode);
        return jsonSchema;
    }

    protected ValidationContext createValidationContext(final JsonNode schemaNode) {
        final JsonMetaSchema jsonMetaSchema = findMetaSchemaForSchema(schemaNode);
        return new ValidationContext(this.uriFactory, jsonMetaSchema, this, null);
    }

    private JsonMetaSchema findMetaSchemaForSchema(final JsonNode schemaNode) {
        final JsonNode uriNode = schemaNode.get("$schema");
        final String uri = uriNode == null || uriNode.isNull() ? defaultMetaSchemaURI: uriNode.textValue();
        final JsonMetaSchema jsonMetaSchema = jsonMetaSchemas.get(uri);
        if (jsonMetaSchema == null) {
            throw new JsonSchemaException("Unknown Metaschema: " + uri);
        }
        return jsonMetaSchema;
    }
    
    /**
     * @return A shared {@link URI} factory that is used for creating the URI references in schemas.
     */
    public URIFactory getUriFactory()
    {
    	return this.uriFactory;
    }
	
    public JsonSchema getSchema(final String schema, final SchemaValidatorsConfig config) {
        try {
            final JsonNode schemaNode = mapper.readTree(schema);
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
            final JsonNode schemaNode = mapper.readTree(schemaStream);
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
            final Map<String, String> map = (config != null) ? config.getUriMappings() : new HashMap<String, String>(uriMap);
            map.putAll(uriMap);
            
            final URI mappedUri;
            try {
                mappedUri = this.uriFactory.create(map.get(schemaUri.toString())!=null ? map.get(schemaUri.toString()) : schemaUri.toString());
            } catch (IllegalArgumentException e) {
                logger.error("Failed to create URI.", e);
                throw new JsonSchemaException(e);
            }
            try {
                inputStream = this.uriFetcher.fetch(mappedUri);
                final JsonNode schemaNode = mapper.readTree(inputStream);
                final JsonMetaSchema jsonMetaSchema = findMetaSchemaForSchema(schemaNode);

                if (idMatchesSourceUri(jsonMetaSchema, schemaNode, schemaUri)) {
                    return new JsonSchema(new ValidationContext(this.uriFactory, jsonMetaSchema, this, config), mappedUri, schemaNode, true /*retrieved via id, resolving will not change anything*/);
                }

                return newJsonSchema(mappedUri, schemaNode, config);
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
        if (logger.isDebugEnabled()) {
            logger.debug("Matching " + id + " to " + schemaUri.toString() + ": " + result);
        }
        return result;
    }
}
