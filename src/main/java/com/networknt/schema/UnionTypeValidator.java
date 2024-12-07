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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * {@link JsonValidator} for type union.
 */
public class UnionTypeValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UnionTypeValidator.class);

    private final List<JsonValidator> schemas;
    private final String error;

    public UnionTypeValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.TYPE, validationContext);
        StringBuilder errorBuilder = new StringBuilder();

        String sep = "";
        errorBuilder.append('[');

        if (!schemaNode.isArray()) {
            throw new JsonSchemaException("Expected array for type property on Union Type Definition.");
        }

        int i = 0;
        this.schemas = new ArrayList<>(schemaNode.size());
        for (JsonNode n : schemaNode) {
            JsonType t = TypeFactory.getSchemaNodeType(n);
            errorBuilder.append(sep).append(t);
            sep = ", ";

            if (n.isObject()) {
                schemas.add(validationContext.newSchema(schemaLocation.append(ValidatorTypeCode.TYPE.getValue()),
                        evaluationPath.append(ValidatorTypeCode.TRUE.getValue()), n, parentSchema));
            } else {
                schemas.add(new TypeValidator(schemaLocation.append(i), evaluationPath.append(i), n, parentSchema,
                        validationContext));
            }
            i++;
        }

        errorBuilder.append(']');

        error = errorBuilder.toString();
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        JsonType nodeType = TypeFactory.getValueNodeType(node, validationContext.getConfig());

        boolean valid = false;

        // Save flag as nested schema evaluation shouldn't trigger fail fast
        boolean failFast = executionContext.isFailFast();
        try {
            executionContext.setFailFast(false);
            for (JsonValidator schema : schemas) {
                Set<ValidationMessage> errors = schema.validate(executionContext, node, rootNode, instanceLocation);
                if (errors == null || errors.isEmpty()) {
                    valid = true;
                    break;
                }
            }
        } finally {
            // Restore flag
            executionContext.setFailFast(failFast);
        }

        if (!valid) {
            return Collections.singleton(message().instanceNode(node).instanceLocation(instanceLocation)
                    .type("type")
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .failFast(executionContext.isFailFast()).arguments(nodeType.toString(), error)
                    .build());
        }

        return Collections.emptySet();
    }

    @Override
    public void preloadJsonSchema() {
        for (final JsonValidator validator : schemas) {
            validator.preloadJsonSchema();
        }
    }

    @Override
    public String getKeyword() {
        return "type";
    }
}
