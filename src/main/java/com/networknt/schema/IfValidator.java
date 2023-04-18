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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class IfValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(IfValidator.class);

    private static final ArrayList<String> KEYWORDS = new ArrayList<String>(Arrays.asList("if", "then", "else"));

    private final JsonSchema ifSchema;
    private final JsonSchema thenSchema;
    private final JsonSchema elseSchema;

    public IfValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.IF_THEN_ELSE, validationContext);

        JsonSchema foundIfSchema = null;
        JsonSchema foundThenSchema = null;
        JsonSchema foundElseSchema = null;

        for (final String keyword : KEYWORDS) {
            final JsonNode node = schemaNode.get(keyword);
            final String schemaPathOfSchema = parentSchema.schemaPath + "/" + keyword;
            if (keyword.equals("if")) {
                foundIfSchema = new JsonSchema(validationContext, schemaPathOfSchema, parentSchema.getCurrentUri(), node, parentSchema);
            } else if (keyword.equals("then") && node != null) {
                foundThenSchema = new JsonSchema(validationContext, schemaPathOfSchema, parentSchema.getCurrentUri(), node, parentSchema);
            } else if (keyword.equals("else") && node != null) {
                foundElseSchema = new JsonSchema(validationContext, schemaPathOfSchema, parentSchema.getCurrentUri(), node, parentSchema);
            }
        }

        ifSchema = foundIfSchema;
        thenSchema = foundThenSchema;
        elseSchema = foundElseSchema;
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        // As if-then-else might contain multiple schemas take a backup of evaluatedProperties.
        Set<String> backupEvaluatedProperties = CollectorContext.getInstance().copyEvaluatedProperties();

        Set<String> ifEvaluatedProperties = Collections.emptySet();

        Set<String> thenEvaluatedProperties = Collections.emptySet();

        Set<String> elseEvaluatedProperties = Collections.emptySet();

        // Make the evaluatedProperties list empty.
        CollectorContext.getInstance().getEvaluatedProperties().clear();

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();

        boolean ifConditionPassed = false;
        try {
            try {
                ifConditionPassed = ifSchema.validate(node, rootNode, at).isEmpty();
            } catch (JsonSchemaException ex) {
                // When failFast is enabled, validations are thrown as exceptions.
                // An exception means the condition failed
                ifConditionPassed = false;
            }
            // Evaluated Properties from if.
            ifEvaluatedProperties = CollectorContext.getInstance().copyEvaluatedProperties();

            if (ifConditionPassed && thenSchema != null) {

                // Make the evaluatedProperties list empty.
                CollectorContext.getInstance().getEvaluatedProperties().clear();

                errors.addAll(thenSchema.validate(node, rootNode, at));

                // Collect the then evaluated properties.
                thenEvaluatedProperties = CollectorContext.getInstance().copyEvaluatedProperties();

            } else if (!ifConditionPassed && elseSchema != null) {

                // Make the evaluatedProperties list empty.
                CollectorContext.getInstance().getEvaluatedProperties().clear();

                errors.addAll(elseSchema.validate(node, rootNode, at));

                // Collect the else evaluated properties.
                elseEvaluatedProperties = CollectorContext.getInstance().copyEvaluatedProperties();
            }

        } finally {
            CollectorContext.getInstance().replaceEvaluatedProperties(backupEvaluatedProperties);
            if (errors.isEmpty()) {
                // If the "if" keyword condition is passed then only add if properties as evaluated.
                if (ifConditionPassed) {
                    CollectorContext.getInstance().getEvaluatedProperties().addAll(ifEvaluatedProperties);
                }
                CollectorContext.getInstance().getEvaluatedProperties().addAll(thenEvaluatedProperties);
                CollectorContext.getInstance().getEvaluatedProperties().addAll(elseEvaluatedProperties);
            }
        }

        return Collections.unmodifiableSet(errors);
    }

    @Override
    public void preloadJsonSchema() {
        if(null != ifSchema) {
            ifSchema.initializeValidators();
        }
        if(null != thenSchema) {
            thenSchema.initializeValidators();
        }
        if(null != elseSchema) {
            elseSchema.initializeValidators();
        }
    }
}
