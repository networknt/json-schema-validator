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
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaException;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.annotation.Annotation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.regex.RegularExpression;
import java.util.*;

/**
 * {@link KeywordValidator} for patternProperties.
 */
public class PatternPropertiesValidator extends BaseKeywordValidator {
    public static final String PROPERTY = "patternProperties";
    private final Map<RegularExpression, Schema> schemas = new IdentityHashMap<>();

    public PatternPropertiesValidator(SchemaLocation schemaLocation, JsonNode schemaNode, Schema parentSchema,
                                      SchemaContext schemaContext) {
        super(KeywordType.PATTERN_PROPERTIES, schemaNode, schemaLocation, parentSchema, schemaContext);
        if (!schemaNode.isObject()) {
            throw new SchemaException("patternProperties must be an object node");
        }
        Iterator<String> names = schemaNode.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            RegularExpression pattern = RegularExpression.compile(name, schemaContext);
            schemas.put(pattern, schemaContext.newSchema(schemaLocation.append(name), 
                    schemaNode.get(name), parentSchema));
        }
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        

        if (!node.isObject()) {
            return;
        }
        Set<String> matchedInstancePropertyNames = null;
        Iterator<String> names = node.fieldNames();
        boolean collectAnnotations = hasUnevaluatedPropertiesInEvaluationPath(executionContext) || collectAnnotations(executionContext);
        while (names.hasNext()) {
            String name = names.next();
            JsonNode n = node.get(name);
            for (Map.Entry<RegularExpression, Schema> entry : schemas.entrySet()) {
                if (entry.getKey().matches(name)) {
                    NodePath path = instanceLocation.append(name);
                    int currentErrors = executionContext.getErrors().size();
                    Schema schema = entry.getValue();
                    executionContext.evaluationPathAddLast(schema.getSchemaLocation().getFragment().getElement(-1).toString());
                    try {
                        schema.validate(executionContext, n, rootNode, path);
                    } finally {
                        executionContext.evaluationPathRemoveLast();
                    }
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
                    .put(Annotation.builder().instanceLocation(instanceLocation)
                            .evaluationPath(executionContext.getEvaluationPath()).schemaLocation(this.schemaLocation)
                            .keyword(getKeyword())
                            .value(matchedInstancePropertyNames != null ? matchedInstancePropertyNames
                                    : Collections.emptySet())
                            .build());
        }
    }
    
    @Override
    public void preloadSchema() {
        preloadSchemas(schemas.values());
    }
}
