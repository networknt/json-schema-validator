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
import com.networknt.schema.utils.SetView;

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
        Set<ValidationMessage> errors = null;

        debug(logger, node, rootNode, instanceLocation);

        ValidatorState state = executionContext.getValidatorState();

        // this is a complex validator, we set the flag to true
        state.setComplexValidator(true);

        int numberOfValidSchema = 0;
        int index = 0;
        SetView<ValidationMessage> childErrors = null;
        List<String> indexes = null;

        // Save flag as nested schema evaluation shouldn't trigger fail fast
        boolean failFast = executionContext.isFailFast();
        try {
            DiscriminatorValidator discriminator = null;
            if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
                DiscriminatorContext discriminatorContext = new DiscriminatorContext();
                executionContext.enterDiscriminatorContext(discriminatorContext, instanceLocation);
                
                // check if discriminator present
                discriminator = (DiscriminatorValidator) this.getParentSchema().getValidators().stream()
                        .filter(v -> "discriminator".equals(v.getKeyword())).findFirst().orElse(null);
            }
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
                    if (indexes == null) {
                        indexes = new ArrayList<>();
                    }
                    indexes.add(Integer.toString(index));
                }
                
                if (numberOfValidSchema > 1 && canShortCircuit()) {
                    // short-circuit
                    // note that the short circuit means that only 2 valid schemas are reported even if could be more
                    break;
                }
                
                if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
                    // The discriminator will cause all messages other than the one with the
                    // matching discriminator to be discarded. Note that the discriminator cannot
                    // affect the actual validation result.
                    if (discriminator != null && !discriminator.getPropertyName().isEmpty()) {
                        JsonNode discriminatorPropertyNode = node.get(discriminator.getPropertyName());
                        if (discriminatorPropertyNode != null) {
                            String discriminatorPropertyValue = discriminatorPropertyNode.asText();
                            discriminatorPropertyValue = discriminator.getMapping().getOrDefault(discriminatorPropertyValue,
                                    discriminatorPropertyValue);
                            JsonNode refNode = schema.getSchemaNode().get("$ref");
                            if (refNode != null) {
                                String ref = refNode.asText();
                                if (ref.equals(discriminatorPropertyValue) || ref.endsWith("/" + discriminatorPropertyValue)) {
                                    executionContext.getCurrentDiscriminatorContext().markMatch();
                                }
                            }
                        } else {
                            // See issue 436 where the condition was relaxed to not cause an assertion
                            // due to missing discriminator property value
                            // Also see BaseJsonValidator#checkDiscriminatorMatch 
                            executionContext.getCurrentDiscriminatorContext().markIgnore();
                        }
                    }
                    DiscriminatorContext currentDiscriminatorContext = executionContext.getCurrentDiscriminatorContext();
                    if (currentDiscriminatorContext.isDiscriminatorMatchFound() && childErrors == null) {
                        // Note that the match is set if found and not reset so checking if childErrors
                        // found is null triggers on the correct schema
                        childErrors = new SetView<>();
                        childErrors.union(schemaErrors);
                    } else if (currentDiscriminatorContext.isDiscriminatorIgnore()) {
                        // This is the normal handling when discriminators aren't enabled
                        if (childErrors == null) {
                            childErrors = new SetView<>();
                        }
                        childErrors.union(schemaErrors);
                    }
                } else if (!schemaErrors.isEmpty() && reportChildErrors(executionContext)) {
                    // This is the normal handling when discriminators aren't enabled
                    if (childErrors == null) {
                        childErrors = new SetView<>();
                    }
                    childErrors.union(schemaErrors);
                }
                index++;
            }

            if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators()
                    && (discriminator != null || executionContext.getCurrentDiscriminatorContext().isActive())
                    && !executionContext.getCurrentDiscriminatorContext().isDiscriminatorMatchFound()
                    && !executionContext.getCurrentDiscriminatorContext().isDiscriminatorIgnore()) {
                errors = Collections.singleton(message().instanceNode(node).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(
                                "based on the provided discriminator. No alternative could be chosen based on the discriminator property")
                        .build());
            }
        } finally {
            // Restore flag
            executionContext.setFailFast(failFast);

            if (this.validationContext.getConfig().isOpenAPI3StyleDiscriminators()) {
                executionContext.leaveDiscriminatorContextImmediately(instanceLocation);
            }
        }

        // ensure there is always an "OneOf" error reported if number of valid schemas
        // is not equal to 1.
        // errors will only not be null in the discriminator case where no match is found
        if (numberOfValidSchema != 1 && errors == null) {
            ValidationMessage message = message().instanceNode(node).instanceLocation(instanceLocation)
                    .messageKey(numberOfValidSchema > 1 ? "oneOf.indexes" : "oneOf")
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .failFast(executionContext.isFailFast())
                    .arguments(Integer.toString(numberOfValidSchema), numberOfValidSchema > 1 ? String.join(", ", indexes) : "").build();
            if (childErrors != null) {
                errors = new SetView<ValidationMessage>().union(Collections.singleton(message)).union(childErrors);
            } else {
                errors = Collections.singleton(message);
            }
        }

        // Make sure to signal parent handlers we matched
        if (errors == null || errors.isEmpty()) {
            state.setMatchedNode(true);
        }

        // reset the ValidatorState object
        resetValidatorState(executionContext);

        return errors != null ? errors : Collections.emptySet();
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
