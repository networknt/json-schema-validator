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

public class OneOfValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(OneOfValidator.class);

    private final List<JsonSchema> schemas = new ArrayList<>();

    public OneOfValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ONE_OF, validationContext);
        int size = schemaNode.size();
        for (int i = 0; i < size; i++) {
            JsonNode childNode = schemaNode.get(i);
            schemas.add(new JsonSchema(validationContext,  schemaPath + "/" + i, parentSchema.getCurrentUri(), childNode, parentSchema));
        }
        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        Set<ValidationMessage> errors = new LinkedHashSet<>();
        CollectorContext collectorContext = CollectorContext.getInstance();

        // As oneOf might contain multiple schemas take a backup of evaluatedProperties.
        Collection<String> backupEvaluatedProperties = collectorContext.getEvaluatedProperties();

        // Make the evaluatedProperties list empty.
        collectorContext.resetEvaluatedProperties();

        try {
            debug(logger, node, rootNode, at);

            ValidatorState state = (ValidatorState) collectorContext.get(ValidatorState.VALIDATOR_STATE_KEY);

            // this is a complex validator, we set the flag to true
            state.setComplexValidator(true);

            int numberOfValidSchema = 0;
            Set<ValidationMessage> childErrors = new LinkedHashSet<>();

            for (JsonSchema schema : schemas) {
                Set<ValidationMessage> schemaErrors = null;
                // Reset state in case the previous validator did not match
                state.setMatchedNode(true);

                if (!state.isWalkEnabled()) {
                    schemaErrors = schema.validate(node, rootNode, at);
                } else {
                    schemaErrors = schema.walk(node, rootNode, at, state.isValidationEnabled());
                }

                // check if any validation errors have occurred
                if (schemaErrors.isEmpty()) {
                    // check whether there are no errors HOWEVER we have validated the exact validator
                    if (!state.hasMatchedNode())
                        continue;

                    numberOfValidSchema++;
                }

                // If the number of valid schema is greater than one, just reset the evaluated properties and break.
                if (numberOfValidSchema > 1) {
                    collectorContext.resetEvaluatedProperties();
                    break;
                }

                childErrors.addAll(schemaErrors);
            }

            // ensure there is always an "OneOf" error reported if number of valid schemas is not equal to 1.
            if (numberOfValidSchema != 1) {
                ValidationMessage message = buildValidationMessage(at, Integer.toString(numberOfValidSchema));
                if (failFast) {
                    throw new JsonSchemaException(message);
                }
                errors.add(message);
                errors.addAll(childErrors);
            }

            // Make sure to signal parent handlers we matched
            if (errors.isEmpty())
                state.setMatchedNode(true);

            // reset the ValidatorState object in the ThreadLocal
            resetValidatorState();

            return Collections.unmodifiableSet(errors);
        } finally {
            if (errors.isEmpty()) {
                collectorContext.getEvaluatedProperties().addAll(backupEvaluatedProperties);
            } else {
                collectorContext.setEvaluatedProperties(backupEvaluatedProperties);
            }
        }
    }

    private void resetValidatorState() {
        ValidatorState state = (ValidatorState) CollectorContext.getInstance().get(ValidatorState.VALIDATOR_STATE_KEY);
        state.setComplexValidator(false);
        state.setMatchedNode(true);
    }

    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        HashSet<ValidationMessage> validationMessages = new LinkedHashSet<ValidationMessage>();
        if (shouldValidateSchema) {
            validationMessages.addAll(validate(node, rootNode, at));
        } else {
            for (JsonSchema schema : schemas) {
                schema.walk(node, rootNode, at, shouldValidateSchema);
            }
        }
        return validationMessages;
    }

    @Override
    public void preloadJsonSchema() {
        for (JsonSchema schema: schemas) {
            schema.initializeValidators();
        }
    }
}
