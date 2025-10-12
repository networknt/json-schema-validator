/*
 * Copyright (c) 2020 Network New Technologies Inc.
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
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.SchemaContext;

/**
 * {@link KeywordValidator} for const.
 */
public class ConstValidator extends BaseKeywordValidator implements KeywordValidator {
    public ConstValidator(SchemaLocation schemaLocation, JsonNode schemaNode,
            Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.CONST, schemaNode, schemaLocation, parentSchema, schemaContext);
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        if (schemaNode.isNumber() && node.isNumber()) {
            if (schemaNode.decimalValue().compareTo(node.decimalValue()) != 0) {
                executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                        .evaluationPath(executionContext.getEvaluationPath()).locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(schemaNode.asText(), node.asText())
                        .build());
            }
        } else if (!schemaNode.equals(node)) {
            executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                    .evaluationPath(executionContext.getEvaluationPath()).locale(executionContext.getExecutionConfig().getLocale()).arguments(schemaNode.asText(), node.asText()).build());
        }
    }
}
