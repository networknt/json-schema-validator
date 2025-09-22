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
import com.networknt.schema.Error;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.JsonNodePath;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * {@link KeywordValidator} for not.
 */
public class NotValidator extends BaseKeywordValidator {
    private static final Logger logger = LoggerFactory.getLogger(NotValidator.class);

    private final JsonSchema schema;

    public NotValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(ValidatorTypeCode.NOT, schemaNode, schemaLocation, parentSchema, validationContext, evaluationPath);
        this.schema = validationContext.newSchema(schemaLocation, evaluationPath, schemaNode, parentSchema);
    }

    @Override
    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation) {
        validate(executionContext, node, rootNode, instanceLocation, false);
    }

    protected void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode,
            JsonNodePath instanceLocation, boolean walk) {
        
        debug(logger, executionContext, node, rootNode, instanceLocation);

        // Save flag as nested schema evaluation shouldn't trigger fail fast
        boolean failFast = executionContext.isFailFast();
        List<Error> existingErrors = executionContext.getErrors();
        List<Error> test = new ArrayList<>();
        executionContext.setErrors(test);
        try {
            executionContext.setFailFast(false);
            if (!walk) {
                this.schema.validate(executionContext, node, rootNode, instanceLocation);
            } else {
                this.schema.walk(executionContext, node, rootNode, instanceLocation, true);
            }
        } finally {
            // Restore flag
            executionContext.setFailFast(failFast);
            executionContext.setErrors(existingErrors);
        }
        if (test.isEmpty()) {
            executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                    .locale(executionContext.getExecutionConfig().getLocale())
                    .arguments(this.schema.toString())
                    .build());
        }
    }
    
    @Override
    public void walk(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation, boolean shouldValidateSchema) {
        if (shouldValidateSchema && node != null) {
            validate(executionContext, node, rootNode, instanceLocation, true);
            return;
        }

        this.schema.walk(executionContext, node, rootNode, instanceLocation, false);
    }

    @Override
    public void preloadJsonSchema() {
        if (null != this.schema) {
            this.schema.initializeValidators();
        }
    }
}
