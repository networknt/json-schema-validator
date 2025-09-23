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
import com.networknt.schema.JsonType;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.TypeFactory;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.utils.JsonNodeUtil;

/**
 * {@link KeywordValidator} for type.
 */
public class TypeValidator extends BaseKeywordValidator {
    private final JsonType schemaType;
    private final UnionTypeValidator unionTypeValidator;

    public TypeValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(ValidatorTypeCode.TYPE, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        this.schemaType = TypeFactory.getSchemaNodeType(schemaNode);
        if (this.schemaType == JsonType.UNION) {
            this.unionTypeValidator = new UnionTypeValidator(schemaLocation, evaluationPath, schemaNode, parentSchema, schemaContext);
        } else {
            this.unionTypeValidator = null;
        }
    }

    public JsonType getSchemaType() {
        return this.schemaType;
    }

    public boolean equalsToSchemaType(JsonNode node) {
        return JsonNodeUtil.equalsToSchemaType(node, this.schemaType, this.parentSchema, this.schemaContext);
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        

        if (this.schemaType == JsonType.UNION) {
            this.unionTypeValidator.validate(executionContext, node, rootNode, instanceLocation);
            return;
        }

        if (!equalsToSchemaType(node)) {
            JsonType nodeType = TypeFactory.getValueNodeType(node, this.schemaContext.getSchemaRegistryConfig());
            executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .arguments(nodeType.toString(), this.schemaType.toString()).build());
        }
    }
}
