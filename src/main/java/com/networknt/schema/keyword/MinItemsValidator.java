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
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link KeywordValidator} for minItems.
 */
public class MinItemsValidator extends BaseKeywordValidator implements KeywordValidator {
    private static final Logger logger = LoggerFactory.getLogger(MinItemsValidator.class);

    private int min = 0;

    public MinItemsValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(ValidatorTypeCode.MIN_ITEMS, schemaNode, schemaLocation, parentSchema, validationContext, evaluationPath);
        if (schemaNode.canConvertToExactIntegral()) {
            min = schemaNode.intValue();
        }
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        if (node.isArray()) {
            if (node.size() < min) {
                executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(min, node.size())
                        .build());
            }
        } else if (this.validationContext.getConfig().isTypeLoose()) {
            if (1 < min) {
                executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(min, 1).build());
            }
        }
    }

}
