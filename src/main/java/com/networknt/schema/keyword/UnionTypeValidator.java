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
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaException;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.Validator;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.utils.JsonType;
import com.networknt.schema.utils.TypeFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link KeywordValidator} for type union.
 */
public class UnionTypeValidator extends BaseKeywordValidator implements KeywordValidator {
    private final List<Validator> schemas;
    private final String error;

    public UnionTypeValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.TYPE, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        StringBuilder errorBuilder = new StringBuilder();

        String sep = "";
        errorBuilder.append('[');

        if (!schemaNode.isArray()) {
            throw new SchemaException("Expected array for type property on Union Type Definition.");
        }

        int i = 0;
        this.schemas = new ArrayList<>(schemaNode.size());
        for (JsonNode n : schemaNode) {
            JsonType t = TypeFactory.getSchemaNodeType(n);
            errorBuilder.append(sep).append(t);
            sep = ", ";

            if (n.isObject()) {
                schemas.add(schemaContext.newSchema(schemaLocation.append(KeywordType.TYPE.getValue()),
                        evaluationPath.append(KeywordType.TRUE.getValue()), n, parentSchema));
            } else {
                schemas.add(new TypeValidator(schemaLocation.append(i), evaluationPath.append(i), n, parentSchema,
                        schemaContext));
            }
            i++;
        }

        errorBuilder.append(']');

        error = errorBuilder.toString();
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        

        JsonType nodeType = TypeFactory.getValueNodeType(node, schemaContext.getSchemaRegistryConfig());

        boolean valid = false;

        // Save flag as nested schema evaluation shouldn't trigger fail fast
        boolean failFast = executionContext.isFailFast();
        List<Error> existingErrors = executionContext.getErrors();
        try {
            List<Error> test = new ArrayList<>();
            executionContext.setFailFast(false);
            executionContext.setErrors(test);
            for (Validator schema : schemas) {
                schema.validate(executionContext, node, rootNode, instanceLocation);
                if (test.isEmpty()) {
                    valid = true;
                    break;
                } else {
                    test.clear();
                }
            }
        } finally {
            // Restore flag
            executionContext.setFailFast(failFast);
            executionContext.setErrors(existingErrors);
        }

        if (!valid) {
            executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                    .keyword("type")
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .arguments(nodeType.toString(), error)
                    .build());
        }
    }

    @Override
    public void preloadSchema() {
        for (final Validator validator : schemas) {
            if (validator instanceof KeywordValidator) {
                ((KeywordValidator) validator).preloadSchema();
            } else if (validator instanceof Schema) {
                ((Schema) validator).initializeValidators();
            }
        }
    }

    @Override
    public String getKeyword() {
        return "type";
    }
}
