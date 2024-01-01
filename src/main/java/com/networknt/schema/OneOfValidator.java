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
import com.networknt.schema.CollectorContext.Scope;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class OneOfValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(OneOfValidator.class);

    private final List<JsonSchema> schemas = new ArrayList<>();

    public OneOfValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.ONE_OF, validationContext);
        int size = schemaNode.size();
        for (int i = 0; i < size; i++) {
            JsonNode childNode = schemaNode.get(i);
            this.schemas.add(validationContext.newSchema( schemaLocation.resolve(i), evaluationPath.resolve(i), childNode, parentSchema));
        }
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        Set<ValidationMessage> errors = new LinkedHashSet<>();
        CollectorContext collectorContext = executionContext.getCollectorContext();

        Scope grandParentScope = collectorContext.enterDynamicScope();
        try {
            debug(logger, node, rootNode, instanceLocation);

            ValidatorState state = (ValidatorState) collectorContext.get(ValidatorState.VALIDATOR_STATE_KEY);

            // this is a complex validator, we set the flag to true
            state.setComplexValidator(true);

            int numberOfValidSchema = 0;
            Set<ValidationMessage> childErrors = new LinkedHashSet<>();

            for (JsonSchema schema : this.schemas) {
                Set<ValidationMessage> schemaErrors = Collections.emptySet();

                Scope parentScope = collectorContext.enterDynamicScope();
                try {
                    // Reset state in case the previous validator did not match
                    state.setMatchedNode(true);

                    if (!state.isWalkEnabled()) {
                        schemaErrors = schema.validate(executionContext, node, rootNode, instanceLocation);
                    } else {
                        schemaErrors = schema.walk(executionContext, node, rootNode, instanceLocation, state.isValidationEnabled());
                    }

                    // check if any validation errors have occurred
                    if (schemaErrors.isEmpty()) {
                        // check whether there are no errors HOWEVER we have validated the exact validator
                        if (!state.hasMatchedNode())
                            continue;

                        numberOfValidSchema++;
                    }

                    if (numberOfValidSchema > 1) {
                        // short-circuit
                        break;
                    }

                    childErrors.addAll(schemaErrors);
                } finally {
                    Scope scope = collectorContext.exitDynamicScope();
                    if (schemaErrors.isEmpty()) {
                        parentScope.mergeWith(scope);
                    }
                }
            }

            // ensure there is always an "OneOf" error reported if number of valid schemas is not equal to 1.
            if (numberOfValidSchema != 1) {
                ValidationMessage message = message().instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(Integer.toString(numberOfValidSchema)).build();
                if (this.failFast) {
                    throw new JsonSchemaException(message);
                }
                errors.add(message);
                errors.addAll(childErrors);
                collectorContext.getEvaluatedItems().clear();
                collectorContext.getEvaluatedProperties().clear();
            }

            // Make sure to signal parent handlers we matched
            if (errors.isEmpty())
                state.setMatchedNode(true);

            // reset the ValidatorState object
            resetValidatorState(collectorContext);

            return Collections.unmodifiableSet(errors);
        } finally {
            Scope scope = collectorContext.exitDynamicScope();
            if (errors.isEmpty()) {
                grandParentScope.mergeWith(scope);
            }
        }
    }

    private static void resetValidatorState(CollectorContext collectorContext) {
        ValidatorState state = (ValidatorState) collectorContext.get(ValidatorState.VALIDATOR_STATE_KEY);
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
    }
}
