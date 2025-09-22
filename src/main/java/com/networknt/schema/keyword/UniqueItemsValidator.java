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
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link KeywordValidator} for uniqueItems.
 */
public class UniqueItemsValidator extends BaseKeywordValidator implements KeywordValidator {
    private static final Logger logger = LoggerFactory.getLogger(UniqueItemsValidator.class);

    private final boolean unique;

    public UniqueItemsValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, ValidationContext validationContext) {
        super(ValidatorTypeCode.UNIQUE_ITEMS, schemaNode, schemaLocation, parentSchema, validationContext, evaluationPath);
        if (schemaNode.isBoolean()) {
            unique = schemaNode.booleanValue();
        } else {
            unique = false;
        }
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        if (unique) {
            Set<JsonNode> set = new HashSet<>();
            for (JsonNode n : node) {
                if (!set.add(n)) {
                    executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                            .locale(executionContext.getExecutionConfig().getLocale())
                            .build());
                }
            }
        }
    }

}
