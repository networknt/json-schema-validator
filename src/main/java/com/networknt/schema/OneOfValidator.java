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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.utils.SetView;

/**
 * {@link JsonValidator} for oneOf.
 */
public class OneOfValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(OneOfValidator.class);

    private final List<JsonSchema> schemas;

    private Boolean canShortCircuit = null;

    public OneOfValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.ONE_OF, validationContext);
        if (!schemaNode.isArray()) {
            JsonType nodeType = TypeFactory.getValueNodeType(schemaNode, this.validationContext.getConfig());
            throw new JsonSchemaException(message().instanceNode(schemaNode)
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
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation) {
        return validate(executionContext, node, rootNode, instanceLocation, false);
    }

    protected Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean walk) {
        Set<ValidationMessage> errors = null;

        debug(logger, executionContext, node, rootNode, instanceLocation);
        int numberOfValidSchema = 0;
        int index = 0;
        SetView<ValidationMessage> childErrors = null;
        List<String> indexes = null;

        // Save flag as nested schema evaluation shouldn't trigger fail fast
        boolean failFast = executionContext.isFailFast();
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
                Set<ValidationMessage> schemaErrors = Collections.emptySet();
                if (!walk) {
                    schemaErrors = schema.validate(executionContext, node, rootNode, instanceLocation);
                } else {
                    schemaErrors = schema.walk(executionContext, node, rootNode, instanceLocation,
                            true);
                }

                // check if any validation errors have occurred
                if (schemaErrors.isEmpty()) {
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
                        childErrors = new SetView<>();
                        childErrors.union(schemaErrors);
                    } else if (currentDiscriminatorContext.isDiscriminatorIgnore()
                            || !currentDiscriminatorContext.isActive()) {
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

            if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()
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

            if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()) {
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

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        HashSet<ValidationMessage> validationMessages = new LinkedHashSet<>();
        if (shouldValidateSchema) {
            validationMessages.addAll(validate(executionContext, node, rootNode, instanceLocation, true));
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
