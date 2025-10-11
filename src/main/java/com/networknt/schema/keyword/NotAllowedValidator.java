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
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.path.NodePath;
import com.networknt.schema.SchemaContext;

import java.util.*;

/**
 * {@link KeywordValidator} for notAllowed.
 */
public class NotAllowedValidator extends BaseKeywordValidator implements KeywordValidator {
    private final List<String> fieldNames;

    public NotAllowedValidator(SchemaLocation schemaLocation, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(KeywordType.NOT_ALLOWED, schemaNode, schemaLocation, parentSchema, schemaContext);
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

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        

        for (String fieldName : fieldNames) {
            JsonNode propertyNode = node.get(fieldName);

            if (propertyNode != null) {
                executionContext.addError(error().property(fieldName).instanceNode(node)
                        .evaluationPath(executionContext.getEvaluationPath()).instanceLocation(instanceLocation.append(fieldName))
                        .locale(executionContext.getExecutionConfig().getLocale())
                        .arguments(fieldName).build());
            }
        }
    }

}
