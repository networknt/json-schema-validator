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

public class AllOfValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(AllOfValidator.class);

    private final List<JsonSchema> schemas = new ArrayList<JsonSchema>();

    public AllOfValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ALL_OF, validationContext);
        this.validationContext = validationContext;
        int size = schemaNode.size();
        for (int i = 0; i < size; i++) {
            schemas.add(new JsonSchema(validationContext,
                                       getValidatorType().getValue(),
                                       parentSchema.getCurrentUri(),
                                       schemaNode.get(i),
                                       parentSchema));
        }
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        // get the Validator state object storing validation data
        ValidatorState state = (ValidatorState) CollectorContext.getInstance().get(ValidatorState.VALIDATOR_STATE_KEY);

        Set<ValidationMessage> childSchemaErrors = new LinkedHashSet<ValidationMessage>();

        // As AllOf might contain multiple schemas take a backup of evaluatedProperties.
        Object backupEvaluatedProperties = CollectorContext.getInstance().get(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES);

        List<String> totalEvaluatedProperties = new ArrayList<>();

        for (JsonSchema schema : schemas) {
            try {
                // Make the evaluatedProperties list empty.
                CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, new ArrayList<>());

                Set<ValidationMessage> localErrors = new HashSet<>();

                if (!state.isWalkEnabled()) {
                    localErrors = schema.validate(node, rootNode, at);
                } else {
                    localErrors = schema.walk(node, rootNode, at, true);
                }

                childSchemaErrors.addAll(localErrors);

                // Keep Collecting total evaluated properties.
                if (localErrors.isEmpty()) {
                    totalEvaluatedProperties.addAll((List<String>) CollectorContext.getInstance().get(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES));
                }

                if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
                    final Iterator<JsonNode> arrayElements = schemaNode.elements();
                    while (arrayElements.hasNext()) {
                        final ObjectNode allOfEntry = (ObjectNode) arrayElements.next();
                        final JsonNode $ref = allOfEntry.get("$ref");
                        if (null != $ref) {
                            final ValidationContext.DiscriminatorContext currentDiscriminatorContext = validationContext
                                    .getCurrentDiscriminatorContext();
                            if (null != currentDiscriminatorContext) {
                                final ObjectNode discriminator = currentDiscriminatorContext
                                        .getDiscriminatorForPath(allOfEntry.get("$ref").asText());
                                if (null != discriminator) {
                                    registerAndMergeDiscriminator(currentDiscriminatorContext, discriminator, parentSchema, at);
                                    // now we have to check whether we have hit the right target
                                    final String discriminatorPropertyName = discriminator.get("propertyName").asText();
                                    final JsonNode discriminatorNode = node.get(discriminatorPropertyName);
                                    final String discriminatorPropertyValue = discriminatorNode == null
                                            ? null
                                            : discriminatorNode.textValue();

                                    final JsonSchema jsonSchema = parentSchema;
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
                if (childSchemaErrors.isEmpty()) {
                    List<String> backupEvaluatedPropertiesList = (backupEvaluatedProperties == null ? new ArrayList<>() : (List<String>) backupEvaluatedProperties);
                    backupEvaluatedPropertiesList.addAll(totalEvaluatedProperties);
                    CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, backupEvaluatedPropertiesList);
                } else {
                    CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, backupEvaluatedProperties);
                }
            }
        }

        return Collections.unmodifiableSet(childSchemaErrors);
    }

    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            return validate(node, rootNode, at);
        }
        for (JsonSchema schema : schemas) {
            // Walk through the schema
            schema.walk(node, rootNode, at, false);
        }
        return Collections.emptySet();
    }

    @Override
    public void preloadJsonSchema() {
        preloadJsonSchemas(schemas);
    }
}
