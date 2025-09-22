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

package com.networknt.schema.keyword;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.DiscriminatorContext;
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.JsonType;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.TypeFactory;
import com.networknt.schema.ValidationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * {@link KeywordValidator} for anyOf.
 */
public class AnyOfValidator extends BaseKeywordValidator {
    private static final Logger logger = LoggerFactory.getLogger(AnyOfValidator.class);
    private static final String DISCRIMINATOR_REMARK = "and the discriminator-selected candidate schema didn't pass validation";

    private final List<Schema> schemas;

    private Boolean canShortCircuit = null;

    public AnyOfValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, ValidationContext validationContext) {
        super(ValidatorTypeCode.ANY_OF, schemaNode, schemaLocation, parentSchema, validationContext, evaluationPath);
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
            this.schemas.add(validationContext.newSchema(schemaLocation.append(i), evaluationPath.append(i),
                    schemaNode.get(i), parentSchema));
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

        if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()) {
            executionContext.enterDiscriminatorContext(new DiscriminatorContext(), instanceLocation);
        }
        int numberOfValidSubSchemas = 0;
        List<Error> existingErrors = executionContext.getErrors();
        List<Error> allErrors = null;
        List<Error> errors = new ArrayList<>();
        executionContext.setErrors(errors);
        try {
            // Save flag as nested schema evaluation shouldn't trigger fail fast
            boolean failFast = executionContext.isFailFast();
            try {
                executionContext.setFailFast(false);
                for (Schema schema : this.schemas) {
                    errors.clear();
                    TypeValidator typeValidator = schema.getTypeValidator();
                    if (typeValidator != null) {
                        // If schema has type validator and node type doesn't match with schemaType then
                        // ignore it
                        // For union type, it is a must to call TypeValidator
                        if (typeValidator.getSchemaType() != JsonType.UNION && !typeValidator.equalsToSchemaType(node)) {
                            typeValidator.validate(executionContext, node, rootNode, instanceLocation);
                            if (allErrors == null) {
                                allErrors = new ArrayList<>();
                            }
                            allErrors.addAll(errors);
                            continue;
                        }
                    }
                    if (!walk) {
                        schema.validate(executionContext, node, rootNode, instanceLocation);
                    } else {
                        schema.walk(executionContext, node, rootNode, instanceLocation, true);
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
                        executionContext.setErrors(existingErrors);
                        return;
                    } else if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()) {
                        DiscriminatorContext currentDiscriminatorContext = executionContext.getCurrentDiscriminatorContext();
                        if (currentDiscriminatorContext.isDiscriminatorMatchFound()
                                || currentDiscriminatorContext.isDiscriminatorIgnore()) {
                            if (!errors.isEmpty()) {
                                // The following is to match the previous logic adding to all errors
                                // which is generally discarded as it returns errors but the allErrors
                                // is getting processed in finally
                                if (allErrors == null) {
                                    allErrors = new ArrayList<>();
                                }
                                allErrors.add(error().instanceNode(node).instanceLocation(instanceLocation)
                                        .locale(executionContext.getExecutionConfig().getLocale())
                                        .arguments(DISCRIMINATOR_REMARK)
                                        .build());
                            } else {
                                // Clear all errors. Note that this is checked in finally.
                                allErrors = null;
                            }
                            existingErrors.addAll(errors);
                            executionContext.setErrors(existingErrors);
                            return;
                        }
                    }
                    if (allErrors == null) {
                        allErrors = new ArrayList<>();
                    }
                    allErrors.addAll(errors);
                }
            } finally {
                // Restore flag
                executionContext.setFailFast(failFast);
            }

            if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()
                    && executionContext.getCurrentDiscriminatorContext().isActive()
                    && !executionContext.getCurrentDiscriminatorContext().isDiscriminatorIgnore()) {
                existingErrors.add(error().instanceNode(node).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(
                                "based on the provided discriminator. No alternative could be chosen based on the discriminator property")
                        .build());
                executionContext.setErrors(existingErrors);
                return;
            }
        } finally {
            if (this.validationContext.getConfig().isDiscriminatorKeywordEnabled()) {
                executionContext.leaveDiscriminatorContextImmediately(instanceLocation);
            }
        }
        if (numberOfValidSubSchemas >= 1) {
            executionContext.setErrors(existingErrors);
        } else {
            if (allErrors != null) {
                existingErrors.addAll(allErrors);
            }
            executionContext.setErrors(existingErrors);
        }
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        if (shouldValidateSchema && node != null) {
            validate(executionContext, node, rootNode, instanceLocation, true);
            return;
        }
        for (Schema schema : this.schemas) {
            schema.walk(executionContext, node, rootNode, instanceLocation, false);
        }
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
    public void preloadJsonSchema() {
        preloadJsonSchemas(this.schemas);
        canShortCircuit(); // cache flag
    }
}