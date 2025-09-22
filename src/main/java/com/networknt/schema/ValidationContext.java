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

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Specification.Version;
import com.networknt.schema.dialect.Dialect;
import com.networknt.schema.keyword.KeywordValidator;

public class ValidationContext {
    private final Dialect dialect;
    private final SchemaRegistry jsonSchemaFactory;
    private final SchemaValidatorsConfig config;
    private final ConcurrentMap<String, Schema> schemaReferences;
    private final ConcurrentMap<String, Schema> schemaResources;
    private final ConcurrentMap<String, Schema> dynamicAnchors;

    public ValidationContext(Dialect dialect,
                             SchemaRegistry jsonSchemaFactory, SchemaValidatorsConfig config) {
        this(dialect, jsonSchemaFactory, config, new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    }
    
    public ValidationContext(Dialect dialect, SchemaRegistry jsonSchemaFactory,
            SchemaValidatorsConfig config, ConcurrentMap<String, Schema> schemaReferences,
            ConcurrentMap<String, Schema> schemaResources, ConcurrentMap<String, Schema> dynamicAnchors) {
        if (dialect == null) {
            throw new IllegalArgumentException("JsonMetaSchema must not be null");
        }
        if (jsonSchemaFactory == null) {
            throw new IllegalArgumentException("JsonSchemaFactory must not be null");
        }
        this.dialect = dialect;
        this.jsonSchemaFactory = jsonSchemaFactory;
        // The deprecated SchemaValidatorsConfig constructor needs to remain until removed
        this.config = config == null ? new SchemaValidatorsConfig() : config;
        this.schemaReferences = schemaReferences;
        this.schemaResources = schemaResources;
        this.dynamicAnchors = dynamicAnchors;
    }

    public Schema newSchema(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, Schema parentSchema) {
        return getJsonSchemaFactory().create(this, schemaLocation, evaluationPath, schemaNode, parentSchema);
    }

    public KeywordValidator newValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath,
            String keyword /* keyword */, JsonNode schemaNode, Schema parentSchema) {
        return this.dialect.newValidator(this, schemaLocation, evaluationPath, keyword, schemaNode, parentSchema);
    }

    public String resolveSchemaId(JsonNode schemaNode) {
        return this.dialect.readId(schemaNode);
    }

    public SchemaRegistry getJsonSchemaFactory() {
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
    public ConcurrentMap<String, Schema> getSchemaReferences() {
        return this.schemaReferences;
    }

    /**
     * Gets the schema resources identified by id.
     *
     * @return the schema resources
     */
    public ConcurrentMap<String, Schema> getSchemaResources() {
        return this.schemaResources;
    }

    /**
     * Gets the dynamic anchors.
     *
     * @return the dynamic anchors
     */
    public ConcurrentMap<String, Schema> getDynamicAnchors() {
        return this.dynamicAnchors;
    }

    public Dialect getMetaSchema() {
        return this.dialect;
    }

    public Optional<Version> activeDialect() {
        return Optional.of(this.dialect.getSpecification());
    }
}
