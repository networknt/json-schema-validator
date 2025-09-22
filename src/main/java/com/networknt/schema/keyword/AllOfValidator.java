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

package com.networknt.schema.keyword;

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.DiscriminatorContext;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonType;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.TypeFactory;
import com.networknt.schema.ValidationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link KeywordValidator} for allOf.
 */
public class AllOfValidator extends BaseKeywordValidator {
    private static final Logger logger = LoggerFactory.getLogger(AllOfValidator.class);

    private final List<JsonSchema> schemas;

    public AllOfValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(ValidatorTypeCode.ALL_OF, schemaNode, schemaLocation, parentSchema, validationContext, evaluationPath);
        if (!schemaNode.isArray()) {
            JsonType nodeType = TypeFactory.getValueNodeType(schemaNode, this.validationContext.getConfig());
            throw new JsonSchemaException(error().instanceNode(schemaNode)
                    .instanceLocation(schemaLocation.getFragment())
                    .messageKey("type")
                    .arguments(nodeType.toString(), "array")
                    .build());
        }
        int size = schemaNode.size();
        this.schemas = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.schemas.add(validationContext.newSchema(schemaLocation.append(i), evaluationPath.append(i),
                    schemaNode.get(i), parentSchema));
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        validate(executionContext, node, rootNode, instanceLocation, false);
    }

    protected void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean walk) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        for (JsonSchema schema : this.schemas) {
            if (!walk) {
                schema.validate(executionContext, node, rootNode, instanceLocation);
            } else {
                schema.walk(executionContext, node, rootNode, instanceLocation, true);
            }
            if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()) {
                final Iterator<JsonNode> arrayElements = this.schemaNode.elements();
                while (arrayElements.hasNext()) {
                    final ObjectNode allOfEntry = (ObjectNode) arrayElements.next();
                    final JsonNode $ref = allOfEntry.get("$ref");
                    if (null != $ref) {
                        final DiscriminatorContext currentDiscriminatorContext = executionContext
                                .getCurrentDiscriminatorContext();
                        if (null != currentDiscriminatorContext) {
                            final ObjectNode discriminator = currentDiscriminatorContext
                                    .getDiscriminatorForPath(allOfEntry.get("$ref").asText());
                            if (null != discriminator) {
                                DiscriminatorValidator.registerAndMergeDiscriminator(currentDiscriminatorContext, discriminator,
                                        this.parentSchema, instanceLocation);
                                // now we have to check whether we have hit the right target
                                final String discriminatorPropertyName = discriminator.get("propertyName").asText();
                                final JsonNode discriminatorNode = node.get(discriminatorPropertyName);
                                final String discriminatorPropertyValue = discriminatorNode == null ? null
                                        : discriminatorNode.textValue();

                                final JsonSchema jsonSchema = this.parentSchema;
                                DiscriminatorValidator.checkDiscriminatorMatch(currentDiscriminatorContext, discriminator,
                                        discriminatorPropertyValue, jsonSchema);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        if (shouldValidateSchema && node != null) {
            validate(executionContext, node, rootNode, instanceLocation, true);
            return;
        }
        for (JsonSchema schema : this.schemas) {
            // Walk through the schema
            schema.walk(executionContext, node, rootNode, instanceLocation, false);
        }
   }

    @Override
    public void preloadJsonSchema() {
        preloadJsonSchemas(this.schemas);
    }
}
