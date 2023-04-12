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
import com.networknt.schema.utils.JsonNodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TypeValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(TypeValidator.class);

    private JsonType schemaType;
    private JsonSchema parentSchema;
    private UnionTypeValidator unionTypeValidator;

    public TypeValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.TYPE, validationContext);
        schemaType = TypeFactory.getSchemaNodeType(schemaNode);
        this.parentSchema = parentSchema;
        this.validationContext = validationContext;
        if (schemaType == JsonType.UNION) {
            unionTypeValidator = new UnionTypeValidator(schemaPath, schemaNode, parentSchema, validationContext);
        }

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public JsonType getSchemaType() {
        return schemaType;
    }

    public boolean equalsToSchemaType(JsonNode node) {
        return JsonNodeUtil.equalsToSchemaType(node,schemaType, parentSchema, validationContext);
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        debug(logger, node, rootNode, at);

        if (schemaType == JsonType.UNION) {
            return unionTypeValidator.validate(node, rootNode, at);
        }

        if (!equalsToSchemaType(node)) {
            JsonType nodeType = TypeFactory.getValueNodeType(node, validationContext.getConfig());
            return Collections.singleton(buildValidationMessage(at, nodeType.toString(), schemaType.toString()));
        }
        // Hack to catch evaluated properties if additionalProperties is given as "additionalProperties":{"type":"string"}
        // Hack to catch patternProperties like "^foo":"value"
        if (schemaPath.endsWith("/type")) {
            addToEvaluatedProperties(at);
        }
        return Collections.emptySet();
    }

    private void addToEvaluatedProperties(String propertyPath) {
        @SuppressWarnings("unchecked")
        List<String> evaluatedProperties = (List<String>) CollectorContext.getInstance().get(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES);
        if (evaluatedProperties == null) {
            evaluatedProperties = new ArrayList<>();
            CollectorContext.getInstance().add(UnEvaluatedPropertiesValidator.EVALUATED_PROPERTIES, evaluatedProperties);
        }
        evaluatedProperties.add(propertyPath);
    }
}
