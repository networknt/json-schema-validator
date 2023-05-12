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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllOfValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(AllOfValidator.class);

    private final List<JsonSchema> schemas = new ArrayList<>();

    public AllOfValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ALL_OF, validationContext);
        this.validationContext = validationContext;
        int size = schemaNode.size();
        for (int i = 0; i < size; i++) {
            this.schemas.add(new JsonSchema(validationContext,
                                       schemaPath + "/" + i,
                                       parentSchema.getCurrentUri(),
                                       schemaNode.get(i),
                                       parentSchema));
        }
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);
        CollectorContext collectorContext = CollectorContext.getInstance();

        // get the Validator state object storing validation data
        ValidatorState state = (ValidatorState) collectorContext.get(ValidatorState.VALIDATOR_STATE_KEY);

        Set<ValidationMessage> childSchemaErrors = new LinkedHashSet<>();

        Collection<String> newEvaluatedItems = Collections.emptyList();
        Collection<String> newEvaluatedProperties = Collections.emptyList();

        for (JsonSchema schema : this.schemas) {
            // As AllOf might contain multiple schemas take a backup of evaluated stuff.
            Collection<String> backupEvaluatedItems = collectorContext.getEvaluatedItems();
            Collection<String> backupEvaluatedProperties = collectorContext.getEvaluatedProperties();

            Set<ValidationMessage> localErrors = new HashSet<>();

            try {
                // Make the evaluated lists empty.
                collectorContext.resetEvaluatedItems();
                collectorContext.resetEvaluatedProperties();

                if (!state.isWalkEnabled()) {
                    localErrors = schema.validate(node, rootNode, at);
                } else {
                    localErrors = schema.walk(node, rootNode, at, true);
                }

                childSchemaErrors.addAll(localErrors);

                // Keep Collecting total evaluated properties.
                if (localErrors.isEmpty()) {
                    newEvaluatedItems = collectorContext.getEvaluatedItems();
                    newEvaluatedProperties = collectorContext.getEvaluatedProperties();
                }

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
                                    registerAndMergeDiscriminator(currentDiscriminatorContext, discriminator, this.parentSchema, at);
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
                collectorContext.setEvaluatedItems(backupEvaluatedItems);
                collectorContext.setEvaluatedProperties(backupEvaluatedProperties);
                if (localErrors.isEmpty()) {
                    collectorContext.getEvaluatedItems().addAll(newEvaluatedItems);
                    collectorContext.getEvaluatedProperties().addAll(newEvaluatedProperties);
                }
                newEvaluatedItems = Collections.emptyList();
                newEvaluatedProperties = Collections.emptyList();
            }
        }

        return Collections.unmodifiableSet(childSchemaErrors);
    }

    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            return validate(node, rootNode, at);
        }
        for (JsonSchema schema : this.schemas) {
            // Walk through the schema
            schema.walk(node, rootNode, at, false);
        }
        return Collections.emptySet();
    }

    @Override
    public void preloadJsonSchema() {
        preloadJsonSchemas(this.schemas);
    }
}
