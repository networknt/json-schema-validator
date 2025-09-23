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
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.NodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;

import java.util.*;

/**
 * {@link KeywordValidator} for if.
 */
public class IfValidator extends BaseKeywordValidator {
    private static final List<String> KEYWORDS = Arrays.asList("if", "then", "else");

    private final Schema ifSchema;
    private final Schema thenSchema;
    private final Schema elseSchema;

    public IfValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(ValidatorTypeCode.IF_THEN_ELSE, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);

        Schema foundIfSchema = null;
        Schema foundThenSchema = null;
        Schema foundElseSchema = null;

        for (final String keyword : KEYWORDS) {
            final JsonNode node = parentSchema.getSchemaNode().get(keyword);
            final SchemaLocation schemaLocationOfSchema = parentSchema.getSchemaLocation().append(keyword);
            final NodePath evaluationPathOfSchema = parentSchema.getEvaluationPath().append(keyword);
            if (keyword.equals("if")) {
                foundIfSchema = schemaContext.newSchema(schemaLocationOfSchema, evaluationPathOfSchema, node,
                        parentSchema);
            } else if (keyword.equals("then") && node != null) {
                foundThenSchema = schemaContext.newSchema(schemaLocationOfSchema, evaluationPathOfSchema, node,
                        parentSchema);
            } else if (keyword.equals("else") && node != null) {
                foundElseSchema = schemaContext.newSchema(schemaLocationOfSchema, evaluationPathOfSchema, node,
                        parentSchema);
            }
        }

        this.ifSchema = foundIfSchema;
        this.thenSchema = foundThenSchema;
        this.elseSchema = foundElseSchema;
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        

        boolean ifConditionPassed = false;

        // Save flag as nested schema evaluation shouldn't trigger fail fast
        boolean failFast = executionContext.isFailFast();
        List<Error> existingErrors = executionContext.getErrors();
        List<Error> test = new ArrayList<>();
        executionContext.setErrors(test);
        try {
            executionContext.setFailFast(false);
            this.ifSchema.validate(executionContext, node, rootNode, instanceLocation);
            ifConditionPassed = test.isEmpty();
        } finally {
            // Restore flag
            executionContext.setErrors(existingErrors);
            executionContext.setFailFast(failFast);
        }

        if (ifConditionPassed && this.thenSchema != null) {
            this.thenSchema.validate(executionContext, node, rootNode, instanceLocation);
        } else if (!ifConditionPassed && this.elseSchema != null) {
            this.elseSchema.validate(executionContext, node, rootNode, instanceLocation);
        }
    }

    @Override
    public void preloadJsonSchema() {
        if (null != this.ifSchema) {
            this.ifSchema.initializeValidators();
        }
        if (null != this.thenSchema) {
            this.thenSchema.initializeValidators();
        }
        if (null != this.elseSchema) {
            this.elseSchema.initializeValidators();
        }
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation, boolean shouldValidateSchema) {
        boolean checkCondition = node != null && shouldValidateSchema;
        boolean ifConditionPassed = false;

        // Save flag as nested schema evaluation shouldn't trigger fail fast
        boolean failFast = executionContext.isFailFast();
        List<Error> existingErrors = executionContext.getErrors();
        List<Error> test = new ArrayList<>();
        executionContext.setErrors(test);
        try {
            executionContext.setFailFast(false);
            this.ifSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
            ifConditionPassed = test.isEmpty();
        } finally {
            // Restore flag
            executionContext.setErrors(existingErrors);
            executionContext.setFailFast(failFast);
        }
        if (!checkCondition) {
            if (this.thenSchema != null) {
                this.thenSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
            }
            if (this.elseSchema != null) {
                this.elseSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
            }
        } else {
            if (this.thenSchema != null && ifConditionPassed) {
                this.thenSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
            }
            else if (this.elseSchema != null && !ifConditionPassed) {
                this.elseSchema.walk(executionContext, node, rootNode, instanceLocation, shouldValidateSchema);
            }
        }
    }

}
