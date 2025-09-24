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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.NodePath;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaContext;

/**
 * {@link KeywordValidator} for readOnly.
 */
public class ReadOnlyValidator extends BaseKeywordValidator {
    private static final Logger logger = LoggerFactory.getLogger(ReadOnlyValidator.class);

    public ReadOnlyValidator(SchemaLocation schemaLocation, NodePath evaluationPath, JsonNode schemaNode, Schema parentSchema, SchemaContext schemaContext) {
        super(Keywords.READ_ONLY, schemaNode, schemaLocation, parentSchema, schemaContext, evaluationPath);
        logger.debug("Loaded ReadOnlyValidator for property {} as {}", parentSchema, "read mode");
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, NodePath instanceLocation) {
        
        if (Boolean.TRUE.equals(executionContext.getExecutionConfig().getReadOnly())) {
            executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .build());
        }
        return;
    }

}