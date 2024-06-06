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

/**
 * {@link JsonValidator} for type.
 */
public class TypeValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(TypeValidator.class);

    private final JsonType schemaType;
    private final UnionTypeValidator unionTypeValidator;

    public TypeValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaLocation, evaluationPath, schemaNode, parentSchema, ValidatorTypeCode.TYPE, validationContext);
        this.schemaType = TypeFactory.getSchemaNodeType(schemaNode);
        if (this.schemaType == JsonType.UNION) {
            this.unionTypeValidator = new UnionTypeValidator(schemaLocation, evaluationPath, schemaNode, parentSchema, validationContext);
        } else {
            this.unionTypeValidator = null;
        }
    }

    public JsonType getSchemaType() {
        return this.schemaType;
    }

    public boolean equalsToSchemaType(JsonNode node) {
        return JsonNodeUtil.equalsToSchemaType(node, this.schemaType, this.parentSchema, this.validationContext);
    }

    @Override
    public Set<ValidationMessage> validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        if (this.schemaType == JsonType.UNION) {
            return this.unionTypeValidator.validate(executionContext, node, rootNode, instanceLocation);
        }

        if (!equalsToSchemaType(node)) {
            JsonType nodeType = TypeFactory.getValueNodeType(node, this.validationContext.getConfig());
            return Collections.singleton(message().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .failFast(executionContext.isFailFast())
                    .arguments(nodeType.toString(), this.schemaType.toString()).build());
        }
        return Collections.emptySet();
    }
}
