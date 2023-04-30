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

package com.networknt.schema;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class NotValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RequiredValidator.class);

    private final JsonSchema schema;

    public NotValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.NOT, validationContext);
        schema = new JsonSchema(validationContext, schemaPath, parentSchema.getCurrentUri(), schemaNode, parentSchema);

        parseErrorCode(getValidatorType().getErrorCodeKey());
    }

    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        Set<ValidationMessage> errors = new HashSet<>();

        //As not will contain a schema take a backup of evaluatedProperties.
        Collection<String> backupEvaluatedProperties = CollectorContext.getInstance().getEvaluatedProperties();

        // Make the evaluatedProperties list empty.
        CollectorContext.getInstance().resetEvaluatedProperties();

        try {
            debug(logger, node, rootNode, at);
            errors = schema.validate(node, rootNode, at);
            if (errors.isEmpty()) {
                return Collections.singleton(buildValidationMessage(at, schema.toString()));
            }
            return Collections.emptySet();
        } finally {
            if (errors.isEmpty()) {
                CollectorContext.getInstance().getEvaluatedProperties().addAll(backupEvaluatedProperties);
            } else {
                CollectorContext.getInstance().setEvaluatedProperties(backupEvaluatedProperties);
            }
        }
    }
    
    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        if (shouldValidateSchema) {
            return validate(node, rootNode, at);
        }
    	return schema.walk(node, rootNode, at, shouldValidateSchema);
    }

    @Override
    public void preloadJsonSchema() {
        if (null != schema) {
            schema.initializeValidators();
        }
    }
}
