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
 * {@link KeywordValidator} for anyOf.
 */
public class AnyOfValidator extends BaseKeywordValidator {
    private final List<Schema> schemas;

    private Boolean canShortCircuit = null;

    public AnyOfValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.ANY_OF, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        if (!schemaNode.isArray()) {
            JsonType nodeType = TypeFactory.getValueNodeType(schemaNode, this.schemaContext.getSchemaRegistryConfig());
            throw new SchemaException(error().instanceNode(schemaNode).instanceLocation(schemaLocation.getFragment())
                    .messageKey("type").arguments(nodeType.toString(), "array").build());
        }
        int size = schemaNode.size();
        this.schemas = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.schemas.add(schemaContext.newSchema(schemaLocation.append(i), evaluationPath.append(i),
                    schemaNode.get(i), parentSchema));
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        validate(executionContext, node, rootNode, instanceLocation, false);
    }

    protected void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation, boolean walk) {
        int numberOfValidSubSchemas = 0;
        List<Error> existingErrors = executionContext.getErrors();
        List<Error> allErrors = null; // Keeps track of all the errors for reporting if in the end none of the schemas
                                      // match
        List<Error> discriminatorErrors = null; // The errors from the sub schema that match the discriminator
        List<Error> subSchemaErrors = new ArrayList<>(); // Temporary errors from each sub schema execution
        executionContext.setErrors(subSchemaErrors);

        // Save flag as nested schema evaluation shouldn't trigger fail fast
        boolean failFast = executionContext.isFailFast();
        try {
            executionContext.setFailFast(false);
            for (Schema schema : this.schemas) {
                subSchemaErrors.clear(); // Reuse and clear for each run
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
                        allErrors.addAll(subSchemaErrors);
                        continue;
                    }
                }
                if (!walk) {
                    schema.validate(executionContext, node, rootNode, instanceLocation);
                } else {
                    schema.walk(executionContext, node, rootNode, instanceLocation, true);
                }

                // check if any validation errors have occurred
                if (subSchemaErrors.isEmpty()) {
                    // we found a valid subschema, so increase counter
                    numberOfValidSubSchemas++;
                }

                if (subSchemaErrors.isEmpty() && (!this.schemaContext.isDiscriminatorKeywordEnabled())
                        && canShortCircuit() && canShortCircuit(executionContext)) {
                    // Successful so return only the existing errors, ie. no new errors
                    executionContext.setErrors(existingErrors);
                    return;
                } else if (this.schemaContext.isDiscriminatorKeywordEnabled()) {
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
                    }
                }
                /*
                 * This adds all the errors for this schema to the list that contains all the
                 * errors for later reporting.
                 * 
                 * There's no need to add these if there was a discriminator match with errors
                 * as only the discriminator errors will be reported if all the schemas fail.
                 */
                if (!subSchemaErrors.isEmpty() && discriminatorErrors == null) {
                    if (allErrors == null) {
                        allErrors = new ArrayList<>();
                    }
                    allErrors.addAll(subSchemaErrors);
                }
            }
        } finally {
            // Restore flag
            executionContext.setFailFast(failFast);
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
                existingErrors.add(error().keyword("discriminator").instanceNode(node)
                        .instanceLocation(instanceLocation).locale(executionContext.getExecutionConfig().getLocale())
                        .messageKey("discriminator.anyOf.no_match_found").arguments(state.getDiscriminatingValue())
                        .build());
            }
        }

        if (numberOfValidSubSchemas >= 1) {
            // Successful so return only the existing errors, ie. no new errors
            executionContext.setErrors(existingErrors);
        } else {
            if (discriminatorErrors != null) {
                // If errors are present matching the discriminator, only these errors should be
                // reported
                existingErrors.addAll(discriminatorErrors);
            } else if (allErrors != null) {
                // As the anyOf has failed, report all the errors
                existingErrors.addAll(allErrors);
            }
            executionContext.setErrors(existingErrors);
        }
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation,
            boolean shouldValidateSchema) {
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
    public void preloadSchema() {
        preloadSchemas(this.schemas);
        canShortCircuit(); // cache flag
    }
}