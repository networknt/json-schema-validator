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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdditionalPropertiesValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(AdditionalPropertiesValidator.class);

    private final boolean allowAdditionalProperties;
    private final JsonSchema additionalPropertiesSchema;
    private final Set<String> allowedProperties;
    private final List<Pattern> patternProperties = new ArrayList<Pattern>();

    public AdditionalPropertiesValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema,
                                         ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.ADDITIONAL_PROPERTIES, validationContext);
        if (schemaNode.isBoolean()) {
            allowAdditionalProperties = schemaNode.booleanValue();
            additionalPropertiesSchema = null;
        } else if (schemaNode.isObject()) {
            allowAdditionalProperties = true;
            additionalPropertiesSchema = new JsonSchema(validationContext, getValidatorType().getValue(), parentSchema.getCurrentUri(), schemaNode, parentSchema);
        } else {
            allowAdditionalProperties = false;
            additionalPropertiesSchema = null;
        }

        allowedProperties = new HashSet<String>();
        JsonNode propertiesNode = parentSchema.getSchemaNode().get(PropertiesValidator.PROPERTY);
        if (propertiesNode != null) {
            for (Iterator<String> it = propertiesNode.fieldNames(); it.hasNext(); ) {
                allowedProperties.add(it.next());
            }
        }

        JsonNode patternPropertiesNode = parentSchema.getSchemaNode().get(PatternPropertiesValidator.PROPERTY);
        if (patternPropertiesNode != null) {
            for (Iterator<String> it = patternPropertiesNode.fieldNames(); it.hasNext(); ) {
                patternProperties.add(Pattern.compile(it.next()));
            }
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    private void addToEvaluatedProperties(String propertyPath) {
        Object evaluatedProperties = CollectorContext.getInstance().get(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES);
        List<String> evaluatedPropertiesList = null;
        if (evaluatedProperties == null) {
            evaluatedPropertiesList = new ArrayList<>();
            CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, evaluatedPropertiesList);
        } else {
            evaluatedPropertiesList = (List<String>) evaluatedProperties;
        }
        evaluatedPropertiesList.add(propertyPath);
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        if (logger.isDebugEnabled()) debug(logger, node, rootNode, at);

        Set<ValidationMessage> errors = new LinkedHashSet<ValidationMessage>();
        if (!node.isObject()) {
            // ignore no object
            return errors;
        }

        // if allowAdditionalProperties is true, add all the properties as evaluated.
        if (allowAdditionalProperties) {
            for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
                addToEvaluatedProperties(at + "." + it.next());
            }
        }

        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String pname = it.next();
            // skip the context items
            if (pname.startsWith("#")) {
                continue;
            }
            boolean handledByPatternProperties = false;
            for (Pattern pattern : patternProperties) {
                Matcher m = pattern.matcher(pname);
                if (m.find()) {
                    handledByPatternProperties = true;
                    break;
                }
            }

            if (!allowedProperties.contains(pname) && !handledByPatternProperties) {
                if (!allowAdditionalProperties) {
                    errors.add(buildValidationMessage(at, pname));
                } else {
                    if (additionalPropertiesSchema != null) {
                        ValidatorState state = (ValidatorState) CollectorContext.getInstance().get(ValidatorState.VALIDATOR_STATE_KEY);
                        if (state != null && state.isWalkEnabled()) {
                            errors.addAll(additionalPropertiesSchema.walk(node.get(pname), rootNode, at + "." + pname, state.isValidationEnabled()));
                        } else {
                            errors.addAll(additionalPropertiesSchema.validate(node.get(pname), rootNode, at + "." + pname));
                        }
                    }
                }
            }
        }
        return Collections.unmodifiableSet(errors);
    }

    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            return validate(node, rootNode, at);
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
            for (Pattern pattern : patternProperties) {
                Matcher m = pattern.matcher(pname);
                if (m.find()) {
                    handledByPatternProperties = true;
                    break;
                }
            }

            if (!allowedProperties.contains(pname) && !handledByPatternProperties) {
                if (allowAdditionalProperties) {
                    if (additionalPropertiesSchema != null) {
                        ValidatorState state = (ValidatorState) CollectorContext.getInstance().get(ValidatorState.VALIDATOR_STATE_KEY);
                        if (state != null && state.isWalkEnabled()) {
                           additionalPropertiesSchema.walk(node.get(pname), rootNode, at + "." + pname, state.isValidationEnabled());
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
