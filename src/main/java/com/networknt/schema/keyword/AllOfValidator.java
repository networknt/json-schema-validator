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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaContext;
import com.networknt.schema.SchemaException;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.utils.JsonType;
import com.networknt.schema.utils.TypeFactory;

/**
 * {@link KeywordValidator} for allOf.
 */
public class AllOfValidator extends BaseKeywordValidator {
    private final List<Schema> schemas;

    public AllOfValidator(SchemaLocation schemaLocation, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.ALL_OF, schemaNode, schemaLocation, parentSchema, schemaContext);
        if (!schemaNode.isArray()) {
            JsonType nodeType = TypeFactory.getValueNodeType(schemaNode, this.schemaContext.getSchemaRegistryConfig());
            throw new SchemaException(error().instanceNode(schemaNode).instanceLocation(schemaLocation.getFragment())
                    .messageKey("type").arguments(nodeType.toString(), "array").build());
        }
        int size = schemaNode.size();
        this.schemas = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.schemas.add(schemaContext.newSchema(schemaLocation.append(i),
                    schemaNode.get(i), parentSchema));
        }
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation) {
        validate(executionContext, node, rootNode, instanceLocation, false);
    }

    protected void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            NodePath instanceLocation, boolean walk) {
        int schemaIndex = 0;
        for (Schema schema : this.schemas) {
            executionContext.evaluationPathAddLast(schemaIndex);
            try {
                if (!walk) {
                    schema.validate(executionContext, node, rootNode, instanceLocation);
                } else {
                    schema.walk(executionContext, node, rootNode, instanceLocation, true);
                }
            } finally {
                executionContext.evaluationPathRemoveLast();
            }
            schemaIndex++;
        }
    }

    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation,
            boolean shouldValidateSchema) {
        if (shouldValidateSchema && node != null) {
            validate(executionContext, node, rootNode, instanceLocation, true);
            return;
        }
        int schemaIndex = 0;
        for (Schema schema : this.schemas) {
            // Walk through the schema
            executionContext.evaluationPathAddLast(schemaIndex);
            try {
                schema.walk(executionContext, node, rootNode, instanceLocation, false);
            } finally {
                executionContext.evaluationPathRemoveLast();
            }
            schemaIndex++;
        }
    }

    @Override
    public void preloadSchema() {
        preloadSchemas(this.schemas);
    }
}
