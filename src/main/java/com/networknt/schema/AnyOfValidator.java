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

public class AnyOfValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RequiredValidator.class);
    private static final String REMARK = "Remaining validation messages report why candidate schemas didn't match";
    private static final String DISCRIMINATOR_REMARK = "and the discriminator-selected candidate schema didn't pass validation";

    private final List<JsonSchema> schemas = new ArrayList<JsonSchema>();
    private final ValidationContext.DiscriminatorContext discriminatorContext;

    public AnyOfValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ANY_OF, validationContext);
        this.validationContext = validationContext;
        int size = schemaNode.size();
        for (int i = 0; i < size; i++) {
            schemas.add(new JsonSchema(validationContext,
                    getValidatorType().getValue(),
                    parentSchema.getCurrentUri(),
                    schemaNode.get(i),
                    parentSchema));
        }

        if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
            this.discriminatorContext = new ValidationContext.DiscriminatorContext();
        } else {
            this.discriminatorContext = null;
        }
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        // get the Validator state object storing validation data
        ValidatorState state = (ValidatorState) CollectorContext.getInstance().get(ValidatorState.VALIDATOR_STATE_KEY);

        if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
            validationContext.enterDiscriminatorContext(this.discriminatorContext, at);
        }

        Set<ValidationMessage> allErrors = new LinkedHashSet<ValidationMessage>();
        String typeValidatorName = "anyOf/type";

        // As anyOf might contain multiple schemas take a backup of evaluatedProperties.
        Object backupEvaluatedProperties = CollectorContext.getInstance().get(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES);

        // Make the evaluatedProperties list empty.
        CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, new ArrayList<>());

        try {
            for (JsonSchema schema : schemas) {
                Set<ValidationMessage> errors = new HashSet<>();
                if (schema.getValidators().containsKey(typeValidatorName)) {
                    TypeValidator typeValidator = ((TypeValidator) schema.getValidators().get(typeValidatorName));
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
                if (errors.isEmpty() && (!this.validationContext.getConfig().isOpenAPI3StyleDiscriminators())) {
                    // Clear all errors.
                    allErrors.clear();
                    // return empty errors.
                    return errors;
                } else if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
                    if (discriminatorContext.isDiscriminatorMatchFound()) {
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

            if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators() && discriminatorContext.isActive()) {
                final Set<ValidationMessage> errors = new HashSet<ValidationMessage>();
                errors.add(buildValidationMessage(at, "based on the provided discriminator. No alternative could be chosen based on the discriminator property"));
                return Collections.unmodifiableSet(errors);
            }
        } finally {
            if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
                validationContext.leaveDiscriminatorContextImmediately(at);
            }
            if (allErrors.isEmpty()) {
                addEvaluatedProperties(backupEvaluatedProperties);
            } else {
                CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, backupEvaluatedProperties);
            }
        }
        return Collections.unmodifiableSet(allErrors);
    }

    private void addEvaluatedProperties(Object backupEvaluatedProperties) {
        // Add all the evaluated properties.
        List<String> backupEvaluatedPropertiesList = (backupEvaluatedProperties == null ? new ArrayList<>() : (List<String>) backupEvaluatedProperties);
        backupEvaluatedPropertiesList.addAll((List<String>) CollectorContext.getInstance().get(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES));
        CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, backupEvaluatedPropertiesList);
    }

    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            return validate(node, rootNode, at);
        }
        for (JsonSchema schema : schemas) {
            schema.walk(node, rootNode, at, false);
        }
        return new LinkedHashSet<>();
    }

    @Override
    public void preloadJsonSchema() {
        preloadJsonSchemas(schemas);
    }
}
