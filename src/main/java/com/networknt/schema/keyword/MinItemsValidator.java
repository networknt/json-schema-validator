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

/**
 * {@link KeywordValidator} for minItems.
 */
public class MinItemsValidator extends BaseKeywordValidator implements KeywordValidator {
    private int min = 0;

    public MinItemsValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(Keywords.MIN_ITEMS, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        if (schemaNode.canConvertToExactIntegral()) {
            min = schemaNode.intValue();
        }
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        

        if (node.isArray()) {
            if (node.size() < min) {
                executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(min, node.size())
                        .build());
            }
        } else if (this.schemaContext.getSchemaRegistryConfig().isTypeLoose()) {
            if (1 < min) {
                executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(min, 1).build());
            }
        }
    }

}
