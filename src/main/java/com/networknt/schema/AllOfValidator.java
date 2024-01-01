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
import com.networknt.schema.CollectorContext.Scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllOfValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(AllOfValidator.class);

    private final List<JsonSchema> schemas = new ArrayList<>();

    public AllOfValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.ALL_OF, validationContext);
        this.validationContext = validationContext;
        int size = schemaNode.size();
        for (int i = 0; i < size; i++) {
            this.schemas.add(validationContext.newSchema(schemaLocation.resolve(i), evaluationPath.resolve(i),
                    schemaNode.get(i), parentSchema));
        }
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, node, rootNode, instanceLocation);
        CollectorContext collectorContext = executionContext.getCollectorContext();

        // get the Validator state object storing validation data
        ValidatorState state = (ValidatorState) collectorContext.get(ValidatorState.VALIDATOR_STATE_KEY);

        Set<ValidationMessage> childSchemaErrors = new LinkedHashSet<>();

        for (JsonSchema schema : this.schemas) {
            Set<ValidationMessage> localErrors = new HashSet<>();

            Scope parentScope = collectorContext.enterDynamicScope();
            try {
                if (!state.isWalkEnabled()) {
                    localErrors = schema.validate(executionContext, node, rootNode, instanceLocation);
                } else {
                    localErrors = schema.walk(executionContext, node, rootNode, instanceLocation, true);
                }

                childSchemaErrors.addAll(localErrors);

                if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
                    final Iterator<JsonNode> arrayElements = this.schemaNode.elements();
                    while (arrayElements.hasNext()) {
                        final ObjectNode allOfEntry = (ObjectNode) arrayElements.next();
                        final JsonNode $ref = allOfEntry.get("$ref");
                        if (null != $ref) {
                            final ValidationContext.DiscriminatorContext currentDiscriminatorContext = this.validationContext
                                    .getCurrentDiscriminatorContext();
                            if (null != currentDiscriminatorContext) {
                                final ObjectNode discriminator = currentDiscriminatorContext
                                        .getDiscriminatorForPath(allOfEntry.get("$ref").asText());
                                if (null != discriminator) {
                                    registerAndMergeDiscriminator(currentDiscriminatorContext, discriminator, this.parentSchema, instanceLocation);
                                    // now we have to check whether we have hit the right target
                                    final String discriminatorPropertyName = discriminator.get("propertyName").asText();
                                    final JsonNode discriminatorNode = node.get(discriminatorPropertyName);
                                    final String discriminatorPropertyValue = discriminatorNode == null
                                            ? null
                                            : discriminatorNode.textValue();

                                    final JsonSchema jsonSchema = this.parentSchema;
                                    checkDiscriminatorMatch(
                                            currentDiscriminatorContext,
                                            discriminator,
                                            discriminatorPropertyValue,
                                            jsonSchema);
                                }
                            }
                        }
                    }
                }
            } finally {
                Scope scope = collectorContext.exitDynamicScope();
                if (localErrors.isEmpty()) {
                    parentScope.mergeWith(scope);
                }
            }
        }

        return Collections.unmodifiableSet(childSchemaErrors);
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            return validate(executionContext, node, rootNode, instanceLocation);
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
