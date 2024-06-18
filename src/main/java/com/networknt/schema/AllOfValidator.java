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

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.utils.SetView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link JsonValidator} for allOf.
 */
public class AllOfValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(AllOfValidator.class);

    private final List<JsonSchema> schemas;

    public AllOfValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.ALL_OF, validationContext);
        if (!schemaNode.isArray()) {
            JsonType nodeType = TypeFactory.getValueNodeType(schemaNode, this.validationContext.getConfig());
            throw new JsonSchemaException(message().instanceNode(schemaNode)
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
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        return validate(executionContext, node, rootNode, instanceLocation, false);
    }

    protected Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean walk) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        SetView<ValidationMessage> childSchemaErrors = null;

        for (JsonSchema schema : this.schemas) {
            Set<ValidationMessage> localErrors = null;

            if (!walk) {
                localErrors = schema.validate(executionContext, node, rootNode, instanceLocation);
            } else {
                localErrors = schema.walk(executionContext, node, rootNode, instanceLocation, true);
            }
            
            if (localErrors != null && !localErrors.isEmpty()) {
                if (childSchemaErrors == null) {
                    childSchemaErrors = new SetView<>();
                }
                childSchemaErrors.union(localErrors);
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
                                registerAndMergeDiscriminator(currentDiscriminatorContext, discriminator,
                                        this.parentSchema, instanceLocation);
                                // now we have to check whether we have hit the right target
                                final String discriminatorPropertyName = discriminator.get("propertyName").asText();
                                final JsonNode discriminatorNode = node.get(discriminatorPropertyName);
                                final String discriminatorPropertyValue = discriminatorNode == null ? null
                                        : discriminatorNode.textValue();

                                final JsonSchema jsonSchema = this.parentSchema;
                                checkDiscriminatorMatch(currentDiscriminatorContext, discriminator,
                                        discriminatorPropertyValue, jsonSchema);
                            }
                        }
                    }
                }
            }
        }

        return childSchemaErrors != null ? childSchemaErrors : Collections.emptySet();
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            return validate(executionContext, node, rootNode, instanceLocation, true);
        }
        for (JsonSchema schema : this.schemas) {
            // Walk through the schema
            schema.walk(executionContext, node, rootNode, instanceLocation, false);
        }
        return Collections.emptySet();
    }

    @Override
    public void preloadJsonSchema() {
        preloadJsonSchemas(this.schemas);
    }
}
