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

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class AnyOfValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RequiredValidator.class);
    private static final String DISCRIMINATOR_REMARK = "and the discriminator-selected candidate schema didn't pass validation";

    private final List<JsonSchema> schemas = new ArrayList<>();
    private final ValidationContext.DiscriminatorContext discriminatorContext;

    public AnyOfValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ANY_OF, validationContext);
        this.validationContext = validationContext;
        int size = schemaNode.size();
        for (int i = 0; i < size; i++) {
            this.schemas.add(validationContext.newSchema(schemaPath + "/" + i, schemaNode.get(i), parentSchema));
        }

        if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
            this.discriminatorContext = new ValidationContext.DiscriminatorContext();
        } else {
            this.discriminatorContext = null;
        }
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);
        CollectorContext collectorContext = CollectorContext.getInstance();

        // get the Validator state object storing validation data
        ValidatorState state = (ValidatorState) collectorContext.get(ValidatorState.VALIDATOR_STATE_KEY);

        if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
            this.validationContext.enterDiscriminatorContext(this.discriminatorContext, at);
        }

        boolean initialHasMatchedNode = state.hasMatchedNode();

        Set<ValidationMessage> allErrors = new LinkedHashSet<>();

        // As anyOf might contain multiple schemas take a backup of evaluated stuff.
        Collection<String> backupEvaluatedItems = collectorContext.getEvaluatedItems();
        Collection<String> backupEvaluatedProperties = collectorContext.getEvaluatedProperties();

        // Make the evaluated lists empty.
        collectorContext.resetEvaluatedItems();
        collectorContext.resetEvaluatedProperties();

        try {
            int numberOfValidSubSchemas = 0;
            for (int i = 0; i < this.schemas.size(); ++i) {
                JsonSchema schema = this.schemas.get(i);
                state.setMatchedNode(initialHasMatchedNode);
                Set<ValidationMessage> errors;

                if (schema.hasTypeValidator()) {
                    TypeValidator typeValidator = schema.getTypeValidator();
                    //If schema has type validator and node type doesn't match with schemaType then ignore it
                    //For union type, it is a must to call TypeValidator
                    if (typeValidator.getSchemaType() != JsonType.UNION && !typeValidator.equalsToSchemaType(node)) {
                        allErrors.add(buildValidationMessage(at, typeValidator.getSchemaType().toString()));
                        continue;
                    }
                }
                if (!state.isWalkEnabled()) {
                    errors = schema.validate(node, rootNode, at);
                } else {
                    errors = schema.walk(node, rootNode, at, true);
                }

                // check if any validation errors have occurred
                if (errors.isEmpty()) {
                    // check whether there are no errors HOWEVER we have validated the exact validator
                    if (!state.hasMatchedNode()) {
                        continue;
                    }
                    // we found a valid subschema, so increase counter
                    numberOfValidSubSchemas++;
                }

                if (errors.isEmpty() && (!this.validationContext.getConfig().isOpenAPI3StyleDiscriminators())) {
                    // Clear all errors.
                    allErrors.clear();
                    // return empty errors.
                    return errors;
                } else if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
                    if (this.discriminatorContext.isDiscriminatorMatchFound()) {
                        if (!errors.isEmpty()) {
                            errors.add(buildValidationMessage(at, DISCRIMINATOR_REMARK));
                            allErrors.addAll(errors);
                        } else {
                            // Clear all errors.
                            allErrors.clear();
                        }
                        return errors;
                    }
                }
                allErrors.addAll(errors);
            }

            // determine only those errors which are NOT of type "required" property missing
            Set<ValidationMessage> childNotRequiredErrors = allErrors.stream().filter(error -> !ValidatorTypeCode.REQUIRED.getValue().equals(error.getType())).collect(Collectors.toSet());

            // in case we had at least one (anyOf, i.e. any number >= 1 of) valid subschemas, we can remove all other errors about "required" properties
            if (numberOfValidSubSchemas >= 1 && childNotRequiredErrors.isEmpty()) {
                allErrors = childNotRequiredErrors;
            }

            if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators() && this.discriminatorContext.isActive()) {
                final Set<ValidationMessage> errors = new HashSet<>();
                errors.add(buildValidationMessage(at, "based on the provided discriminator. No alternative could be chosen based on the discriminator property"));
                return Collections.unmodifiableSet(errors);
            }
        } finally {
            if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
                this.validationContext.leaveDiscriminatorContextImmediately(at);
            }
            if (allErrors.isEmpty()) {
                state.setMatchedNode(true);
            } else {
                collectorContext.getEvaluatedItems().clear();
                collectorContext.getEvaluatedProperties().clear();
            }
            collectorContext.getEvaluatedItems().addAll(backupEvaluatedItems);
            collectorContext.getEvaluatedProperties().addAll(backupEvaluatedProperties);
        }
        return Collections.unmodifiableSet(allErrors);
    }

    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            return validate(node, rootNode, at);
        }
        for (JsonSchema schema : this.schemas) {
            schema.walk(node, rootNode, at, false);
        }
        return new LinkedHashSet<>();
    }

    @Override
    public void preloadJsonSchema() {
        preloadJsonSchemas(this.schemas);
    }
}