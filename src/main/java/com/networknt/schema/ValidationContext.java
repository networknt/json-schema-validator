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

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.uri.URIFactory;
import com.networknt.schema.urn.URNFactory;

public class ValidationContext {
    private final URIFactory uriFactory;
    private final URNFactory urnFactory;
    private final JsonMetaSchema metaSchema;
    private final JsonSchemaFactory jsonSchemaFactory;
    private final SchemaValidatorsConfig config;
    private final ConcurrentMap<String, JsonSchema> schemaReferences;
    private final ConcurrentMap<String, JsonSchema> schemaResources;

    public ValidationContext(URIFactory uriFactory, URNFactory urnFactory, JsonMetaSchema metaSchema,
            JsonSchemaFactory jsonSchemaFactory, SchemaValidatorsConfig config) {
        this(uriFactory, urnFactory, metaSchema, jsonSchemaFactory, config, new ConcurrentHashMap<>(),
                new ConcurrentHashMap<>());
    }

    public ValidationContext(URIFactory uriFactory, URNFactory urnFactory, JsonMetaSchema metaSchema,
            JsonSchemaFactory jsonSchemaFactory, SchemaValidatorsConfig config,
            ConcurrentMap<String, JsonSchema> schemaReferences, ConcurrentMap<String, JsonSchema> schemaResources) {
        if (uriFactory == null) {
            throw new IllegalArgumentException("URIFactory must not be null");
        }
        if (metaSchema == null) {
            throw new IllegalArgumentException("JsonMetaSchema must not be null");
        }
        if (jsonSchemaFactory == null) {
            throw new IllegalArgumentException("JsonSchemaFactory must not be null");
        }
        this.uriFactory = uriFactory;
        this.urnFactory = urnFactory;
        this.metaSchema = metaSchema;
        this.jsonSchemaFactory = jsonSchemaFactory;
        this.config = config == null ? new SchemaValidatorsConfig() : config;
        this.schemaReferences = schemaReferences;
        this.schemaResources = schemaResources;
    }

    public JsonSchema newSchema(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema) {
        return getJsonSchemaFactory().create(this, schemaLocation, evaluationPath, schemaNode, parentSchema);
    }

    public JsonSchema newSchema(SchemaLocation schemaLocation, JsonNodePath evaluationPath, URI currentUri, JsonNode schemaNode, JsonSchema parentSchema) {
        return getJsonSchemaFactory().create(this, schemaLocation, evaluationPath, currentUri, schemaNode, parentSchema);
    }

    public JsonValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath,
            String keyword /* keyword */, JsonNode schemaNode, JsonSchema parentSchema) {
        return this.metaSchema.newValidator(this, schemaLocation, evaluationPath, keyword, schemaNode, parentSchema);
    }

    public String resolveSchemaId(JsonNode schemaNode) {
        return this.metaSchema.readId(schemaNode);
    }

    public URIFactory getURIFactory() {
        return this.uriFactory;
    }

    public URNFactory getURNFactory() {
        return this.urnFactory;
    }

    public JsonSchemaFactory getJsonSchemaFactory() {
        return this.jsonSchemaFactory;
    }

    public SchemaValidatorsConfig getConfig() {
        return this.config;
    }

    /**
     * Gets the schema references identified by the ref uri.
     *
     * @return the schema references
     */
    public ConcurrentMap<String, JsonSchema> getSchemaReferences() {
        return this.schemaReferences;
    }

    /**
     * Gets the schema resources identified by id.
     *
     * @return the schema resources
     */
    public ConcurrentMap<String, JsonSchema> getSchemaResources() {
        return this.schemaResources;
    }

    public JsonMetaSchema getMetaSchema() {
        return this.metaSchema;
    }

    public Optional<VersionFlag> activeDialect() {
        String metaSchema = getMetaSchema().getUri();
        return SpecVersionDetector.detectOptionalVersion(metaSchema);
    }
}
