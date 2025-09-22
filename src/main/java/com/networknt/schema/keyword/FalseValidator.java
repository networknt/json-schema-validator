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
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationContext;

/**
 * {@link KeywordValidator} for false.
 */
public class FalseValidator extends BaseKeywordValidator implements KeywordValidator {
    private final String reason;

    public FalseValidator(SchemaLocation schemaLocation, JsonNodePath evaluationPath, final JsonNode schemaNode, Schema parentSchema, ValidationContext validationContext) {
        super(ValidatorTypeCode.FALSE, schemaNode, schemaLocation, parentSchema, validationContext, evaluationPath);
        this.reason = this.evaluationPath.getParent().getName(-1);
    }

    public void validate(ExecutionContext executionContext, JsonNode node, JsonNode rootNode, JsonNodePath instanceLocation) {
        // For the false validator, it is always not valid
        executionContext.addError(error().instanceNode(node).instanceLocation(instanceLocation)
                .locale(executionContext.getExecutionConfig().getLocale())
                .arguments(reason).build());
    }
}
