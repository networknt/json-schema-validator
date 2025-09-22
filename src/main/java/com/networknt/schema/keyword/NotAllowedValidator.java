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

import java.util.*;

/**
 * {@link KeywordValidator} for notAllowed.
 */
public class NotAllowedValidator extends BaseKeywordValidator implements KeywordValidator {
    private static final Logger logger = LoggerFactory.getLogger(NotAllowedValidator.class);

    private final List<String> fieldNames;

    public NotAllowedValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(ValidatorTypeCode.NOT_ALLOWED, schemaNode, schemaLocation, parentSchema, validationContext, evaluationPath);
        if (schemaNode.isArray()) {
            int size = schemaNode.size();
            this.fieldNames = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                fieldNames.add(schemaNode.get(i).asText());
            }
        } else {
            this.fieldNames = Collections.emptyList();
        }
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        debug(logger, executionContext, node, rootNode, instanceLocation);

        for (String fieldName : fieldNames) {
            JsonNode propertyNode = node.get(fieldName);

            if (propertyNode != null) {
                executionContext.addError(error().property(fieldName).instanceNode(node)
                        .instanceLocation(instanceLocation.append(fieldName))
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(fieldName).build());
            }
        }
    }

}
