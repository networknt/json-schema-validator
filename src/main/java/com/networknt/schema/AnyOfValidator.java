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
 * {@link JsonValidator} for anyOf.
 */
public class AnyOfValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(AnyOfValidator.class);
    private static final String DISCRIMINATOR_REMARK = "and the discriminator-selected candidate schema didn't pass validation";

    private final List<JsonSchema> schemas;

    private Boolean canShortCircuit = null;

    public AnyOfValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.ANY_OF, validationContext);
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
            this.schemas.add(validationContext.newSchema(schemaLocation.append(i), evaluationPath.append(i),
                    schemaNode.get(i), parentSchema));
        }
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation) {
        return validate(executionContext, node, rootNode, instanceLocation, false);
    }

    protected Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean walk) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()) {
            executionContext.enterDiscriminatorContext(new DiscriminatorContext(), instanceLocation);
        }
        SetView<ValidationMessage> allErrors = null;

        int numberOfValidSubSchemas = 0;
        try {
            // Save flag as nested schema evaluation shouldn't trigger fail fast
            boolean failFast = executionContext.isFailFast();
            try {
                executionContext.setFailFast(false);
                for (JsonSchema schema : this.schemas) {
                    Set<ValidationMessage> errors = Collections.emptySet();
                    TypeValidator typeValidator = schema.getTypeValidator();
                    if (typeValidator != null) {
                        // If schema has type validator and node type doesn't match with schemaType then
                        // ignore it
                        // For union type, it is a must to call TypeValidator
                        if (typeValidator.getSchemaType() != JsonType.UNION && !typeValidator.equalsToSchemaType(node)) {
                            if (allErrors == null) {
                                allErrors = new SetView<>();
                            }
                            allErrors.union(typeValidator.validate(executionContext, node, rootNode, instanceLocation));
                            continue;
                        }
                    }
                    if (!walk) {
                        errors = schema.validate(executionContext, node, rootNode, instanceLocation);
                    } else {
                        errors = schema.walk(executionContext, node, rootNode, instanceLocation, true);
                    }

                    // check if any validation errors have occurred
                    if (errors.isEmpty()) {
                        // we found a valid subschema, so increase counter
                        numberOfValidSubSchemas++;
                    }

                    if (errors.isEmpty() && (!this.validationContext.getConfig().isDiscriminatorKeywordEnabled())
                            && canShortCircuit() && canShortCircuit(executionContext)) {
                        // Clear all errors. Note that this is checked in finally.
                        allErrors = null;
                        // return empty errors.
                        return errors;
                    } else if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()) {
                        DiscriminatorContext currentDiscriminatorContext = executionContext.getCurrentDiscriminatorContext();
                        if (currentDiscriminatorContext.isDiscriminatorMatchFound()
                                || currentDiscriminatorContext.isDiscriminatorIgnore()) {
                            if (!errors.isEmpty()) {
                                // The following is to match the previous logic adding to all errors
                                // which is generally discarded as it returns errors but the allErrors
                                // is getting processed in finally
                                if (allErrors == null) {
                                    allErrors = new SetView<>();
                                }
                                allErrors.union(Collections
                                        .singleton(message().instanceNode(node).instanceLocation(instanceLocation)
                                                .locale(executionContext.getExecutionConfig().getLocale())
                                                .failFast(executionContext.isFailFast()).arguments(DISCRIMINATOR_REMARK)
                                                .build()));
                            } else {
                                // Clear all errors. Note that this is checked in finally.
                                allErrors = null;
                            }
                            return errors;
                        }
                    }
                    if (allErrors == null) {
                        allErrors = new SetView<>();
                    }
                    allErrors.union(errors);
                }
            } finally {
                // Restore flag
                executionContext.setFailFast(failFast);
            }

            if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()
                    && executionContext.getCurrentDiscriminatorContext().isActive()
                    && !executionContext.getCurrentDiscriminatorContext().isDiscriminatorIgnore()) {
                return Collections.singleton(message().instanceNode(node).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(
                                "based on the provided discriminator. No alternative could be chosen based on the discriminator property")
                        .build());
            }
        } finally {
            if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()) {
                executionContext.leaveDiscriminatorContextImmediately(instanceLocation);
            }
        }
        if (numberOfValidSubSchemas >= 1) {
            return Collections.emptySet();
        }
        return allErrors != null ? allErrors : Collections.emptySet();
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            return validate(executionContext, node, rootNode, instanceLocation, true);
        }
        for (JsonSchema schema : this.schemas) {
            schema.walk(executionContext, node, rootNode, instanceLocation, false);
        }
        return new LinkedHashSet<>();
    }

    /**
     * If annotation collection is enabled cannot short circuit.
     * 
     * @see <a href=
     *      "https://github.com/json-schema-org/json-schema-spec/blob/f8967bcbc6cee27753046f63024b55336a9b1b54/jsonschema-core.md?plain=1#L1717-L1720">anyOf</a>
     * @param executionContext the execution context
     * @return true if can short circuit
     */
    protected boolean canShortCircuit(ExecutionContext executionContext) {
        return !executionContext.getExecutionConfig().isAnnotationCollectionEnabled();
    }

    /**
     * If annotations are require for evaluation cannot short circuit.
     * 
     * @return true if can short circuit
     */
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
    public void preloadJsonSchema() {
        preloadJsonSchemas(this.schemas);
        canShortCircuit(); // cache flag
    }
}