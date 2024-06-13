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
import com.networknt.schema.annotation.JsonNodeAnnotation;
import com.networknt.schema.regex.RegularExpression;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link JsonValidator} for additionalProperties.
 */
public class AdditionalPropertiesValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(AdditionalPropertiesValidator.class);

    private final boolean allowAdditionalProperties;
    private final JsonSchema additionalPropertiesSchema;
    private final Set<String> allowedProperties;
    private final List<RegularExpression> patternProperties;

    private Boolean hasUnevaluatedPropertiesValidator;

    public AdditionalPropertiesValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema,
                                         ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.ADDITIONAL_PROPERTIES, validationContext);
        if (schemaNode.isBoolean()) {
            allowAdditionalProperties = schemaNode.booleanValue();
            additionalPropertiesSchema = null;
        } else if (schemaNode.isObject()) {
            allowAdditionalProperties = true;
            additionalPropertiesSchema = validationContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
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
            this.patternProperties = new ArrayList<>(patternPropertiesNode.size());
            for (Iterator<String> it = patternPropertiesNode.fieldNames(); it.hasNext(); ) {
                patternProperties.add(RegularExpression.compile(it.next(), validationContext));
            }
        } else {
            this.patternProperties = Collections.emptyList();
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
        if (!node.isObject()) {
            // ignore no object
            return Collections.emptySet();
        }

        Set<String> matchedInstancePropertyNames = null;
        
        boolean collectAnnotations = collectAnnotations() || collectAnnotations(executionContext);
        // if allowAdditionalProperties is true, add all the properties as evaluated.
        if (allowAdditionalProperties && collectAnnotations) {
            for (Iterator<String> it = node.fieldNames(); it.hasNext();) {
                if (matchedInstancePropertyNames == null) {
                    matchedInstancePropertyNames = new LinkedHashSet<>();
                }
                String fieldName = it.next();
                matchedInstancePropertyNames.add(fieldName);
            }
        }

        Set<ValidationMessage> errors = null;

        for (Iterator<Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
            Entry<String, JsonNode> entry = it.next();
            String pname = entry.getKey();
            // skip the context items
            if (pname.startsWith("#")) {
                continue;
            }
            if (!allowedProperties.contains(pname) && !handledByPatternProperties(pname)) {
                if (!allowAdditionalProperties) {
                    if (errors == null) {
                        errors = new LinkedHashSet<>();
                    }
                    errors.add(message().instanceNode(node).property(pname)
                            .instanceLocation(instanceLocation)
                            .locale(executionContext.getExecutionConfig().getLocale())
                            .failFast(executionContext.isFailFast()).arguments(pname).build());
                } else {
                    if (additionalPropertiesSchema != null) {
                        Set<ValidationMessage> results = !walk
                                ? additionalPropertiesSchema.validate(executionContext, entry.getValue(), rootNode,
                                        instanceLocation.append(pname))
                                : additionalPropertiesSchema.walk(executionContext, entry.getValue(), rootNode,
                                        instanceLocation.append(pname), true);
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
        if (collectAnnotations) {
            executionContext.getAnnotations().put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                    .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation).keyword(getKeyword())
                    .value(matchedInstancePropertyNames != null ? matchedInstancePropertyNames : Collections.emptySet())
                    .build());
        }
        return errors == null ? Collections.emptySet() : Collections.unmodifiableSet(errors);
    }

    @Override
    public Set<ValidationMessage> walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        if (shouldValidateSchema && node != null) {
            return validate(executionContext, node, rootNode, instanceLocation, true);
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
            if (!allowedProperties.contains(pname) && !handledByPatternProperties(pname)) {
                if (allowAdditionalProperties) {
                    if (additionalPropertiesSchema != null) {
                        additionalPropertiesSchema.walk(executionContext, node.get(pname), rootNode,
                                instanceLocation.append(pname), shouldValidateSchema);
                    }
                }
            }
        }
        return Collections.emptySet();
    }

    private boolean handledByPatternProperties(String pname) {
        for (RegularExpression pattern : this.patternProperties) {
            if (pattern.matches(pname)) {
                return true;
            }
        }
        return false;
    }

    private boolean collectAnnotations() {
        return hasUnevaluatedPropertiesValidator();
    }

    private boolean hasUnevaluatedPropertiesValidator() {
        if (this.hasUnevaluatedPropertiesValidator == null) {
            this.hasUnevaluatedPropertiesValidator = hasAdjacentKeywordInEvaluationPath("unevaluatedProperties");
        }
        return hasUnevaluatedPropertiesValidator;
    }

    @Override
    public void preloadJsonSchema() {
        if(additionalPropertiesSchema != null) {
            additionalPropertiesSchema.initializeValidators();
        }
        collectAnnotations(); // cache the flag
    }
}
