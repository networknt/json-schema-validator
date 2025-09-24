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
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.NodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.annotation.Annotation;
import com.networknt.schema.regex.RegularExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link KeywordValidator} for additionalProperties.
 */
public class AdditionalPropertiesValidator extends BaseKeywordValidator {
    private final boolean allowAdditionalProperties;
    private final Schema additionalPropertiesSchema;
    private final Set<String> allowedProperties;
    private final List<RegularExpression> patternProperties;

    private Boolean hasUnevaluatedPropertiesValidator;

    public AdditionalPropertiesValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema,
                                         SchemaContext schemaContext) {
        super(KeywordType.ADDITIONAL_PROPERTIES, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        if (schemaNode.isBoolean()) {
            allowAdditionalProperties = schemaNode.booleanValue();
            additionalPropertiesSchema = null;
        } else if (schemaNode.isObject()) {
            allowAdditionalProperties = true;
            additionalPropertiesSchema = schemaContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
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
                patternProperties.add(RegularExpression.compile(it.next(), schemaContext));
            }
        } else {
            this.patternProperties = Collections.emptyList();
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        validate(executionContext, node, rootNode, instanceLocation, false);
    }

    protected void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation, boolean walk) {
        if (!node.isObject()) {
            // ignore no object
            return;
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

        for (Iterator<Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
            Entry<String, JsonNode> entry = it.next();
            String pname = entry.getKey();
            // skip the context items
            if (pname.startsWith("#")) {
                continue;
            }
            if (!allowedProperties.contains(pname) && !handledByPatternProperties(pname)) {
                if (!allowAdditionalProperties) {
                    executionContext.addError(error().instanceNode(node).property(pname)
                            .instanceLocation(instanceLocation)
                            .locale(executionContext.getExecutionConfig().getLocale())
                            .arguments(pname).build());
                } else {
                    if (additionalPropertiesSchema != null) {
                        if (!walk) {
                            additionalPropertiesSchema.validate(executionContext, entry.getValue(), rootNode,
                                    instanceLocation.append(pname));
                        } else {
                            additionalPropertiesSchema.walk(executionContext, entry.getValue(), rootNode,
                                    instanceLocation.append(pname), true);   
                        }
                    }
                }
            }
        }
        if (collectAnnotations) {
            executionContext.getAnnotations().put(Annotation.builder().instanceLocation(instanceLocation)
                    .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation).keyword(getKeyword())
                    .value(matchedInstancePropertyNames != null ? matchedInstancePropertyNames : Collections.emptySet())
                    .build());
        }
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation, boolean shouldValidateSchema) {
        if (shouldValidateSchema && node != null) {
            validate(executionContext, node, rootNode, instanceLocation, true);
            return;
        }

        if (node == null || !node.isObject()) {
            // ignore no object
            return;
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
    public void preloadSchema() {
        if(additionalPropertiesSchema != null) {
            additionalPropertiesSchema.initializeValidators();
        }
        collectAnnotations(); // cache the flag
    }
}
