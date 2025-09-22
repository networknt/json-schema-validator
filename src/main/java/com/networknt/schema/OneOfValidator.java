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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * {@link KeywordValidator} for oneOf.
 */
public class OneOfValidator extends BaseKeywordValidator {
    private static final Logger logger = LoggerFactory.getLogger(OneOfValidator.class);

    private final List<JsonSchema> schemas;

    private Boolean canShortCircuit = null;

    public OneOfValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(ValidatorTypeCode.ONE_OF, schemaNode, schemaLocation, parentSchema, validationContext, evaluationPath);
        if (!schemaNode.isArray()) {
            JsonType nodeType = TypeFactory.getValueNodeType(schemaNode, this.validationContext.getConfig());
            throw new JsonSchemaException(error().instanceNode(schemaNode)
                    .instanceLocation(schemaLocation.getFragment())
                    .messageKey("type")
                    .arguments(nodeType.toString(), "array")
                    .build());
        }
        int size = schemaNode.size();
        this.schemas = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            JsonNode childNode = schemaNode.get(i);
            this.schemas.add(validationContext.newSchema( schemaLocation.append(i), evaluationPath.append(i), childNode, parentSchema));
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation) {
        validate(executionContext, node, rootNode, instanceLocation, false);
    }

    protected void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean walk) {
        debug(logger, executionContext, node, rootNode, instanceLocation);
        int numberOfValidSchema = 0;
        int index = 0;
        List<String> indexes = null;
        List<Error> existingErrors = executionContext.getErrors();
        List<Error> childErrors = null;
        List<Error> schemaErrors = new ArrayList<>();
        executionContext.setErrors(schemaErrors);
        // Save flag as nested schema evaluation shouldn't trigger fail fast
        boolean failFast = executionContext.isFailFast();
        boolean addMessages = true;
        try {
            DiscriminatorValidator discriminator = null;
            if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()) {
                DiscriminatorContext discriminatorContext = new DiscriminatorContext();
                executionContext.enterDiscriminatorContext(discriminatorContext, instanceLocation);

                // check if discriminator present
                discriminator = (DiscriminatorValidator) this.getParentSchema().getValidators().stream()
                        .filter(v -> "discriminator".equals(v.getKeyword())).findFirst().orElse(null);
                if (discriminator != null) {
                    // this is just to make the discriminator context active
                    discriminatorContext.registerDiscriminator(discriminator.getSchemaLocation(),
                            (ObjectNode) discriminator.getSchemaNode());
                }
            }
            executionContext.setFailFast(false);
            for (JsonSchema schema : this.schemas) {
                schemaErrors.clear();
                if (!walk) {
                    schema.validate(executionContext, node, rootNode, instanceLocation);
                } else {
                    schema.walk(executionContext, node, rootNode, instanceLocation, true);
                }

                // check if any validation errors have occurred
                if (schemaErrors.isEmpty()) { // No new errors
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
                
                if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()) {
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
                        childErrors = new ArrayList<>();
                        childErrors.addAll(schemaErrors);
                    } else if (currentDiscriminatorContext.isDiscriminatorIgnore()
                            || !currentDiscriminatorContext.isActive()) {
                        // This is the normal handling when discriminators aren't enabled
                        if (childErrors == null) {
                            childErrors = new ArrayList<>();
                        }
                        childErrors.addAll(schemaErrors);
                    }
                } else if (!schemaErrors.isEmpty() && reportChildErrors(executionContext)) {
                    // This is the normal handling when discriminators aren't enabled
                    if (childErrors == null) {
                        childErrors = new ArrayList<>();
                    }
                    childErrors.addAll(schemaErrors);
                }
                index++;
            }

            if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()
                    && (discriminator != null || executionContext.getCurrentDiscriminatorContext().isActive())
                    && !executionContext.getCurrentDiscriminatorContext().isDiscriminatorMatchFound()
                    && !executionContext.getCurrentDiscriminatorContext().isDiscriminatorIgnore()) {
                addMessages = false;
                existingErrors.add(error().instanceNode(node).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(
                                "based on the provided discriminator. No alternative could be chosen based on the discriminator property")
                        .build());
            }
        } finally {
            // Restore flag
            executionContext.setFailFast(failFast);

            if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()) {
                executionContext.leaveDiscriminatorContextImmediately(instanceLocation);
            }
        }

        // ensure there is always an "OneOf" error reported if number of valid schemas
        // is not equal to 1.
        // errors will only not be null in the discriminator case where no match is found
        if (numberOfValidSchema != 1 && addMessages) {
            Error message = error().instanceNode(node).instanceLocation(instanceLocation)
                    .messageKey(numberOfValidSchema > 1 ? "oneOf.indexes" : "oneOf")
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .arguments(Integer.toString(numberOfValidSchema), numberOfValidSchema > 1 ? String.join(", ", indexes) : "").build();
            existingErrors.add(message);
            if (childErrors != null) {
                existingErrors.addAll(childErrors);
            }
        }
        executionContext.setErrors(existingErrors);
        return;
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
            for (KeywordValidator validator : getEvaluationParentSchema().getValidators()) {
                if ("unevaluatedProperties".equals(validator.getKeyword())
                        || "unevaluatedItems".equals(validator.getKeyword())) {
                    canShortCircuit = false;
                }
            }
            this.canShortCircuit = canShortCircuit;
        }
        return this.canShortCircuit;
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        if (shouldValidateSchema && node != null) {
            validate(executionContext, node, rootNode, instanceLocation, true);
        } else {
            for (JsonSchema schema : this.schemas) {
                schema.walk(executionContext, node, rootNode, instanceLocation, false);
            }
        }
    }

    @Override
    public void preloadJsonSchema() {
        for (JsonSchema schema: this.schemas) {
            schema.initializeValidators();
        }
        canShortCircuit(); // cache the flag
    }
}
