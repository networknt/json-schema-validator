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
import com.networknt.schema.regex.RegularExpression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AdditionalPropertiesValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(AdditionalPropertiesValidator.class);

    private final boolean allowAdditionalProperties;
    private final JsonSchema additionalPropertiesSchema;
    private final Set<String> allowedProperties;
    private final List<RegularExpression> patternProperties = new ArrayList<>();

    public AdditionalPropertiesValidator(JsonNodePath schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                                         ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ADDITIONAL_PROPERTIES, validationContext);
        if (schemaNode.isBoolean()) {
            allowAdditionalProperties = schemaNode.booleanValue();
            additionalPropertiesSchema = null;
        } else if (schemaNode.isObject()) {
            allowAdditionalProperties = true;
            additionalPropertiesSchema = validationContext.newSchema(schemaPath, schemaNode, parentSchema);
        } else {
            allowAdditionalProperties = false;
            additionalPropertiesSchema = null;
        }

        JsonNode propertiesNode = parentSchema.getSchemaNode().get(PropertiesValidator.PROPERTY);
        if (propertiesNode != null) {
            allowedProperties = new HashSet<>();
            for (Iterator<String> it = propertiesNode.fieldNames(); it.hasNext(); ) {
                allowedProperties.add(it.next());
            }
        } else {
            allowedProperties = Collections.emptySet();
        }

        JsonNode patternPropertiesNode = parentSchema.getSchemaNode().get(PatternPropertiesValidator.PROPERTY);
        if (patternPropertiesNode != null) {
            for (Iterator<String> it = patternPropertiesNode.fieldNames(); it.hasNext(); ) {
                patternProperties.add(RegularExpression.compile(it.next(), validationContext));
            }
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at) {
        debug(logger, node, rootNode, at);
        if (!node.isObject()) {
            // ignore no object
            return Collections.emptySet();
        }

        CollectorContext collectorContext = executionContext.getCollectorContext();
        // if allowAdditionalProperties is true, add all the properties as evaluated.
        if (allowAdditionalProperties) {
            for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
                collectorContext.getEvaluatedProperties().add(atPath(at, it.next()));
            }
        }

        Set<ValidationMessage> errors = null;

        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            // skip the context items
            if (pname.startsWith("#")) {
                continue;
            }
            boolean handledByPatternProperties = false;
            for (RegularExpression pattern : patternProperties) {
                if (pattern.matches(pname)) {
                    handledByPatternProperties = true;
                    break;
                }
            }

            if (!allowedProperties.contains(pname) && !handledByPatternProperties) {
                if (!allowAdditionalProperties) {
                    if (errors == null) {
                        errors = new LinkedHashSet<>();
                    }
                    errors.add(buildValidationMessage(pname, at.resolve(pname),
                            executionContext.getExecutionConfig().getLocale(), pname));
                } else {
                    if (additionalPropertiesSchema != null) {
                        ValidatorState state = (ValidatorState) collectorContext.get(ValidatorState.VALIDATOR_STATE_KEY);
                        if (state != null && state.isWalkEnabled()) {
                            Set<ValidationMessage> results = additionalPropertiesSchema.walk(executionContext, node.get(pname), rootNode, atPath(at, pname), state.isValidationEnabled());
                            if (!results.isEmpty()) {
                                if (errors == null) {
                                    errors = new LinkedHashSet<>();
                                }
                                errors.addAll(results);
                            }
                        } else {
                            Set<ValidationMessage> results = additionalPropertiesSchema.validate(executionContext, node.get(pname), rootNode, atPath(at, pname));
                            if (!results.isEmpty()) {
                                if (errors == null) {
                                    errors = new LinkedHashSet<>();
                                }
                                errors.addAll(results);
                            }
                        }
                    }
                }
            }
        }
        return errors == null ? Collections.emptySet() : Collections.unmodifiableSet(errors);
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at, boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            return validate(executionContext, node, rootNode, at);
        }

        if (node == null || !node.isObject()) {
            // ignore no object
            return Collections.emptySet();
        }

        // Else continue walking.
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            // skip the context items
            if (pname.startsWith("#")) {
                continue;
            }
            boolean handledByPatternProperties = false;
            for (RegularExpression pattern : patternProperties) {
                if (pattern.matches(pname)) {
                    handledByPatternProperties = true;
                    break;
                }
            }

            if (!allowedProperties.contains(pname) && !handledByPatternProperties) {
                if (allowAdditionalProperties) {
                    if (additionalPropertiesSchema != null) {
                        ValidatorState state = (ValidatorState) executionContext.getCollectorContext().get(ValidatorState.VALIDATOR_STATE_KEY);
                        if (state != null && state.isWalkEnabled()) {
                           additionalPropertiesSchema.walk(executionContext, node.get(pname), rootNode, atPath(at, pname), state.isValidationEnabled());
                        }
                    }
                }
            }
        }
        return Collections.emptySet();
    }

    @Override
    public void preloadJsonSchema() {
        if(additionalPropertiesSchema!=null) {
            additionalPropertiesSchema.initializeValidators();
        }
    }
}
