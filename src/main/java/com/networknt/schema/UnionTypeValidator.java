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

public class UnionTypeValidator extends BaseJsonValidator implements JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(UnionTypeValidator.class);

    private final List<JsonValidator> schemas = new ArrayList<JsonValidator>();
    private final String error;


    public UnionTypeValidator(JsonNodePath schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.UNION_TYPE, validationContext);
        this.validationContext = validationContext;
        StringBuilder errorBuilder = new StringBuilder();

        String sep = "";
        errorBuilder.append('[');

        if (!schemaNode.isArray())
            throw new JsonSchemaException("Expected array for type property on Union Type Definition.");

        int i = 0;
        for (JsonNode n : schemaNode) {
            JsonType t = TypeFactory.getSchemaNodeType(n);
            errorBuilder.append(sep).append(t);
            sep = ", ";

            if (n.isObject())
                schemas.add(validationContext.newSchema(UriReference.get(ValidatorTypeCode.TYPE.getValue()), n, parentSchema));
            else
                schemas.add(new TypeValidator(schemaPath.resolve(i), n, parentSchema, validationContext));

            i++;
        }

        errorBuilder.append(']');

        error = errorBuilder.toString();
    }

    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath at) {
        debug(logger, node, rootNode, at);

        JsonType nodeType = TypeFactory.getValueNodeType(node, validationContext.getConfig());

        boolean valid = false;

        for (JsonValidator schema : schemas) {
            Set<ValidationMessage> errors = schema.validate(executionContext, node, rootNode, at);
            if (errors == null || errors.isEmpty()) {
                valid = true;
                break;
            }
        }

        if (!valid) {
            return Collections.singleton(buildValidationMessage(null, at,
                    executionContext.getExecutionConfig().getLocale(), nodeType.toString(), error));
        }

        return Collections.emptySet();
    }

    @Override
    public void preloadJsonSchema() {
        for (final JsonValidator validator : schemas) {
            validator.preloadJsonSchema();
        }
    }
}
