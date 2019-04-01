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
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.url.StandardURLFetcher;
import com.networknt.schema.url.URLFetcher;

public class JsonSchemaFactory {
    private static final Logger logger = LoggerFactory
            .getLogger(JsonSchemaFactory.class);
    
    
    public static class Builder {
        private ObjectMapper objectMapper = new ObjectMapper();
        private URLFetcher urlFetcher;
        private String defaultMetaSchemaURI;
        private Map<String, JsonMetaSchema> jsonMetaSchemas = new HashMap<String, JsonMetaSchema>();
        private Map<URL, URL> urlMap = new HashMap<URL, URL>();
        
        public Builder objectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }
        
        public Builder urlFetcher(URLFetcher urlFetcher) {
            this.urlFetcher = urlFetcher;
            return this;
        }
        
        public Builder defaultMetaSchemaURI(String defaultMetaSchemaURI) {
            this.defaultMetaSchemaURI = defaultMetaSchemaURI;
            return this;
        }
        
        public Builder addMetaSchema(JsonMetaSchema jsonMetaSchema) {
            this.jsonMetaSchemas.put(jsonMetaSchema.getUri(), jsonMetaSchema);
            return this;
        }
        
        public Builder addMetaSchemas(Collection<? extends JsonMetaSchema> jsonMetaSchemas) {
            for (JsonMetaSchema jsonMetaSchema: jsonMetaSchemas) {
                this.jsonMetaSchemas.put(jsonMetaSchema.getUri(), jsonMetaSchema);
            }
            return this;
        }
        
        public Builder addUrlMappings(Map<URL, URL> map) {
            this.urlMap.putAll(map);
            return this;
        }
        
