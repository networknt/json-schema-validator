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
import java.util.Stack;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.uri.URIFactory;
import com.networknt.schema.urn.URNFactory;

public class ValidationContext {
    private final URIFactory uriFactory;
    private final URNFactory urnFactory;
    private final JsonMetaSchema metaSchema;
    private final JsonSchemaFactory jsonSchemaFactory;
    private SchemaValidatorsConfig config;
    private final Map<String, JsonSchemaRef> refParsingInProgress = new HashMap<String, JsonSchemaRef>();
    private final Stack<DiscriminatorContext> discriminatorContexts = new Stack<DiscriminatorContext>();

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

    public JsonValidator newValidator(String schemaPath, String keyword /* keyword */, JsonNode schemaNode,
                                      JsonSchema parentSchema, String customMessage) {
        return metaSchema.newValidator(this, schemaPath, keyword, schemaNode, parentSchema, customMessage);
    }

    public String resolveSchemaId(JsonNode schemaNode) {
        return metaSchema.readId(schemaNode);
    }

    public URIFactory getURIFactory() {
        return this.uriFactory;
    }

    public URNFactory getURNFactory() {
        return this.urnFactory;
    }

    public JsonSchemaFactory getJsonSchemaFactory() {
        return jsonSchemaFactory;
    }

    public SchemaValidatorsConfig getConfig() {
        if (config == null) {
            config = new SchemaValidatorsConfig();
        }
        return config;
    }

    public void setConfig(SchemaValidatorsConfig config) {
        this.config = config;
    }

    public void setReferenceParsingInProgress(String refValue, JsonSchemaRef ref) {
        refParsingInProgress.put(refValue, ref);
    }

    public JsonSchemaRef getReferenceParsingInProgress(String refValue) {
        return refParsingInProgress.get(refValue);
    }

    public DiscriminatorContext getCurrentDiscriminatorContext() {
        if (!discriminatorContexts.empty()) {
            return discriminatorContexts.peek();
        }
        return null; // this is the case when we get on a schema that has a discriminator, but it's not used in anyOf
    }

    public void enterDiscriminatorContext(final DiscriminatorContext ctx, String at) {
        discriminatorContexts.push(ctx);
    }

    public void leaveDiscriminatorContextImmediately(String at) {
        discriminatorContexts.pop();
    }

    protected JsonMetaSchema getMetaSchema() {
        return metaSchema;
    }

    public static class DiscriminatorContext {
        private final Map<String, ObjectNode> discriminators = new HashMap<String, ObjectNode>();

        private boolean discriminatorMatchFound = false;

        public void registerDiscriminator(final String schemaPath, final ObjectNode discriminator) {
            discriminators.put(schemaPath, discriminator);
        }

        public ObjectNode getDiscriminatorForPath(final String schemaPath) {
            return discriminators.get(schemaPath);
        }

        public void markMatch() {
            discriminatorMatchFound = true;
        }

        public boolean isDiscriminatorMatchFound() {
            return discriminatorMatchFound;
        }

        /**
         * Returns true if we have a discriminator active. In this case no valid match in anyOf should lead to validation failure
         *
         * @return true in case there are discriminator candidates
         */
        public boolean isActive() {
            return !discriminators.isEmpty();
        }
    }
}
