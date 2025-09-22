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
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaException;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;
import com.networknt.schema.annotation.JsonNodeAnnotation;
import com.networknt.schema.regex.RegularExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * {@link KeywordValidator} for patternProperties.
 */
public class PatternPropertiesValidator extends BaseKeywordValidator {
    public static final String PROPERTY = "patternProperties";
    private static final Logger logger = LoggerFactory.getLogger(PatternPropertiesValidator.class);
    private final Map<RegularExpression, JsonSchema> schemas = new IdentityHashMap<>();

    private Boolean hasUnevaluatedPropertiesValidator = null;

    public PatternPropertiesValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema,
                                      ValidationContext validationContext) {
        super(ValidatorTypeCode.PATTERN_PROPERTIES, schemaNode, schemaLocation, parentSchema, validationContext, evaluationPath);
        if (!schemaNode.isObject()) {
            throw new JsonSchemaException("patternProperties must be an object node");
        }
        Iterator<String> names = schemaNode.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            RegularExpression pattern = RegularExpression.compile(name, validationContext);
            schemas.put(pattern, validationContext.newSchema(schemaLocation.append(name), evaluationPath.append(name),
                    schemaNode.get(name), parentSchema));
        }
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        if (!node.isObject()) {
            return;
        }
        Set<String> matchedInstancePropertyNames = null;
        Iterator<String> names = node.fieldNames();
        boolean collectAnnotations = collectAnnotations() || collectAnnotations(executionContext);
        while (names.hasNext()) {
            String name = names.next();
            JsonNode n = node.get(name);
            for (Map.Entry<RegularExpression, JsonSchema> entry : schemas.entrySet()) {
                if (entry.getKey().matches(name)) {
                    JsonNodePath path = instanceLocation.append(name);
                    int currentErrors = executionContext.getErrors().size();
                    entry.getValue().validate(executionContext, n, rootNode, path);
                    if (currentErrors == executionContext.getErrors().size()) { // No new errors
                        if (collectAnnotations) {
                            if (matchedInstancePropertyNames == null) {
                                matchedInstancePropertyNames = new LinkedHashSet<>();
                            }
                            matchedInstancePropertyNames.add(name);
                        }
                    }
                }
            }
        }
        if (collectAnnotations) {
            executionContext.getAnnotations()
                    .put(JsonNodeAnnotation.builder().instanceLocation(instanceLocation)
                            .evaluationPath(this.evaluationPath).schemaLocation(this.schemaLocation)
                            .keyword(getKeyword())
                            .value(matchedInstancePropertyNames != null ? matchedInstancePropertyNames
                                    : Collections.emptySet())
                            .build());
        }
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
        preloadJsonSchemas(schemas.values());
        collectAnnotations(); // cache the flag
    }
}
