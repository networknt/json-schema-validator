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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.SpecVersion.VersionFlag;
import com.networknt.schema.uri.URIFactory;
import com.networknt.schema.urn.URNFactory;

public class ValidationContext {
    private final URIFactory uriFactory;
    private final URNFactory urnFactory;
    private final JsonMetaSchema metaSchema;
    private final JsonSchemaFactory jsonSchemaFactory;
    private SchemaValidatorsConfig config;
    private final Map<String, JsonSchemaRef> refParsingInProgress = new HashMap<>();
    private final Stack<DiscriminatorContext> discriminatorContexts = new Stack<>();

    public ValidationContext(URIFactory uriFactory, URNFactory urnFactory, JsonMetaSchema metaSchema,
                             JsonSchemaFactory jsonSchemaFactory, SchemaValidatorsConfig config) {
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
        this.config = config;
    }

    public JsonSchema newSchema(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema) {
        return getJsonSchemaFactory().create(this, schemaPath, schemaNode, parentSchema);
    }

    public JsonValidator newValidator(String schemaPath, String keyword /* keyword */, JsonNode schemaNode,
                                      JsonSchema parentSchema, Map<String, String> customMessage) {
        return this.metaSchema.newValidator(this, schemaPath, keyword, schemaNode, parentSchema, customMessage);
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
        if (this.config == null) {
            this.config = new SchemaValidatorsConfig();
        }
        return this.config;
    }

    public void setConfig(SchemaValidatorsConfig config) {
        this.config = config;
    }

    public void setReferenceParsingInProgress(String refValue, JsonSchemaRef ref) {
        this.refParsingInProgress.put(refValue, ref);
    }

    public JsonSchemaRef getReferenceParsingInProgress(String refValue) {
        return this.refParsingInProgress.get(refValue);
    }

    public DiscriminatorContext getCurrentDiscriminatorContext() {
        if (!this.discriminatorContexts.empty()) {
            return this.discriminatorContexts.peek();
        }
        return null; // this is the case when we get on a schema that has a discriminator, but it's not used in anyOf
    }

    public void enterDiscriminatorContext(final DiscriminatorContext ctx, @SuppressWarnings("unused") JsonNodePath at) {
        this.discriminatorContexts.push(ctx);
    }

    public void leaveDiscriminatorContextImmediately(@SuppressWarnings("unused") JsonNodePath at) {
        this.discriminatorContexts.pop();
    }

    public JsonMetaSchema getMetaSchema() {
        return this.metaSchema;
    }

    public Optional<VersionFlag> activeDialect() {
        String metaSchema = getMetaSchema().getUri();
        return SpecVersionDetector.detectOptionalVersion(metaSchema);
    }

    public static class DiscriminatorContext {
        private final Map<String, ObjectNode> discriminators = new HashMap<>();

        private boolean discriminatorMatchFound = false;

        public void registerDiscriminator(final String schemaPath, final ObjectNode discriminator) {
            this.discriminators.put(schemaPath, discriminator);
        }

        public ObjectNode getDiscriminatorForPath(final String schemaPath) {
            return this.discriminators.get(schemaPath);
        }

        public void markMatch() {
            this.discriminatorMatchFound = true;
        }

        public boolean isDiscriminatorMatchFound() {
            return this.discriminatorMatchFound;
        }

        /**
         * Returns true if we have a discriminator active. In this case no valid match in anyOf should lead to validation failure
         *
         * @return true in case there are discriminator candidates
         */
        public boolean isActive() {
            return !this.discriminators.isEmpty();
        }
    }
}