        public JsonSchemaFactory build() {
            // create builtin keywords with (custom) formats.
            return new JsonSchemaFactory(
                    objectMapper == null ? new ObjectMapper() : objectMapper, 
                    urlFetcher == null ? new StandardURLFetcher(): urlFetcher, 
                    defaultMetaSchemaURI, 
                    jsonMetaSchemas,
                    urlMap
            );
        }
    }
    
    private final ObjectMapper mapper;
    private final URLFetcher urlFetcher;
    private final String defaultMetaSchemaURI;
    private final Map<String, JsonMetaSchema> jsonMetaSchemas;
    private final Map<URL, URL> urlMap;

    private JsonSchemaFactory(ObjectMapper mapper, URLFetcher urlFetcher, String defaultMetaSchemaURI, Map<String, JsonMetaSchema> jsonMetaSchemas, Map<URL, URL> urlMap) {
        if (mapper == null) {
            throw new IllegalArgumentException("ObjectMapper must not be null");
        }
        if (urlFetcher == null) {
            throw new IllegalArgumentException("URLFetcher must not be null");
        }
        if (defaultMetaSchemaURI == null || defaultMetaSchemaURI.trim().isEmpty()) {
            throw new IllegalArgumentException("defaultMetaSchemaURI must not be null or empty");
        }
        if (jsonMetaSchemas == null || jsonMetaSchemas.isEmpty()) {
            throw new IllegalArgumentException("Json Meta Schemas must not be null or empty");
        }
        if (jsonMetaSchemas.get(defaultMetaSchemaURI) == null) {
            throw new IllegalArgumentException("Meta Schema for default Meta Schema URI must be provided");
        }
        if (urlMap == null) {
            throw new IllegalArgumentException("URL Mappings must not be null");
        }
        this.mapper = mapper;
        this.defaultMetaSchemaURI = defaultMetaSchemaURI;
        this.urlFetcher = urlFetcher;
        this.jsonMetaSchemas = jsonMetaSchemas;
        this.urlMap = urlMap;
    }

    /**
     * Builder without keywords or formats.
     * 
     * Use {@link #getDraftV4()} instead, or if you need a builder based on Draft4, use
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
        JsonMetaSchema draftV4 = JsonMetaSchema.getDraftV4();
        return builder()
                .defaultMetaSchemaURI(draftV4.getUri())
                .addMetaSchema(draftV4)
                .build();
    }
    
    public static Builder builder(JsonSchemaFactory blueprint) {
        return builder()
                .addMetaSchemas(blueprint.jsonMetaSchemas.values())
                .urlFetcher(blueprint.urlFetcher)
                .defaultMetaSchemaURI(blueprint.defaultMetaSchemaURI)
                .objectMapper(blueprint.mapper)
                .addUrlMappings(blueprint.urlMap);
    }
    
    private JsonSchema newJsonSchema(JsonNode schemaNode, SchemaValidatorsConfig config) {
        final ValidationContext validationContext = createValidationContext(schemaNode);
        validationContext.setConfig(config);
        JsonSchema jsonSchema = new JsonSchema(validationContext, schemaNode);
        return jsonSchema;
    }

    protected ValidationContext createValidationContext(JsonNode schemaNode) {
        final JsonMetaSchema jsonMetaSchema = findMetaSchemaForSchema(schemaNode);
        return new ValidationContext(jsonMetaSchema, this);
    }

    private JsonMetaSchema findMetaSchemaForSchema(JsonNode schemaNode) {
        final JsonNode uriNode = schemaNode.get("$schema");
        final String uri = uriNode == null || uriNode.isNull() ? defaultMetaSchemaURI: uriNode.textValue();
        final JsonMetaSchema jsonMetaSchema = jsonMetaSchemas.get(uri);
        if (jsonMetaSchema == null) {
            throw new JsonSchemaException("Unknown Metaschema: " + uri);
        }
        return jsonMetaSchema;
    }

    public JsonSchema getSchema(String schema, SchemaValidatorsConfig config) {
        try {
            final JsonNode schemaNode = mapper.readTree(schema);
            return newJsonSchema(schemaNode, config);
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new JsonSchemaException(ioe);
        }
    }

    public JsonSchema getSchema(String schema) {
        return getSchema(schema, null);
    }

    public JsonSchema getSchema(InputStream schemaStream, SchemaValidatorsConfig config) {
        try {
            final JsonNode schemaNode = mapper.readTree(schemaStream);
            return newJsonSchema(schemaNode, config);
        } catch (IOException ioe) {
            logger.error("Failed to load json schema!", ioe);
            throw new JsonSchemaException(ioe);
        }
    }

    public JsonSchema getSchema(InputStream schemaStream) {
        return getSchema(schemaStream, null);
    }

    public JsonSchema getSchema(URL schemaURL, SchemaValidatorsConfig config) {
        try {
            InputStream inputStream = null;
            Map<URL, URL> map = (config != null) ? config.getUrlMappings() : new HashMap<URL, URL>(urlMap);
            map.putAll(urlMap);
            URL mappedURL = map.getOrDefault(schemaURL, schemaURL);
            try {
                inputStream = urlFetcher.fetch(mappedURL);
                JsonNode schemaNode = mapper.readTree(inputStream);
                final JsonMetaSchema jsonMetaSchema = findMetaSchemaForSchema(schemaNode);

                if (idMatchesSourceUrl(jsonMetaSchema, schemaNode, schemaURL)) {
                    
                    return new JsonSchema(new ValidationContext(jsonMetaSchema, this), schemaNode, true /*retrieved via id, resolving will not change anything*/);
                }

                return newJsonSchema(schemaNode, config);
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

    public JsonSchema getSchema(URL schemaURL) {
        return getSchema(schemaURL, null);
    }

    public JsonSchema getSchema(JsonNode jsonNode, SchemaValidatorsConfig config) {
        return newJsonSchema(jsonNode, config);
    }

    public JsonSchema getSchema(JsonNode jsonNode) {
        return newJsonSchema(jsonNode, null);
    }

    private boolean idMatchesSourceUrl(JsonMetaSchema metaSchema, JsonNode schema, URL schemaUrl) {

        String id = metaSchema.readId(schema);
        if (id == null || id.isEmpty()) {
            return false;
        }
        boolean result = id.equals(schemaUrl.toString());
        if (logger.isDebugEnabled()) {
            logger.debug("Matching " + id + " to " + schemaUrl.toString() + ": " + result);
        }
        return result;
    }
}
