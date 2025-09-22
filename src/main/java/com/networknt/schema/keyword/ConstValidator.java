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
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link KeywordValidator} for const.
 */
public class ConstValidator extends BaseKeywordValidator implements KeywordValidator {
    private static final Logger logger = LoggerFactory.getLogger(ConstValidator.class);

    public ConstValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode,
            JsonSchema parentSchema, ValidationContext validationContext) {
        super(ValidatorTypeCode.CONST, schemaNode, schemaLocation, parentSchema, validationContext, evaluationPath);
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        if (schemaNode.isNumber() && node.isNumber()) {
            if (schemaNode.decimalValue().compareTo(node.decimalValue()) != 0) {
                executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(schemaNode.asText(), node.asText())
                        .build());
            }
        } else if (!schemaNode.equals(node)) {
            executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale()).arguments(schemaNode.asText(), node.asText()).build());
        }
    }
}
