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

/**
 * {@link JsonValidator} for oneOf.
 */
public class OneOfValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(OneOfValidator.class);

    private final List<JsonSchema> schemas = new ArrayList<>();

    private Boolean canShortCircuit = null;

    public OneOfValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.ONE_OF, validationContext);
        int size = schemaNode.size();
        for (int i = 0; i < size; i++) {
            JsonNode childNode = schemaNode.get(i);
            this.schemas.add(validationContext.newSchema( schemaLocation.append(i), evaluationPath.append(i), childNode, parentSchema));
        }
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        Set<ValidationMessage> errors = new LinkedHashSet<>();

        debug(logger, node, rootNode, instanceLocation);

        ValidatorState state = executionContext.getValidatorState();

        // this is a complex validator, we set the flag to true
        state.setComplexValidator(true);

        int numberOfValidSchema = 0;
        Set<ValidationMessage> childErrors = new LinkedHashSet<>();

        // Save flag as nested schema evaluation shouldn't trigger fail fast
        boolean failFast = executionContext.isFailFast();
        try {
            executionContext.setFailFast(false);
            for (JsonSchema schema : this.schemas) {
                Set<ValidationMessage> schemaErrors = Collections.emptySet();

                // Reset state in case the previous validator did not match
                state.setMatchedNode(true);

                if (!state.isWalkEnabled()) {
                    schemaErrors = schema.validate(executionContext, node, rootNode, instanceLocation);
                } else {
                    schemaErrors = schema.walk(executionContext, node, rootNode, instanceLocation,
                            state.isValidationEnabled());
                }

                // check if any validation errors have occurred
                if (schemaErrors.isEmpty()) {
                    // check whether there are no errors HOWEVER we have validated the exact
                    // validator
                    if (!state.hasMatchedNode()) {
                        continue;
                    }
                    numberOfValidSchema++;
                }

                if (numberOfValidSchema > 1 && canShortCircuit()) {
                    // short-circuit
                    break;
                }

                if (reportChildErrors(executionContext)) {
                    childErrors.addAll(schemaErrors);
                }
            }
        } finally {
            // Restore flag
            executionContext.setFailFast(failFast);
        }

        // ensure there is always an "OneOf" error reported if number of valid schemas
        // is not equal to 1.
        if (numberOfValidSchema != 1) {
            ValidationMessage message = message().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .failFast(executionContext.isFailFast())
                    .arguments(Integer.toString(numberOfValidSchema)).build();
            errors.add(message);
            errors.addAll(childErrors);
        }

        // Make sure to signal parent handlers we matched
        if (errors.isEmpty()) {
            state.setMatchedNode(true);
        }

        // reset the ValidatorState object
        resetValidatorState(executionContext);

        return Collections.unmodifiableSet(errors);
    }

    /**
     * Determines if child errors should be reported.
     * 
     * @param executionContext the execution context
     * @return true if child errors should be reported
     */
    protected boolean reportChildErrors(ExecutionContext executionContext) {
        // check the original flag if it's going to fail fast anyway
        // no point aggregating all the errors
        return !executionContext.getExecutionConfig().isFailFast();
    }
    
    protected boolean canShortCircuit() {
        if (this.canShortCircuit == null) {
            boolean canShortCircuit = true;
            for (JsonValidator validator : getEvaluationParentSchema().getValidators()) {
                if ("unevaluatedProperties".equals(validator.getKeyword())
                        || "unevaluatedItems".equals(validator.getKeyword())) {
                    canShortCircuit = false;
                }
            }
            this.canShortCircuit = canShortCircuit;
        }
        return this.canShortCircuit;
    }

    private static void resetValidatorState(ExecutionContext executionContext) {
        ValidatorState state = executionContext.getValidatorState();
        state.setComplexValidator(false);
        state.setMatchedNode(true);
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        HashSet<ValidationMessage> validationMessages = new LinkedHashSet<>();
        if (shouldValidateSchema) {
            validationMessages.addAll(validate(executionContext, node, rootNode, instanceLocation));
        } else {
            for (JsonSchema schema : this.schemas) {
                schema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
            }
        }
        return validationMessages;
    }

    @Override
    public void preloadJsonSchema() {
        for (JsonSchema schema: this.schemas) {
            schema.initializeValidators();
        }
        canShortCircuit(); // cache the flag
    }
}
