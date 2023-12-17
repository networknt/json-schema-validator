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

public class IfValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(IfValidator.class);

    private static final ArrayList<String> KEYWORDS = new ArrayList<>(Arrays.asList("if", "then", "else"));

    private final JsonSchema ifSchema;
    private final JsonSchema thenSchema;
    private final JsonSchema elseSchema;

    public IfValidator(JsonNodePath schemaPath, JsonNodePath validationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, validationPath, schemaNode, parentSchema, ValidatorTypeCode.IF_THEN_ELSE, validationContext);

        JsonSchema foundIfSchema = null;
        JsonSchema foundThenSchema = null;
        JsonSchema foundElseSchema = null;

        for (final String keyword : KEYWORDS) {
            final JsonNode node = schemaNode.get(keyword);
            final JsonNodePath schemaPathOfSchema = parentSchema.schemaPath.resolve(keyword);
            if (keyword.equals("if")) {
                foundIfSchema = validationContext.newSchema(schemaPathOfSchema, null, node, parentSchema);
            } else if (keyword.equals("then") && node != null) {
                foundThenSchema = validationContext.newSchema(schemaPathOfSchema, null, node, parentSchema);
            } else if (keyword.equals("else") && node != null) {
                foundElseSchema = validationContext.newSchema(schemaPathOfSchema, null, node, parentSchema);
            }
        }

        this.ifSchema = foundIfSchema;
        this.thenSchema = foundThenSchema;
        this.elseSchema = foundElseSchema;
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at) {
        debug(logger, node, rootNode, at);
        CollectorContext collectorContext = executionContext.getCollectorContext();

        Set<ValidationMessage> errors = new LinkedHashSet<>();

        Scope parentScope = collectorContext.enterDynamicScope();
        boolean ifConditionPassed = false;
        try {
            try {
                ifConditionPassed = this.ifSchema.validate(executionContext, node, rootNode, at).isEmpty();
            } catch (JsonSchemaException ex) {
                // When failFast is enabled, validations are thrown as exceptions.
                // An exception means the condition failed
                ifConditionPassed = false;
            }

            if (ifConditionPassed && this.thenSchema != null) {
                errors.addAll(this.thenSchema.validate(executionContext, node, rootNode, at));
            } else if (!ifConditionPassed && this.elseSchema != null) {
                // discard ifCondition results
                collectorContext.exitDynamicScope();
                collectorContext.enterDynamicScope();

                errors.addAll(this.elseSchema.validate(executionContext, node, rootNode, at));
            }

        } finally {
            Scope scope = collectorContext.exitDynamicScope();
            if (errors.isEmpty()) {
                parentScope.mergeWith(scope);
            }
        }

        return Collections.unmodifiableSet(errors);
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
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at, boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            return validate(executionContext, node, rootNode, at);
        }

        if (null != this.ifSchema) {
            this.ifSchema.walk(executionContext, node, rootNode, at, false);
        }
        if (null != this.thenSchema) {
            this.thenSchema.walk(executionContext, node, rootNode, at, false);
        }
        if (null != this.elseSchema) {
            this.elseSchema.walk(executionContext, node, rootNode, at, false);
        }

        return Collections.emptySet();
    }

}
