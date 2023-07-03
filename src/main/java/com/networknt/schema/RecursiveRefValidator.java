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
import com.networknt.schema.CollectorContext.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;

public class RecursiveRefValidator extends BaseJsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(RecursiveRefValidator.class);

    public RecursiveRefValidator(String schemaPath, JsonNode schemaNode, JsonSchema parentSchema, ValidationContext validationContext) {
        super(schemaPath, schemaNode, parentSchema, ValidatorTypeCode.RECURSIVE_REF, validationContext);

        String refValue = schemaNode.asText();
        if (!"#".equals(refValue)) {
            throw new JsonSchemaException(
                ValidationMessage.of(
                    ValidatorTypeCode.RECURSIVE_REF.getValue(),
                    CustomErrorMessageType.of("internal.invalidRecursiveRef"),
                    new MessageFormat("{0}: The value of a $recursiveRef must be '#' but is '{1}'"),
                    schemaPath, schemaPath, refValue
                )
            );
        }
    }

    @Override
    public Set<ValidationMessage> validate(JsonNode node, JsonNode rootNode, String at) {
        CollectorContext collectorContext = CollectorContext.getInstance();

        Set<ValidationMessage> errors = new HashSet<>();

        Scope parentScope = collectorContext.enterDynamicScope();
        try {
            debug(logger, node, rootNode, at);

            JsonSchema schema = collectorContext.getOutermostSchema();
            if (null != schema) {
                // This is important because if we use same JsonSchemaFactory for creating multiple JSONSchema instances,
                // these schemas will be cached along with config. We have to replace the config for cached $ref references
                // with the latest config. Reset the config.
                schema.getValidationContext().setConfig(getParentSchema().getValidationContext().getConfig());
                errors =  schema.validate(node, rootNode, at);
            }
        } finally {
            Scope scope = collectorContext.exitDynamicScope();
            if (errors.isEmpty()) {
                parentScope.mergeWith(scope);
            }
        }

        return errors;
    }

    @Override
    public Set<ValidationMessage> walk(JsonNode node, JsonNode rootNode, String at, boolean shouldValidateSchema) {
        CollectorContext collectorContext = CollectorContext.getInstance();

        Set<ValidationMessage> errors = new HashSet<>();

        Scope parentScope = collectorContext.enterDynamicScope();
        try {
            debug(logger, node, rootNode, at);

            JsonSchema schema = collectorContext.getOutermostSchema();
            if (null != schema) {
                // This is important because if we use same JsonSchemaFactory for creating multiple JSONSchema instances,
                // these schemas will be cached along with config. We have to replace the config for cached $ref references
                // with the latest config. Reset the config.
                schema.getValidationContext().setConfig(getParentSchema().getValidationContext().getConfig());
                errors = schema.walk(node, rootNode, at, shouldValidateSchema);
            }
        } finally {
            Scope scope = collectorContext.exitDynamicScope();
            if (shouldValidateSchema) {
                if (errors.isEmpty()) {
                    parentScope.mergeWith(scope);
                }
            }
        }

        return errors;
    }

}
