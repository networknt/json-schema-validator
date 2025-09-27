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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.SchemaException;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.utils.JsonType;
import com.networknt.schema.utils.TypeFactory;

/**
 * {@link KeywordValidator} for oneOf.
 */
public class OneOfValidator extends BaseKeywordValidator {
    private final List<Schema> schemas;

    private Boolean canShortCircuit = null;

    public OneOfValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.ONE_OF, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        if (!schemaNode.isArray()) {
            JsonType nodeType = TypeFactory.getValueNodeType(schemaNode, this.schemaContext.getSchemaRegistryConfig());
            throw new SchemaException(error().instanceNode(schemaNode).instanceLocation(schemaLocation.getFragment())
                    .messageKey("type").arguments(nodeType.toString(), "array").build());
        }
        int size = schemaNode.size();
        this.schemas = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            JsonNode childNode = schemaNode.get(i);
            this.schemas.add(schemaContext.newSchema(schemaLocation.append(i), evaluationPath.append(i), childNode,
                    parentSchema));
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        validate(executionContext, node, rootNode, instanceLocation, false);
    }

    protected void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation, boolean walk) {
        int numberOfValidSchema = 0;
        int index = 0;
        List<String> indexes = null;
        List<Error> existingErrors = executionContext.getErrors();
        List<Error> allErrors = null; // Keeps track of all the errors for reporting if in the end none or more than
                                      // one sub schema matches
        List<Error> discriminatorErrors = null; // The errors from the sub schema that match the discriminator
        List<Error> subSchemaErrors = new ArrayList<>(); // Temporary errors from each sub schema execution

        executionContext.setErrors(subSchemaErrors);
        // Save flag as nested schema evaluation shouldn't trigger fail fast
        boolean failFast = executionContext.isFailFast();
        try {
            executionContext.setFailFast(false);
            for (Schema schema : this.schemas) {
                subSchemaErrors.clear();
                if (!walk) {
                    schema.validate(executionContext, node, rootNode, instanceLocation);
                } else {
                    schema.walk(executionContext, node, rootNode, instanceLocation, true);
                }

                // check if any validation errors have occurred
                if (subSchemaErrors.isEmpty()) { // No new errors
                    numberOfValidSchema++;
                    if (indexes == null) {
                        indexes = new ArrayList<>();
                    }
                    indexes.add(Integer.toString(index));
                }

                if (numberOfValidSchema > 1 && canShortCircuit()) {
                    // short-circuit
                    // note that the short circuit means that only 2 valid schemas are reported even
                    // if could be more
                    break;
                }

                if (this.schemaContext.isDiscriminatorKeywordEnabled()) {
                    boolean discriminatorMatchFound = false;
                    DiscriminatorState discriminator = executionContext.getDiscriminatorMapping().get(instanceLocation);
                    JsonNode refNode = schema.getSchemaNode().get("$ref");
                    if (discriminator != null && refNode != null) {
                        discriminatorMatchFound = discriminator.matches(refNode.asText());
                    }
                    if (discriminatorMatchFound) {
                        /*
                         * Note that discriminator cannot change the outcome of the evaluation but can
                         * be used to filter off any additional messages
                         * 
                         * The discriminator will cause all messages other than the one with the //
                         * matching discriminator to be discarded.
                         */
                        if (!subSchemaErrors.isEmpty()) {
                            /*
                             * This means that the discriminated value has errors and doesn't match so these
                             * errors are the only ones that will be reported *IF* there are no other
                             * schemas that successfully validate to meet the requirement of anyOf.
                             * 
                             * If there are any successful schemas as per anyOf, all these errors will be
                             * discarded.
                             */
                            discriminatorErrors = new ArrayList<>(subSchemaErrors);
                            allErrors = null; // This is no longer needed
                        }
                    } else {
                        // This is the normal handling when discriminators aren't enabled
                        if (discriminatorErrors == null) {
                            if (allErrors == null) {
                                allErrors = new ArrayList<>();
                            }
                            allErrors.addAll(subSchemaErrors);
                        }
                    }
                } else if (!subSchemaErrors.isEmpty() && reportChildErrors(executionContext)) {
                    // This is the normal handling when discriminators aren't enabled
                    if (allErrors == null) {
                        allErrors = new ArrayList<>();
                    }
                    allErrors.addAll(subSchemaErrors);
                }
                index++;
            }

            if (this.schemaContext.isDiscriminatorKeywordEnabled()) {
                /*
                 * The only case where the discriminator can change the outcome of the result is
                 * if the discriminator value does not match an implicit or explicit mapping
                 */
                /*
                 * If the discriminator value does not match an implicit or explicit mapping, no
                 * schema can be determined and validation SHOULD fail. Mapping keys MUST be
                 * string values, but tooling MAY convert response values to strings for
                 * comparison.
                 * 
                 * https://spec.openapis.org/oas/v3.1.2#examples-0
                 */
                DiscriminatorState state = executionContext.getDiscriminatorMapping().get(instanceLocation);
                if (state != null && !state.hasMatchedSchema() && state.hasDiscriminatingValue()) {
                    // The check for state.hasDiscriminatingValue is due to issue 988
                    // Note that this is related to the DiscriminatorValidator by default not
                    // generating an assertion
                    // if the discriminatingValue is not set in the payload
                    existingErrors
                            .add(error().keyword("discriminator").instanceNode(node).instanceLocation(instanceLocation)
                                    .locale(executionContext.getExecutionConfig().getLocale())
                                    .messageKey("discriminator.oneOf.no_match_found")
                                    .arguments(state.getDiscriminatingValue()).build());
                }
            }
        } finally {
            // Restore flag
            executionContext.setFailFast(failFast);
        }

        if (numberOfValidSchema != 1) {
            /*
             * Ensure there is always an "oneOf" error reported if number of valid schemas
             * is not equal to 1
             */
            Error message = error().instanceNode(node).instanceLocation(instanceLocation)
                    .messageKey(numberOfValidSchema > 1 ? "oneOf.indexes" : "oneOf")
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .arguments(Integer.toString(numberOfValidSchema),
                            numberOfValidSchema > 1 ? String.join(", ", indexes) : "")
                    .build();
            existingErrors.add(message);

            if (discriminatorErrors != null) {
                existingErrors.addAll(discriminatorErrors);
            } else if (allErrors != null) {
                existingErrors.addAll(allErrors);
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
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation,
            boolean shouldValidateSchema) {
        if (shouldValidateSchema && node != null) {
            validate(executionContext, node, rootNode, instanceLocation, true);
        } else {
            for (Schema schema : this.schemas) {
                schema.walk(executionContext, node, rootNode, instanceLocation, false);
            }
        }
    }

    @Override
    public void preloadSchema() {
        for (Schema schema : this.schemas) {
            schema.initializeValidators();
        }
        canShortCircuit(); // cache the flag
    }
}
