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

    private static final ArrayList<String> KEYWORDS = new ArrayList<>(Arrays.asList("if", "then", "else"));

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
                foundIfSchema = validationContext.newSchema(schemaPathOfSchema, node, parentSchema);
            } else if (keyword.equals("then") && node != null) {
                foundThenSchema = validationContext.newSchema(schemaPathOfSchema, node, parentSchema);
            } else if (keyword.equals("else") && node != null) {
                foundElseSchema = validationContext.newSchema(schemaPathOfSchema, node, parentSchema);
            }
        }

        this.ifSchema = foundIfSchema;
        this.thenSchema = foundThenSchema;
        this.elseSchema = foundElseSchema;
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);
        CollectorContext collectorContext = CollectorContext.getInstance();

        // As if-then-else might contain multiple schemas take a backup of evaluated stuff.
        Collection<String> backupEvaluatedItems = collectorContext.getEvaluatedItems();
        Collection<String> backupEvaluatedProperties = collectorContext.getEvaluatedProperties();

        Collection<String> ifEvaluatedItems = Collections.emptyList();
        Collection<String> ifEvaluatedProperties = Collections.emptyList();

        Collection<String> thenEvaluatedItems = Collections.emptyList();
        Collection<String> thenEvaluatedProperties = Collections.emptyList();

        Collection<String> elseEvaluatedItems = Collections.emptyList();
        Collection<String> elseEvaluatedProperties = Collections.emptyList();

        // Make the evaluated lists empty.
        collectorContext.resetEvaluatedItems();
        collectorContext.resetEvaluatedProperties();

        Set<ValidationMessage> errors = new LinkedHashSet<>();

        boolean ifConditionPassed = false;
        try {
            try {
                ifConditionPassed = this.ifSchema.validate(node, rootNode, at).isEmpty();
            } catch (JsonSchemaException ex) {
                // When failFast is enabled, validations are thrown as exceptions.
                // An exception means the condition failed
                ifConditionPassed = false;
            }
            // Evaluated stuff from if.
            ifEvaluatedItems = collectorContext.getEvaluatedItems();
            ifEvaluatedProperties = collectorContext.getEvaluatedProperties();

            if (ifConditionPassed && this.thenSchema != null) {

                // Make the evaluated lists empty.
                collectorContext.resetEvaluatedItems();
                collectorContext.resetEvaluatedProperties();

                errors.addAll(this.thenSchema.validate(node, rootNode, at));

                // Collect the then evaluated stuff.
                thenEvaluatedItems = collectorContext.getEvaluatedItems();
                thenEvaluatedProperties = collectorContext.getEvaluatedProperties();

            } else if (!ifConditionPassed && this.elseSchema != null) {

                // Make the evaluated lists empty.
                collectorContext.resetEvaluatedItems();
                collectorContext.resetEvaluatedProperties();

                errors.addAll(this.elseSchema.validate(node, rootNode, at));

                // Collect the else evaluated stuff.
                elseEvaluatedItems = collectorContext.getEvaluatedItems();
                elseEvaluatedProperties = collectorContext.getEvaluatedProperties();
            }

        } finally {
            collectorContext.setEvaluatedItems(backupEvaluatedItems);
            collectorContext.setEvaluatedProperties(backupEvaluatedProperties);
            if (errors.isEmpty()) {
                // If the "if" keyword condition is passed then only add if stuff as evaluated.
                if (ifConditionPassed) {
                    collectorContext.getEvaluatedItems().addAll(ifEvaluatedItems);
                    collectorContext.getEvaluatedProperties().addAll(ifEvaluatedProperties);
                }
                collectorContext.getEvaluatedItems().addAll(thenEvaluatedItems);
                collectorContext.getEvaluatedItems().addAll(elseEvaluatedItems);
                collectorContext.getEvaluatedProperties().addAll(thenEvaluatedProperties);
                collectorContext.getEvaluatedProperties().addAll(elseEvaluatedProperties);
            }
        }

        return Collections.unmodifiableSet(errors);
    }

    @Override
    public void preloadJsonSchema() {
        if(null != this.ifSchema) {
            this.ifSchema.initializeValidators();
        }
        if(null != this.thenSchema) {
            this.thenSchema.initializeValidators();
        }
        if(null != this.elseSchema) {
            this.elseSchema.initializeValidators();
        }
    }
}
